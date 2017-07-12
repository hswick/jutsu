(ns jutsu.core
  (:require [jutsu.web :as web]))

;;Initializes the jutsu server
;;Initializes the jutsu server router for sente
;;opens client in browser
(defn start-jutsu! 
  ([] (web/start!))
  ([color] (web/start! color)))

(def graph-count (atom 0))

;;Data has to be vector should throw error if isnt
;;Should change meta-data to id
(defn graph!
  "Sends graph data to client to be visualized"
  ([id data] (graph! id data {}))
  ([id data layout]
   (doseq [uid (:any @web/connected-uids)]
    (web/chsk-send! uid [:graph/graph 
                         {:data data
                          :layout layout
                          :id id}]))
   (swap! graph-count inc)))

;;"Sends data to update graph with specified id"
(defn update-graph!
  [id data]
  (doseq [uid (:any @web/connected-uids)]
    (web/chsk-send! uid [:graph/update
                         {:id id
                          :data data}])))

(defn dataset! [id data]
  (doseq [uid (:any @web/connected-uids)]
    (web/chsk-send! uid [:dataset/dataset {:id id :data data}])))
