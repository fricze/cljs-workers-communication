(ns app.main
  (:require [app.worker :refer [run]]))

(defn main! []
  (run)

  (println "[main]: loading"))

(defn reload! []
  (run))
