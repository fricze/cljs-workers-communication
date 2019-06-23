(ns app.calendar
  (:require ["./calendar.js" :refer [getDaysInMonthRecursive]]))

(defn get-days-in-month-loop [month year]
  (loop [month month
         year year
         date (js/Date. year month 1)
         days []]
    (if (= (.getMonth date) month)
      (let [next-date (js/Date. date)
            _ (.setDate next-date (inc (.getDate date)))]
        (recur month year next-date (conj days (js/Date. date))))
      days)))

(defn get-days-in-month
  ([month year]
   (get-days-in-month month year (js/Date. year month 1) []))

  ([month year date days]
   (if (= (.getMonth date) month)
     (let [next-date (js/Date. date)
           _ (.setDate next-date (inc (.getDate date)))]
       (get-days-in-month
        month year
        next-date
        (conj days (js/Date. date))))

     days)))

(comment
  (js/console.log
   (getDaysInMonthRecursive 3 2014))

  (js/console.log (clj->js (get-days-in-month 10 2014))))


;; REFACTOR 1


(defn same-month [date month]
  (= (.getMonth date) month))

(defn get-next-date []
  (let [next-date (js/Date. date)
        _ (.setDate next-date (inc (.getDate date)))]
    next-date))

(defn get-days-in-month-loop [month year]
  (loop [month month
         year year
         date (js/Date. year month 1)
         days []]
    (if (same-month date month)
      (let [next-date (get-next-date date)]
        (recur month year next-date (conj days (js/Date. date))))
      days)))

(defn get-days-in-month
  ([month year]
   (get-days-in-month month year (js/Date. year month 1) []))

  ([month year date days]
   (if (same-month date month)
     (get-days-in-month
      month year
      (get-next-date date)
      (conj days (js/Date. date)))

     days)))


;; REFACTOR 2


(defn get-days-in-month-loop [month year]
  (loop [month month
         year year
         date (js/Date. year month 1)
         days []]
    (if (same-month date month)
      (recur month year (get-next-date date) (conj days (js/Date. date)))
      days)))

(defn get-days-in-month
  ([month year]
   (get-days-in-month month year (js/Date. year month 1) []))

  ([month year date days]
   (if (same-month date month)
     (get-days-in-month
      month year
      (get-next-date date)
      (conj days (js/Date. date)))

     days)))
