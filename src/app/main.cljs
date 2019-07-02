(ns app.main
  (:require [app.lib :as lib]
            [app.most-counter :refer [make-counter-3!]]
            [app.worker :refer [run]]
            ["react-dom" :refer [render]]
            ["react-hyperscript" :as h]
            ["./calendar.js" :refer [getDaysInMonthRecursive Calendar]]
            [app.calendar-view :refer [calendar-view]]
            [app.get-month :refer [get-days-in-month-loop-3]]))

(defn get-calendar-block [props]
  (clj->js (calendar-view
            (-> props
                js->clj
                clojure.walk/keywordize-keys
                (clojure.set/rename-keys
                 {:daysPerRow :days-per-row})))))

(def root (h Calendar #js {:_getCalendarBlock get-calendar-block}))

;; (render root (js/document.getElementById "root"))

(make-counter-3!)

(defn main! []
  (run)

  (println "[main]: loading"))

(defn reload! []

  (run)
  #_(println "[main] reloaded lib:" lib/c lib/d)
  #_(println "[main] reloaded:" a b))
