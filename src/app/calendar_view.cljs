(ns app.calendar-view
  (:require [app.get-month :refer [get-days-in-month-loop]]))

(defn get-next-days [month year]
  (if (= month 11)
    (get-days-in-month-loop 11 (inc year))
    (get-days-in-month-loop (inc month) year)))

(defn get-prev-days [month year]
  (if (= month 0)
    (get-days-in-month-loop 11 (dec year))
    (get-days-in-month-loop (dec month) year)))

(defn calendar-view [{:keys [month year days-per-row]}]
  (let [days-collection      (get-days-in-month-loop month year)
        next-days-collection (get-next-days month year)
        prev-days-collection (get-prev-days month year)
        first-day            (first days-collection)
        how-far-to-left      (mod (.getDay first-day) days-per-row)
        last-from-prev-days  (take-last how-far-to-left prev-days-collection)
        current-with-prev    (concat last-from-prev-days days-collection)
        rows                 (js/Math.ceil (/ (count current-with-prev) days-per-row))
        should-have-cells    (* rows days-per-row)
        how-far-to-right     (- should-have-cells (count current-with-prev))
        first-from-next-days (take how-far-to-right next-days-collection)
        all-days-collection  (concat last-from-prev-days
                                     days-collection
                                     first-from-next-days)]
    all-days-collection))


#_(js/console.log (clj->js (calendar-view {:month 10 :year 2017 :days-per-row 7})))

