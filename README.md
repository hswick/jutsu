## Currently in a stage of hammock driven development

# jutsu 術

![alt text](https://sites.google.com/site/narutonarutoshippudenmanga/_/rsrc/1338580006294/handsigns/Handsigns.png)

jutsu translation: technique, way

Do you want fast data visualization that looks professional without a lot of hassle? Then you have come to the right place!

This tool is meant for purposes of all things data viz! The current api is functionally a light wrapper around [plotly.js](https://plot.ly/javascript/).
So you can follow their conventions for plotting while getting to use EDN instead of JSON. 

Data visualization, exploration, science, and analysis.

Note this tools functionality is primarily side effects.

The power of jutsu is that it works by sending data to a client just like a website. While any computation is done on the server.
This functionally decouples the visualizations from the computations. Making the user more capable of dealing with a flexible amount of situtations.

Future release will support cloud functionality which will be very few api changes.

## Getting Started

If you are new to Clojure then follow these instructions, otherwise skip to usage.

Make sure you have java installed

`java -version` if you don't, then install a java SDK such as OpenJDK. 

Then install [boot](https://github.com/boot-clj/boot).
Boot is a build tool for clojure which allows you to run tasks from the command line.

To make sure everything is working run `boot repl` in your command line.

In order to use jutsu you will need to create a build.boot file to add jutsu as a dependency. 
You can follow the instructions on boot's [homepage](https://github.com/boot-clj/boot) to guide you.

## Usage

Add jutsu to your dependencies

```clojure
[hswick/jutsu "0.0.1"]
```

Then include jutsu into your current namespace.

If working in a repl:

```clojure

(require '[jutsu.core :as j])

```
Or if working in a source file:

```clojure
(:require [jutsu.core :as j])
```

Then start using jutsu! 
```clojure
;;This will start up a httpkit server and open the jutsu client in your default browser.
(j/start-jutsu!)

;;If you are running this in a script as opposed to the repl 
;;delay for a bit to let the websocket connect.
;;(Thread/sleep 3000)

;;Adds a graph to the jutsu client
(j/graph!
  "foo"
  [{:x [1 2 3 4]
    :y [1 2 3 4]
    :mode "markers"
    :type "scatter"}])
   
;;To do realtime updates of a graph
(j/update-graph!  
  "foo"
  {:data {:y [[4]] :x [[5]]} 
   :traces [0]}))
   
;;You can even view matrix like datasets
(j/dataset! [[0 4 2 0] [1 2 3 4]])
```

## Dev

Run all three commands in separate tabs for optimal dev experience.

`boot night` to edit

`boot dev` to run

`boot repl-client` to connect to repl server started with `boot dev`

Pull requests are very welcome!

## Testing

`boot test-jutsu` to run tests (will reload on resource file changes)

This has the unfortunate side effect of constantly reopening a tab in your browser and can be annoying at times.
Would love advice on how to test this better. 

## Web Socket
Sente receives event via chsk/receive and jutsu uses pattern matching to route the events appropriately.

Dynamically adding more event routes is a possibility, however the coupling between clj(s) makes testing very difficult.
Server side router currently only sends events to the client-side-router. However, functionality for receiving events is planned.


## More
[What jutsu started as](https://github.com/danielsz/sente-boot)

[plotly cheat sheet](https://images.plot.ly/plotly-documentation/images/plotly_js_cheat_sheet.pdf)

[plotly full reference](https://plot.ly/javascript/reference/)

## LICENSE
Copyright 2017 Harley Swick

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.