(ns jutsu.test
  (:require [jutsu.core :as j]
            [jutsu.web :as w]
            [clojure.core.match :refer [match]]))

(defn test-graph! []
  (j/graph! (str "graph-" @j/graph-count)
    [{:x [1 2 3 4]
      :y [1 2 3 4]
      :mode "markers"
      :type "scatter"}]))

(defn test-graph-2! [name]
  (j/graph! name
    [{:x [1 2 3 4]
      :y [1 2 3 4]
      :mode "markers"
      :type "scatter"}]))

(defn test-update-graph! []
  (j/update-graph!  
    "foo"
    {:data {:y [[4]] :x [[5]]} 
     :traces [0]}))

(defn test-dataset! []
  (j/dataset! @j/graph-count
    [[1 2 3 4]
     [1 2 3 4]
     [1 2 3 4]
     [1 2 3 4]]))

(defn test-line-graph []
  (j/graph! "Line Chart Test"
    [{:x [1 2 3 5]
      :y [6 7 8 9]
      :type "scatter"}]))

(defn test-bar-chart []
  (j/graph! "Bar Chart Test"
    [{:x ["foo" "bar" "foobar"]
      :y [20 30 40]
      :type "bar"}]))

(defn test-pie-chart []
  (j/graph! "Pie Chart Test"
    [{:values [19 26 55]
      :labels ["Residential" "Non Residential" "Utility"]
      :type "pie"}]))

(defn test-3D-graph []
  (j/graph! "3D WebGL Test"
    [{:x (take 100 (repeatedly #(rand)))
      :y (take 100 (repeatedly #(rand)))
      :z (take 100 (repeatedly #(rand)))
      :type "scatter3d"
      :mode "markers"}]
    {:width 600
     :height 600}))

(j/start-jutsu!)
(Thread/sleep 3000)
(w/test-fast-server>user-pushes)

(defn run-tests []
  (test-graph!)
  (test-graph-2! "foo")
  (test-update-graph!)
  (test-dataset!)
  (test-line-graph)
  (test-bar-chart)
  (test-pie-chart)
  (test-3D-graph)
  (w/test-fast-server>user-pushes))

(run-tests)
