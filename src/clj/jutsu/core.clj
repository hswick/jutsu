(ns jutsu.core
  (:require [jutsu.web :as web]))

;;Initializes the jutsu server and opens client in browser
(defn start-jutsu! [] (web/start!))

;;Data has to be vector should throw error if isnt
(defn graph
  "Sends graph data to client to be visualized"
  ([data] (graph data {}))
  ([data layout]
   (doseq [uid (:any @web/connected-uids)]
    (web/chsk-send! uid [:graph/graph {:data data
                                       :layout layout}]))))

(defn test-graph []
  (graph [{:x [1 2 3 4]
           :y [1 2 3 4]
           :mode "markers"
           :type "scatter"}]))
