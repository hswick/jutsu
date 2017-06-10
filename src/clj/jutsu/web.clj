(ns jutsu.web
  (:require
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
;(sente/set-logging-level! :trace) ; Uncomment for more logging

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

(def jutsu-socket-routes
  (comp/routes
        (GET  "/chsk"  req (ring-ajax-get-or-ws-handshake req));;need to decouple these from index
        (POST "/chsk"  req (ring-ajax-post                req))
        (route/not-found "<h1>Page not found</h1>")))

;;Default index page for jutsu
(defn index-page-handler [req]
  (hiccup/html
    (include-css "//netdna.bootstrapcdn.com/twitter-bootstrap/2.3.1/css/bootstrap-combined.min.css")    
    [:h1.jutsu-header "jutsu 術"
     [:style "body {text-align: center; background-color: #ebd5f2;}"]]
    [:script {:src "main.js"}]))

;;Order matters in how you compose routes
(defn jutsu-routes
  ([] (jutsu-routes (GET  "/"      req (index-page-handler req))))
  ([index-route] (comp/routes index-route jutsu-socket-routes)))
      
      ;;should add this as an option)))

(defn jutsu-ring-handler [jutsu-routes]
  (let [ring-defaults-config
        (-> site-defaults
            (assoc-in [:static :resources] "/")
            (assoc-in [:security :anti-forgery] {:read-token (fn [req] (-> req :params :csrf-token))}))]
    (ring.middleware.defaults/wrap-defaults jutsu-routes ring-defaults-config)))

;;;; Routing handlers

(defmulti event-msg-handler :id) ; Dispatch on event-id
   ;; Wrap for logging, catching, etc.:
(defn event-msg-handler* [{:as ev-msg :keys [id ?data event]}]
  (debugf "Event: %s" id)
  (event-msg-handler ev-msg))

(defn init-server-event-handler! [event-handler]
  (defmethod event-msg-handler :default ; Fallback
    [{:as ev-msg :keys [event id ?data ring-req ?reply-fn send-fn]}]
    (let [session (:session ring-req)
          uid     (:uid     session)]
      (debugf "Unhandled event: %s" event)
      ;(debugf "Unhandled id: " id)
      (when ?reply-fn
        (?reply-fn {:umatched-event-as-echoed-from-from-server event}))))
  
  (defmethod event-msg-handler :chsk/recv
    [{:as ev-msg :keys [?data id]}]
    (debugf "Push event from client: %s" ?data)
    (event-handler ?data)))

; Note that this'll be fast+reliable even over Ajax!:
(defn test-fast-server>user-pushes []
  (doseq [uid (:any @connected-uids)]
    (doseq [i (range 100)]
      (chsk-send! uid [:fast-push/is-fast (str "hello " i "!!")]))))

;;;; Init

;;; http-kit
(defn jutsu-server-map [ring-handler port]
    (println "Starting http-kit...")
    (let [http-kit-stop-fn (http-kit/run-server ring-handler {:port port})]
      {:server  nil ; http-kit doesn't expose this
       :port    (:local-port (meta http-kit-stop-fn))
       :stop-fn (fn [] (http-kit-stop-fn :timeout 100))}))

(defonce web-server_ (atom nil)) ; {:server _ :port _ :stop-fn (fn [])}
(defn stop-web-server! [] (when-let [m @web-server_] ((:stop-fn m))))

(defn start-web-server!
  ([& [ring-handler port]]
   (stop-web-server!)
   (let [{:keys [stop-fn port] :as server-map}
         (jutsu-server-map ring-handler
                          (or port 0)) ; 0 => auto (any available) port                        
         uri (format "http://localhost:%s/" port)]
    ;(debugf "Web server is running at `%s`" uri)
     (reset! web-server_ server-map)
     uri)))

(defn display-uri! [uri]
  (try
    (.browse (java.awt.Desktop/getDesktop) (java.net.URI. uri))
    (catch java.awt.HeadlessException _))
  uri)
  
;;Web socket router
(defonce router_ (atom nil))
(defn  stop-router! [] (when-let [stop-f @router_] (stop-f)))
(defn start-router! []
  (stop-router!)
  (reset! router_ (sente/start-chsk-router! ch-chsk event-msg-handler*)))

;;Default way to start up jutsu server
(defn start! []
  (init-server-event-handler! (fn [?data] (debugf (str ?data))))
  (start-router!);;Have to call this to get websocket working
  (-> (jutsu-routes)
      jutsu-ring-handler
      start-web-server!
      display-uri!))
