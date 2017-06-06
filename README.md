# Jutsu 術

## Currently in a stage of hammock driven development

Data Science Framework built with clojure design principles in mind.

Easy to use abstractions that are decomposable for simplicity.

Datasets are maps where the keys are columns and the elements are rows
-Currently the concept of dataset assumes that rows of all columns are equal

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
(update-graph!  
  {:id "foo"}
  {:data {:y [[4]] :x [[5]]} 
   :traces [0]}))
```

##

## Client Side Web Socket
Sente receives event via chsk/receive, an event handler can be passed to deal with all the events.
Currently using core.match to deal with events:
:graph/graph
:graph/update-graph
:datset/dataset

## Server Side Web Socket
Server side router sends and receives events to the client-side-router. 
Jutsu works by sending data to the client side to be visualized.
All computations are done on the server

## Dev

Run all three commands in separate tabs for optimal dev experience.

`boot night` to edit

`boot dev` to run

`boot repl-client` to connect to repl server started with `boot dev`.

## More
[[https://github.com/danielsz/sente-boot][What jutsu started as]]

Would like to create the UI as FSM demonstrated here
http://blog.cognitect.com/blog/2017/5/22/restate-your-ui-using-state-machines-to-simplify-user-interface-development