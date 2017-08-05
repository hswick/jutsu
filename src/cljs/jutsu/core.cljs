(ns jutsu.core
  (:require
            [clojure.string  :as string]
            [hiccups.runtime :as hiccupsrt]
            [cljsjs.plotly]
            [jutsu.web :as web]
            [cljs.core.match :refer-macros [match]])
  (:require-macros
                   [hiccups.core :as hiccups :refer [html]]))

(defn append-to-body! [el]
  (.insertAdjacentHTML (.-body js/document) "beforeEnd" el))

(defn draw-plot!
  [id data layout]
  (when (not (.getElementById js/document (str "graph-" id)))
    (append-to-body! (html [:div.container                         
                            [:style (str ".container {text-align: left;}")]
                            [:h1 (str id)]
                            [(keyword (str "div#graph-" id))]])))
  (js/Plotly.newPlot
    (str "graph-" id)
    (clj->js data)
    (clj->js layout))
  (.scrollIntoView (.getElementById js/document (str "graph-" id))))

(defn extend-traces!
  [id data]
  (.extendTraces js/Plotly
    (str "graph-" id)
    (clj->js (:data data))
    (clj->js (:traces data))))

(defn draw-dataset!
  [id data]
  (append-to-body! (html [:div.container
                          {:style "overflow: scroll; height: 25%;"}
                          [:h1 (str id)]
                          [(keyword (str "div#dataset-" id))
                           [:table.table-striped
                            {:style "width: 100%;"}
                            (for [data-row data]
                              [:tr
                               (map (fn [item] [:td (str item)]) data-row)])]]]))
  (.scrollIntoView (.getElementById js/document (str "dataset-" id))))

(defn sanitize-id [id]
  (clojure.string/replace id #" " #"-"))
                                  
(defn jutsu-client-event-handler [?data]
  (match (first ?data)
    :graph/graph
    (draw-plot! 
      (sanitize-id (:id (second ?data)))
      (:data (second ?data))
      (:layout (second ?data)))
    :graph/update
    (extend-traces! 
      (sanitize-id (:id (second ?data)))
      (:data (second ?data)))
    :dataset/dataset
    (draw-dataset!
      (sanitize-id (:id (second ?data)))
      (:data (second ?data)))
    :else
    (.log js/console (str "Unhandled event " ?data))))

(defn initialize-client-events []
  (web/init-client-side-events! 
    jutsu-client-event-handler))
  
(defn jutsu-start []
  (initialize-client-events)
  (web/start!))

(jutsu-start)
