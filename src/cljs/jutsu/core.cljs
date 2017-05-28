(ns jutsu.core
 (:require
   [clojure.string  :as str]
   [hiccups.runtime :as hiccupsrt]
   [cljsjs.plotly]
   [jutsu.web :as web])
 (:require-macros
   [hiccups.core :as hiccups :refer [html]]))

;;Will probably replace this with reagent
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
    (when (= :graph/graph (first ?data)
            (draw-plot! 
             (:data (second ?data))
             (:layout (second ?data)))))))

(web/start!)
