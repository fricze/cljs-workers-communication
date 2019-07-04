(ns app.worker
  (:require [reagent.core :as reagent]
            [re-frame.core :as rf]
            [clojure.string :as str]))

(rf/reg-event-db
 :initialize
 (fn [_ _] {}))

(rf/reg-event-fx
 :work-it
 []
 (fn [{:keys [db]}]
   (let [worker (js/Worker. "http://localhost:8080/worker.js")
         msg-channel (js/MessageChannel.)]
     {:spawn-worker {:msg-channel msg-channel :worker worker}
      :db (assoc db :msg-channel msg-channel :worker worker)})))

(defn listen-on-channel! [channel handler]
  (aset channel "port1" "onmessage" handler))

(defn worker-handshake! [worker channel]
  (.postMessage worker
                #js {:welcome true}
                #js [(.-port2 channel)]))

(rf/reg-fx
 :spawn-worker
 (fn [{:keys [msg-channel worker]}]

   (listen-on-channel! msg-channel #(rf/dispatch [:worker-msg %]))

   (worker-handshake! worker msg-channel)))

(rf/reg-event-fx
 :send-it
 (fn [{:keys [db]}]
   {:post-message db}))

(rf/reg-fx
 :post-message
 (fn [{:keys [msg-channel worker]}]
   (.postMessage worker #js {:random-data (js/Math.random)})))

(rf/reg-event-db
 :worker-msg
 (fn [db [_ msg]]
   (assoc db :msg (.-data msg))))

(rf/reg-sub
 :worker-msg
 (fn [db _]
   (:msg db)))

(defn ui
  []
  [:div
   [:h1 "Let's talk with service worker!"]
   [:button {:on-click #(rf/dispatch [:work-it])} "worker"]
   [:button {:on-click #(rf/dispatch [:send-it])} "sender"]
   [:div
    [:h1 "service worker message"]
    [:div
     (js/JSON.stringify (clj->js @(rf/subscribe [:worker-msg])))]]])

;; -- Entry Point -------------------------------------------------------------


(defn ^:export run
  []
  (rf/dispatch-sync [:initialize])
  ;; puts a value into application state
  (reagent/render [ui]
                  ;; mount the application's ui into '<div id="app" />'
                  (js/document.getElementById "root")))
