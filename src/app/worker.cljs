(ns app.worker
  (:require [reagent.core :as reagent]
            [re-frame.core :as rf]
            [clojure.string :as str]))

(defonce service-worker (atom {:worker/reference nil :worker/msg-channel nil}))

(defn listen-on-channel! [channel handler]
  (aset channel "port1" "onmessage" handler))

(defn send-worker-handshake! [worker channel]
  (.postMessage worker
                #js {:welcome true}
                #js [(.-port2 channel)]))

(defn spawn-worker []
  {:worker/reference   (js/Worker. "http://localhost:8080/worker.js")
   :worker/msg-channel (js/MessageChannel.)})

(rf/reg-event-fx
 :event/start-worker
 (fn []
   {:effect/spawn-worker (spawn-worker)}))

(rf/reg-fx
 :effect/spawn-worker
 (fn [worker]

   (reset! service-worker worker)

   (listen-on-channel!
    (:worker/msg-channel worker)
    #(rf/dispatch [:event/msg-from-worker %]))

   (send-worker-handshake!
    (:worker/reference worker)
    (:worker/msg-channel worker))))

;; collects service worker data, and calls :post-message effect
(rf/reg-event-fx
 :event/post-to-worker
 [(rf/inject-cofx :coeffect/service-worker)]
 (fn [{:keys [worker]}]
   {:effect/post-to-worker service-worker}))

;; assigns current worker reference and message channel to
;; :service-worker coeffect
(rf/reg-cofx
 :coeffect/service-worker
 (fn [coeffects]
   (assoc coeffects :worker @service-worker)))

;; actually send message to worker
(rf/reg-fx
 :effect/post-to-worker
 (fn [worker]
   (.postMessage (:worker/reference worker)
                 #js {:random-number (js/Math.random)})))

;; saves message got from worker in re-frame db
(rf/reg-event-db
 :event/msg-from-worker
 (fn [db [_ msg]]
   (assoc db :worker/msg (.-data msg))))

(rf/reg-sub
 :sub/worker.msg
 (fn [db _]
   (:worker/msg db)))

(defn ui
  []
  [:div
   [:h1 "Let's talk with service worker!"]

   [:button {:on-click #(rf/dispatch [:event/start-worker])} "spawn worker"]
   [:button {:on-click #(rf/dispatch [:event/post-to-worker])}
    "send random data to worker"]

   [:div
    [:h1 "service worker message"]

    [:div
     (js/JSON.stringify (clj->js @(rf/subscribe [:sub/worker.msg])))]]])

(defn ^:export run []
  (reagent/render [ui] (js/document.getElementById "root")))
