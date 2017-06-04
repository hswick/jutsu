(ns jutsu.core
  (:require [jutsu.web :as web]
            [jutsu.data :refer :all])
  (:import [org.nd4j.linalg.dimensionalityreduction PCA]))

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

(defn test-graph []
  (graph! (str "graph-" @graph-count)
    [{:x [1 2 3 4]
      :y [1 2 3 4]
      :mode "markers"
      :type "scatter"}]))

(defn test-graph-2 [name]
  (graph! name
    [{:x [1 2 3 4]
      :y [1 2 3 4]
      :mode "markers"
      :type "scatter"}]))

(defn update-graph!
  "Sends data to update graph with specified id"
  [id data]
  (doseq [uid (:any @web/connected-uids)]
    (web/chsk-send! uid [:graph/update
                         {:id id
                          :data data}])))

(defn test-update-graph! []
  (update-graph!  
    "foo"
    {:data {:y [[4]] :x [[5]]} 
     :traces [0]}))

(defn test-covar []
   (let [data (->> (csv->clj "iris.csv")
                  rest
                  (map #(take 4 %))
                  (map strings->floats)
                  clj->nd4j
                  ((fn [ndarray] (pca ndarray 2))))]
     data))

(defn test-iris! []
  (let [dataset (-> (csv->clj "iris.csv")
                    ((fn [data]
                       {:coords (->> data
                                     rest
                                     (map #(take 4 %))
                                     (map strings->floats)
                                     clj->nd4j
                                     normalize-zero
                                     ((fn [ndarray] (pca ndarray 2))))
                        :labels (map #(nth % 4) data)})))
        split-labels-and-ids (->> (:labels dataset)
                                  (map-indexed (fn [id label] [label id]))
                                  (partition-by first)
                                  rest)
        graph-data (map
                    (fn [species-with-ids]
                      {:x (map (fn [[label id]]  
                                 (get-double-from-row (:coords dataset) (dec id) 0)) 
                            species-with-ids)
                       :y (map (fn [[label id]] 
                                 (get-double-from-row (:coords dataset) (dec id) 1)) 
                            species-with-ids)
                       ;:text (map first species-with-ids)
                       :name (ffirst species-with-ids)
                       :mode "markers"
                       :type "scatter"})
                    split-labels-and-ids)]
    (graph! "test-iris" graph-data)))

(defn test-etl []
  (csv->nd4j "iris.csv"))

(defn dataset! []
  (doseq [uid (:any @web/connected-uids)]
    (web/chsk-send! uid [:dataset/dataset 
                         {:data [[1 2 3]
                                 [4 5 6]
                                 [6 7 8]
                                 [9 10 11]]}]))) 
