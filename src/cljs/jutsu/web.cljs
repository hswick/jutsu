(ns jutsu.web
 (:require
   [clojure.string  :as str]
   [cljs.core.async :as async  :refer (<! >! put! chan)]
   [taoensso.encore :as enc    :refer (tracef debugf infof warnf errorf)]
   [taoensso.sente  :as sente  :refer (cb-success?)]
   ;; Optional, for Transit encoding:
   [taoensso.sente.packers.transit :as sente-transit]
   [hiccups.runtime :as hiccupsrt]
   [cljsjs.plotly])
 (:require-macros
   [cljs.core.async.macros :as asyncm :refer (go go-loop)]
   [hiccups.core :as hiccups :refer [html]]))

;; (sente/set-logging-level! :trace) ; Uncomment for more logging
;;;; Packer (client<->server serializtion format) config
(def packer (sente-transit/get-flexi-packer :edn))
;; (def packer :edn) ; Default packer (no need for Transit dep)

(debugf "ClojureScript appears to have loaded correctly.")
(let [rand-chsk-type (if (>= (rand) 0.5) :ajax :auto)
        
      {:keys [chsk ch-recv send-fn state]}
      (sente/make-channel-socket! "/chsk" ; Note the same URL as before
                                  {:type   rand-chsk-type
                                   :packer packer})]
    (debugf "Randomly selected chsk type: %s" rand-chsk-type)
    (def chsk       chsk)
    (def ch-chsk    ch-recv) ; ChannelSocket's receive channel
    (def chsk-send! send-fn) ; ChannelSocket's send API fn
    (def chsk-state state))  ; Watchable, read-only atom
    

;;;; Routing handlers

(defmulti event-msg-handler :id) ; Dispatch on event-id
    ;; Wrap for logging, catching, etc.:
(defn     event-msg-handler* [{:as ev-msg :keys [id ?data event]}]
  (debugf "Event: %s" event)
  (event-msg-handler ev-msg))

(defn init-client-side-events! [event-handler on-init] ; Client-side methods
  (defmethod event-msg-handler :default ; Fallback
    [{:as ev-msg :keys [event]}]
    (debugf "Unhandled event: %s" event))
      
  (defmethod event-msg-handler :chsk/state
    [{:as ev-msg :keys [?data]}]
    (if (= ?data {:first-open? true})
      (debugf "Channel socket successfully established!")
      (debugf "Channel socket state change: %s" ?data)))
      
  (defmethod event-msg-handler :chsk/recv
    [{:as ev-msg :keys [?data id]}]
    (debugf "Push event from server: %s" ?data)
    (event-handler ?data))
  
  (defmethod event-msg-handler :chsk/handshake
    [{:as ev-msg :keys [?data]}]
    (let [[?uid ?csrf-token ?handshake-data] ?data]
      (debugf "Handshake: %s" ?data)
      (on-init))))
      
      
      ;; Add your (defmethod handle-event-msg! <event-id> [ev-msg] <body>)s here...
(def router_ (atom nil))
(defn  stop-router! [] (when-let [stop-f @router_] (stop-f)))
(defn start-router! [event-handler]
  (stop-router!)
  (reset! router_ (sente/start-chsk-router! ch-chsk event-msg-handler*)))

(defn start! []
  (start-router!))
