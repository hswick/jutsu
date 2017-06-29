(set-env!
  :resource-paths   #{"src/clj" "src/cljs"}
  :dependencies '[[adzerk/boot-cljs      "2.0.0" :scope "test"]
                  [adzerk/boot-reload    "0.5.1"      :scope "test"]
                  [nightlight "1.6.5" :scope "test"]
                  [samestep/boot-refresh "0.1.0" :scope "test"]
                  [adzerk/boot-test "1.2.0" :scope "test"] 
                  [org.clojure/core.async "0.1.346.0-17112a-alpha"]
                  [com.taoensso/sente        "1.5.0-RC2"] ; <--- Sente
                  [http-kit                  "2.1.19"]
                  [ring                      "1.4.0-RC1"]
                  [ring/ring-defaults        "0.1.5"] ; Includes `ring-anti-forgery`, etc.
                  [compojure                 "1.3.4"] ; Or routing lib of your choice
                  [hiccup                    "1.0.5"] ; Optional, just for HTML
                  [com.cognitect/transit-clj  "0.8.275"];;; Transit deps optional; may be used to aid perf. of larger data payloads
                  [com.cognitect/transit-cljs "0.8.220"]
                  [hiccups "0.3.0"]
                  [cljsjs/plotly "1.25.0-0"]
                  [org.clojure/core.match "0.3.0-alpha4"]
                  [adzerk/bootlaces "0.1.13" :scope "test"]])


(require
 '[adzerk.boot-cljs      :refer [cljs]]
 '[adzerk.boot-reload    :refer [reload]]
 '[nightlight.boot :refer [nightlight]]
 '[samestep.boot-refresh :refer [refresh]]
 '[adzerk.boot-test :refer :all]
 'jutsu.web
 '[adzerk.bootlaces :refer :all])

(def +version+ "0.0.1")
(bootlaces! +version+ :dont-modify-paths? true)

(task-options!
  aot {:namespace '#{jutsu.core}}
  jar {:main 'jutsu.core
       :file "jutsu.jar"
       :manifest {"Description" "Data visualization tool"}}
  pom {:version +version+
       :project 'hswick/jutsu})

(try
 (require 'jutsu.core)
 (catch Exception e (.getMessage e)))

(deftask testing [] (merge-env! :source-paths #{"test"}) identity)

(deftask night 
 "Start nightlight editor on localhost:4000"
 []
 (comp
  (wait)
  (nightlight :port 4000)))

(deftask dev
  "Run a restartable system for development"
  []
  (comp
   (watch)
   (cljs)
   (reload)
   (refresh)
   (repl
    :server true
    :init-ns 'jutsu.core)))

(deftask repl-client
 []
 (comp
  (repl 
    :client true)))

(deftask test-jutsu
  []
  (set-env! :source-paths #{"test"})
  (comp
    (watch)
    (cljs)
    (test)))
