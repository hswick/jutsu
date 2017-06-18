(ns jutsu.core
  (:require [jutsu.web :as web]))

;;Initializes the jutsu server
;;Initializes the jutsu server router for sente
;;opens client in browser
;;TODO: options map for server (turn off auto open client)
(defn start-server! [] (web/start!))

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

(defn dataset! [data]
  (doseq [uid (:any @web/connected-uids)]
    (web/chsk-send! uid [:dataset/dataset {:data data}])))

;;Assumes you have a map with a key called :data
(defn scatter-graph! [split-dataset]
  (graph! "test-iris" (map
                        (fn [dataset]
                          {:x (map #(jutsu-nth % 0) (:data dataset))
                           :y (map #(jutsu-nth % 1) (:data dataset)) 
                           :name (first (:labels dataset))
                           :mode "markers"
                           :type "scatter"}
                          split-dataset))))


