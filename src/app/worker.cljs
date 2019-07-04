(ns app.worker
  (:require [reagent.core :as reagent]
            [re-frame.core :as rf]
            [clojure.string :as str]))

;; A detailed walk-through of this source code is provided in the docs:
;; https://github.com/Day8/re-frame/blob/master/docs/CodeWalkthrough.md

;; -- Domino 1 - Event Dispatch -----------------------------------------------


(defn dispatch-timer-event
  []
  (let [now (js/Date.)]
    (rf/dispatch [:timer now])))  ;; <-- dispatch used

;; Call the dispatching function every second.
;; `defonce` is like `def` but it ensures only one instance is ever
;; created in the face of figwheel hot-reloading of this file.

(defonce do-timer (js/setInterval dispatch-timer-event 1000))


;; -- Domino 2 - Event Handlers -----------------------------------------------


(rf/reg-event-db              ;; sets up initial application state
 :initialize                 ;; usage:  (dispatch [:initialize])
 (fn [_ _]                   ;; the two parameters are not important here, so use _
   {:time (js/Date.)         ;; What it returns becomes the new application state
    :time-color "#f88"}))    ;; so the application state will initially be a map with two keys


(rf/reg-event-db                ;; usage:  (dispatch [:time-color-change 34562])
 :time-color-change            ;; dispatched when the user enters a new colour into the UI text field
 (fn [db [_ new-color-value]]  ;; -db event handlers given 2 parameters:  current application state and event (a vector)
   (assoc db :time-color new-color-value)))   ;; compute and return the new application state

(rf/reg-event-fx                 ;; usage:  (dispatch [:timer a-js-Date])
 :timer                         ;; every second an event of this kind will be dispatched
 [(rf/inject-cofx :now)]
 (fn [{:keys [db now]} [_ new-time]]          ;; note how the 2nd parameter is destructured to obtain the data value
   {:db (assoc db :time now)}))  ;; compute and return the new application state

(rf/reg-event-fx
 :work-it
 []
 (fn [{:keys [db]}]
   (let [worker (js/Worker. "http://localhost:8080/worker.js")
         msg-channel (js/MessageChannel.)]
     {:spawn-worker {:msg-channel msg-channel :worker worker}
      :db (assoc db :msg-channel msg-channel :worker worker)})))

(rf/reg-fx
 :spawn-worker
 (fn [{:keys [msg-channel worker]}]
   (aset msg-channel "port1" "onmessage"
         #(rf/dispatch [:worker-msg %]))

   (.postMessage worker
                 #js {:welcome true}
                 #js [(.-port2 msg-channel)])))

(rf/reg-event-fx
 :send-it
 (fn [{:keys [db]}]
   {:post-message db}))

(rf/reg-fx
 :post-message
 (fn [{:keys [msg-channel worker]}]
   (.postMessage worker #js {:henlo :girls})))

(rf/reg-event-fx
 :worker-msg
 (fn [{:keys [db]} msg]
   (js/console.log (str :am-groot))
   (js/console.log msg)

   {:db db}))

(rf/reg-cofx               ;; registration function
 :now                 ;; what cofx-id are we registering
 (fn [coeffects _]    ;; second parameter not used in this case
   (assoc coeffects :now (js/Date.))))   ;; add :now key, with value


 ;; -- Domino 4 - Query  -------------------------------------------------------

(rf/reg-sub
 :time
 (fn [db _]     ;; db is current app state. 2nd unused param is query vector
   (:time db))) ;; return a query computation over the application state

(rf/reg-sub
 :time-color
 (fn [db _]
   (:time-color db)))


;; -- Domino 5 - View Functions ----------------------------------------------


(defn clock
  []
  [:div.example-clock
   {:style {:color @(rf/subscribe [:time-color])}}
   (-> @(rf/subscribe [:time])
       .toTimeString
       (str/split " ")
       first)])

(defn color-input
  []
  [:div.color-input
   "Time color: "
   [:input {:type "text"
            :value @(rf/subscribe [:time-color])
            :on-change #(rf/dispatch [:time-color-change (-> % .-target .-value)])}]])  ;; <---

(defn ui
  []
  [:div
   [:h1 "Hello world, it is now"]
   [:button {:on-click #(rf/dispatch [:work-it])} "worker"]
   [:button {:on-click #(rf/dispatch [:send-it])} "sender"]
   [clock]
   [color-input]])

;; -- Entry Point -------------------------------------------------------------

(defn ^:export run
  []
  (rf/dispatch-sync [:initialize])
  ;; puts a value into application state
  (reagent/render [ui]
                  ;; mount the application's ui into '<div id="app" />'
                  (js/document.getElementById "root")))
