# Jutsu 術

### Currently in a stage of hammock driven development

This app is essentially plotly.js api connected to a repl server via sente.

## Usage

```clojure
(:require [jutsu.core :as jutsu])

;;Start Jutsu server and opens client in browser
(jutsu/start-server!)

;;Adds a graph to the Jutsu client
(jutsu/graph!
  {:id "foo"}
  {:x [1 2 3 4]
   :y [1 2 3 4]
   :mode "markers"
   :type "scatter"})
   
;;To do realtime updates of a graph
(jutsu/update-graph! "foo" {:data {:y [[4]] :x [[5]] :traces [0]}})
```

## Dev

boot night to edit

boot dev to run

## More
[[https://github.com/danielsz/sente-boot][What jutsu started as]]

Useful cljs clj app configuring
https://github.com/magomimmo/modern-cljs/blob/master/doc/second-edition/tutorial-09.md

Would like to create the UI as FSM demonstrated here
http://blog.cognitect.com/blog/2017/5/22/restate-your-ui-using-state-machines-to-simplify-user-interface-development