(ns app.main
  (:require [app.lib :as lib]
            [app.most-counter :refer [make-counter-3!]]
            [app.calendar]))

(def a 1)

(defonce b 2)

(make-counter-3!)

(defn main! []
  (println "[main]: loading"))

(defn reload! []
  (println "[main] reloaded lib:" lib/c lib/d)
  (println "[main] reloaded:" a b))
