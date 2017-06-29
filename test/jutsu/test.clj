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
  (j/dataset! [[1 2 3 4]
               [1 2 3 4]
               [1 2 3 4]
               [1 2 3 4]]))

(j/start-jutsu!)
(Thread/sleep 3000)
(w/test-fast-server>user-pushes)

(defn run-tests []
  (test-graph!)
  (test-graph-2! "foo")
  (test-update-graph!)
  (test-dataset!)
  (w/test-fast-server>user-pushes))

(run-tests)
