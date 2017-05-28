(ns jutsu.core
 (:require
   [clojure.string  :as str]
   [hiccups.runtime :as hiccupsrt]
   [cljsjs.plotly]
   [jutsu.web :as web]
   [cljs.core.match :refer-macros [match]])
 (:require-macros
   [hiccups.core :as hiccups :refer [html]]))

(defn append-to-body! [el]
  (.insertAdjacentHTML (.-body js/document) "beforeEnd" el))

(def graph-count (atom 0))

(defn draw-plot!
  [data layout]
  (append-to-body! (html [:div.container                          
                          [:style (str ".container {text-align: left;}")]
                          [:h1 (str "graph " @graph-count)]
                          [(keyword (str "div#inner-graph-container-" @graph-count))]]))
  (js/Plotly.newPlot
    (str "inner-graph-container-" @graph-count)
    (clj->js data)
    (clj->js layout))
  (swap! graph-count inc))

(web/init-client-side-events!
  (fn
    [?data]
    (match (first ?data)
      :graph/graph 
      (draw-plot! 
             (:data (second ?data))
             (:layout (second ?data))))))

(web/start!)
