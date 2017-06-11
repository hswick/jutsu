(ns jutsu.test
  (:require [jutsu.core :as j]
            [jutsu.web :as w]))

(defn test-graph []
  (j/graph! (str "graph-" @j/graph-count)
    [{:x [1 2 3 4]
      :y [1 2 3 4]
      :mode "markers"
      :type "scatter"}]))

(defn run-tests []
  (j/start-server!)
  (Thread/sleep 3000)
  (test-graph)
  (j/test-graph-2 "foo")
  (w/test-fast-server>user-pushes))
