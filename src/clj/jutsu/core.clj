(ns jutsu.core
  (:require [jutsu.web :as web]))

;;Initializes the jutsu server
;;Initializes the jutsu server router for sente
;;opens client in browser
(defn start-jutsu!
  "
  Initialize jutsu server.
  [] will choose a random port number for you, and will try to display jutsu client
  in default web browser.
  [port display] port is an integer value to host jutsu on. display is a boolean value
  to open up jutsu in browser or not.
  "
  ([] (web/start!))
  ([port display] (web/start! port display))
  ([port display header] (web/start! port display header)))

(def graph-count (atom 0))

;;Data has to be vector should throw error if isnt
;;Should change meta-data to id
(defn graph!
  "Sends graph data to client to be visualized.
  [id data] id is a string/numerical identifier for your graph.
  data is the edn data you want to be plotted. This data must follow plotly.js format.
  [id data layout] optional layout argument for global plotly config options."
  ([id data] (graph! id data {}))
  ([id data layout] (graph! id data layout {}))
  ([id data layout options]
   (doseq [uid (:any @web/connected-uids)]
    (web/chsk-send! uid [:graph/graph 
                         {:data data
                          :layout layout
                          :options options
                          :id id}]))
   (swap! graph-count inc)))

;;"Sends data to update graph with specified id"
(defn update-graph!
  "
  Sends data to client to update graph specified by id.
  [id data] id is a string/numerical identifier for the graph.
  data is an edn data that must be formatted according to plotly conventions.
  "
  [id data]
  (doseq [uid (:any @web/connected-uids)]
    (web/chsk-send! uid [:graph/update
                         {:id id
                          :data data}])))

(defn dataset!
  "
  Plot a table of clojure data to be viewed in a simple grid.
  [id data] id is a string/numerical identifier for the table.
  data is a tabular in shape dataset.
  "
  [id data]
  (doseq [uid (:any @web/connected-uids)]
    (web/chsk-send! uid [:dataset/dataset {:id id :data data}])))
