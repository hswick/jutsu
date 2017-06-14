(ns jutsu.test
  (:require [jutsu.core :refer :all]
            [jutsu.web :as web]))

(initialize-client-events
  (fn [] (web/chsk-send! [:chsk/recv {:had-a-callback? "nope"}])))

(jutsu-start)


