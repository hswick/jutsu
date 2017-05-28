(ns jutsu.core
  (:require [jutsu.web :as web]))

;;Initializes the jutsu server and opens client in browser
(defn start-jutsu! [] (web/start!))

(def graph-count (atom 0))

;;Data has to be vector should throw error if isnt
(defn graph!
  "Sends graph data to client to be visualized"
  ([meta-data data] (graph! meta-data data {}))
  ([meta-data data layout]
   (doseq [uid (:any @web/connected-uids)]
    (web/chsk-send! uid [:graph/graph 
                         {:data data
                          :layout layout
                          :meta-data 
                          (merge meta-data
                            (if (:id meta-data) {}
                              {:id @graph-count}))}]))
   (swap! graph-count inc)))

(defn test-graph []
  (graph! {:id (str "graph-" @graph-count)}
    [{:x [1 2 3 4]
      :y [1 2 3 4]
      :mode "markers"
      :type "scatter"}]))

(defn test-graph-2 [name]
  (graph! {:id name}
    [{:x [1 2 3 4]
      :y [1 2 3 4]
      :mode "markers"
      :type "scatter"}]))

(defn update-graph!
  "Sends data to update graph with specified id"
  [id data]
  (doseq [uid (:any @web/connected-uids)]
    (web/chsk-send! uid [:graph/update
                         {:meta-data {:id id}
                          :data data}])))

(defn test-update-graph! []
  (update-graph! "foo" {:data {:y [[4]] :x [[5]] :traces [0]}}))
