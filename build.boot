(set-env!
 :source-paths   #{"src/clj" "src/cljs"}
  :dependencies '[[adzerk/boot-cljs      "0.0-3269-2" :scope "test"]
                  [adzerk/boot-reload    "0.5.1"      :scope "test"]
                  [nightlight "1.6.3" :scope "test"]
                  [pandeiro/boot-http "0.8.3"]
                  [org.clojure/core.async "0.1.346.0-17112a-alpha"]
                  [com.taoensso/sente        "1.5.0-RC2"] ; <--- Sente
                  [http-kit                  "2.1.19"]
                  [ring                      "1.4.0-RC1"]
                  [ring/ring-defaults        "0.1.5"] ; Includes `ring-anti-forgery`, etc.
                  [compojure                 "1.3.4"] ; Or routing lib of your choice
                  [hiccup                    "1.0.5"] ; Optional, just for HTML
                  [com.cognitect/transit-clj  "0.8.275"];;; Transit deps optional; may be used to aid perf. of larger data payloads
                  [com.cognitect/transit-cljs "0.8.220"]])

(require
 '[adzerk.boot-cljs      :refer [cljs]]
 '[adzerk.boot-reload    :refer [reload]]
 '[nightlight.boot :refer [nightlight]]
 '[pandeiro.boot-http :refer [serve]])

(try
 (require 'example.my-app)
 (catch Exception e (.getMessage e)))

(deftask night 
 "Start nightlight editor on localhost:4000"
 []
 (comp
  (wait)
  (nightlight :port 4000)))

(deftask dev
  "Run a restartable system in the Repl"
  []
  (comp
   (watch)
   (cljs :source-map true)
   (reload)
   (repl :init-ns 'example.my-app)))
