(ns jutsu.core
  (:require
     [clojure.string     :as str]
     [ring.middleware.defaults :refer [site-defaults]]
     [compojure.core     :as comp :refer (defroutes GET POST)]
     [compojure.route    :as route]
     [hiccup.core        :as hiccup]
     [clojure.core.async :as async  :refer (<! <!! >! >!! put! chan go go-loop)]
     [taoensso.timbre    :as timbre :refer (tracef debugf infof warnf errorf)]
     [taoensso.sente     :as sente]
     [org.httpkit.server :as http-kit]
     [taoensso.sente.server-adapters.http-kit :refer (sente-web-server-adapter)]
     [taoensso.sente.packers.transit :as sente-transit]
     [hiccup.page :refer [include-css]]))

;;;; Logging config
;; (sente/set-logging-level! :trace) ; Uncomment for more logging

;;; http-kit
(defn start-web-server!* [ring-handler port]
    (println "Starting http-kit...")
    (let [http-kit-stop-fn (http-kit/run-server ring-handler {:port port})]
      {:server  nil ; http-kit doesn't expose this
       :port    (:local-port (meta http-kit-stop-fn))
       :stop-fn (fn [] (http-kit-stop-fn :timeout 100))}))

;;;; Packer (client<->server serializtion format) config

(def packer (sente-transit/get-flexi-packer :edn)) ; Experimental, needs Transit dep
;; (def packer :edn) ; Default packer (no need for Transit dep)

;;;; Server-side sente setup

(let [{:keys [ch-recv send-fn ajax-post-fn ajax-get-or-ws-handshake-fn
              connected-uids]}
      (sente/make-channel-socket! sente-web-server-adapter {:packer packer})]
  (def ring-ajax-post                ajax-post-fn)
  (def ring-ajax-get-or-ws-handshake ajax-get-or-ws-handshake-fn)
  (def ch-chsk                       ch-recv) ; ChannelSocket's receive channel
  (def chsk-send!                    send-fn) ; ChannelSocket's send API fn
  (def connected-uids                connected-uids)) ; Watchable, read-only atom
  

(defn landing-pg-handler [req]
  (hiccup/html
   [:h1 "Sente reference example"]
   [:p "An Ajax/WebSocket connection has been configured (random)."]
   [:hr]
   [:p [:strong "Step 1: "] "Open browser's JavaScript console."]
   [:p [:strong "Step 2: "] "Try: "
    [:button#btn1 {:type "button"} "chsk-send! (w/o reply)"]
    [:button#btn2 {:type "button"} "chsk-send! (with reply)"]]
   ;;
   [:p [:strong "Step 3: "] "See browser's console + nREPL's std-out."]
   ;;
   [:hr]
   [:h2 "Login with a user-id"]
   [:p  "The server can use this id to send events to *you* specifically."]
   [:p [:input#input-login {:type :text :placeholder "User-id"}]
    [:button#btn-login {:type "button"} "Secure login!"]]
   [:script {:src "main.js"}])) ; Include our cljs target

(defn index-page-handler [req]
  (hiccup/html
    (include-css "//netdna.bootstrapcdn.com/twitter-bootstrap/2.3.1/css/bootstrap-combined.min.css")    
    [:h1.jutsu-header "Jutsu 術"
     [:style "body {text-align: center; background-color: #ebd5f2;}"]]
    [:script {:src "main.js"}]))
   

(defn login!
  "Here's where you'll add your server-side login/auth procedure (Friend, etc.).
  In our simplified example we'll just always successfully authenticate the user
  with whatever user-id they provided in the auth request."
  [ring-request]
  (let [{:keys [session params]} ring-request
        {:keys [user-id]} params]
    (debugf "Login request: %s" params)
    {:status 200 :session (assoc session :uid user-id)}))

(defroutes my-routes
  (GET  "/"      req (index-page-handler req))
  (GET  "/chsk"  req (ring-ajax-get-or-ws-handshake req))
  (POST "/chsk"  req (ring-ajax-post                req))
  (POST "/login" req (login! req))
  (route/not-found "<h1>Page not found</h1>"))

(def my-ring-handler
  (let [ring-defaults-config
        (-> site-defaults
            (assoc-in [:static :resources] "/")
            (assoc-in [:security :anti-forgery] {:read-token (fn [req] (-> req :params :csrf-token))}))]
    (ring.middleware.defaults/wrap-defaults my-routes ring-defaults-config)))

;;;; Routing handlers

(defmulti event-msg-handler :id) ; Dispatch on event-id
   ;; Wrap for logging, catching, etc.:
(defn event-msg-handler* [{:as ev-msg :keys [id ?data event]}]
  ;(debugf "Event: %s" event)
  (event-msg-handler ev-msg))

(do ; Server-side methods
    (defmethod event-msg-handler :default ; Fallback
      [{:as ev-msg :keys [event id ?data ring-req ?reply-fn send-fn]}]
      (let [session (:session ring-req)
            uid     (:uid     session)]
        ;(debugf "Unhandled event: %s" event)
        (when ?reply-fn
          (?reply-fn {:umatched-event-as-echoed-from-from-server event})))))

    ;; Add your (defmethod event-msg-handler <event-id> [ev-msg] <body>)s here...

; Note that this'll be fast+reliable even over Ajax!:
(defn test-fast-server>user-pushes []
  (doseq [uid (:any @connected-uids)]
    (doseq [i (range 100)]
      (chsk-send! uid [:fast-push/is-fast (str "hello " i "!!")]))))

;;Data has to be vector should throw error if isnt
(defn graph
  ([data] (graph data {}))
  ([data layout]
   (doseq [uid (:any @connected-uids)]
    (chsk-send! uid [:graph/graph {:data data
                                   :layout layout}]))))

(defn test-graph []
  (graph [{:x [1 2 3 4]
           :y [1 2 3 4]
           :mode "markers"
           :type "scatter"}]))

;;;; Init

(defonce web-server_ (atom nil)) ; {:server _ :port _ :stop-fn (fn [])}
(defn stop-web-server! [] (when-let [m @web-server_] ((:stop-fn m))))
(defn start-web-server! [& [port]]
  (stop-web-server!)
  (let [{:keys [stop-fn port] :as server-map}
        (start-web-server!* (var my-ring-handler)
                            (or port 0)) ; 0 => auto (any available) port
                            
        uri (format "http://localhost:%s/" port)]
    ;(debugf "Web server is running at `%s`" uri)
    (try
      (.browse (java.awt.Desktop/getDesktop) (java.net.URI. uri))
      (catch java.awt.HeadlessException _))
    (reset! web-server_ server-map)))

(defonce router_ (atom nil))

(defn  stop-router! [] (when-let [stop-f @router_] (stop-f)))
(defn start-router! []
  (stop-router!)
  (reset! router_ (sente/start-chsk-router! ch-chsk event-msg-handler*)))

(defn start! []
  (start-router!)
  (start-web-server!))
