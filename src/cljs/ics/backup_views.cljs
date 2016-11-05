(ns ics.backup-view
  (:require [re-frame.core :as re-frame :refer [subscribe dispatch]]
            [re-com.core :refer [button info-button v-box hyperlink-href radio-button throbber]]
            [reagent.core :as r]
            [cljs-time.core :as t]
            [cljs-time.periodic :as p]
            [cljs-time.format :as f]
            [cljs-time.coerce :as c]
            [cljsjs.reactable]
            [cljsjs.nprogress]
            [ics.common :refer [validate-email sec-to-date date-to-format sec-to-format]]
            [ics.common-component :refer [date-component today parse-date]]
            [cljs.pprint :refer [pprint]]
            [cljs-time.core :as ct]))

(defn debug [data]
  [:pre (with-out-str (pprint data))])

;;;
(def hi-config
  {:chart    {:type "line"}
   :title    {:text "Monthly Average Temperature"
              :x    -20}
   :subtitle {:text "Source: WorldClimate.com"
              :x    -20}
   :xAxis    {:categories ["Jan" "Feb" "Mar" "Apr" "May" "Jun" "Jul" "Aug" "Sep" "Oct" "Nov" "Dec"]}
   :yAxis    {:title     {:text "数量"}
              :plotLines [{:value 0 :width 1 :color "#808080"}]}
   :tooltip  {:valueSuffix "C"}
   :legend   {:layout        "vertial"
              :align         "right"
              :verticalAlign "middle"
              :borderWidth   0}
   :series   [{:name "Tokey"
               :data [7.0, 6.9, 9.5, 14.5, 18.2, 21.5, 25.2, 26.5, 23.3, 18.3, 13.9, 9.6]}
              {:name "New York"
               :data [-0.2, 0.8, 5.7, 11.3, 17.0, 22.0, 24.8, 24.1, 20.1, 14.1, 8.6, 2.5]}
              {:name "Berlin"
               :data [-0.9, 0.6, 3.5, 8.4, 13.5, 17.0, 18.6, 17.9, 14.3, 9.0, 3.9, 1.0]}
              {:name "London"
               :data [3.9, 4.2, 5.7, 8.5, 11.9, 15.2, 17.0, 16.6, 14.2, 10.3, 6.6, 4.8]}]
   })

(defn hello-message [name]
  (r/create-class
    {:reagent-render
     (fn []
       [:div (str "Hello " name)])}))

(defn timer []
  (let [state (r/atom {:secondsElapsed 0})
        tick #(swap! state update-in [:secondsElapsed] inc)
        interval (r/atom nil)]
    (r/create-class
      {:reagent-render
       (fn []
         [:div (str "Seconds Elapsed: " (:secondsElapsed @state))])
       :component-did-mount
       (fn []
         (reset! interval (js/setInterval tick 1000)))
       :component-will-unmount
       (fn []
         (js/clearInterval @interval))})))

(defn todolist [items]
  (r/create-class
    {:reagent-render
     (fn []
       (let [create-item (fn [item]
                           [:li {:key (:id item)} (:text item)])]
         [:ul
          (map create-item items)]))}))

(defn todoapp []
  (let [state (r/atom {:items []
                       :text  ""})
        onChange (fn [e]
                   (swap! state update-in [:text] #(-> e .-target .-value)))
        handleSubmit (fn [e]
                       (.preventDefault e)
                       (let [next-items (vec
                                          (concat (:items @state)
                                                  [{:text (:text @state) :id (js/Date.)}]))
                             next-text ""]
                         (reset! state {:items next-items
                                        :text  next-text})))]
    (r/create-class
      {:reagent-render
       (fn []
         [:div
          [:h3 "TODO"]
          ;[todolist (:items @state)] ;; todo !!!!
          [:ul                                              ;; OK
           (map (fn [item]
                  [:li {:id (:id item)} (:text item)])
                (:items @state))]
          [:form {:onSubmit handleSubmit}
           [:input {:onChange onChange
                    :value    (:text @state)}]
           [:button (str "Add #" (count (:items @state)))]]])})))

(defn highcharts [config]
  (r/create-class
    {:reagent-render
     (fn []
       [:div
        {:style {:min-width "800px" :max-width "2000px"
                 :height    "400px" :margin "0 auto"}}
        ])
     :component-did-mount
     (fn [this]
       (js/Highcharts.Chart. (r/dom-node this) (clj->js config)))}))

(defn default []
  (let [date (r/atom (today))]
    (fn []
      [:div
       [:h2 "Default"]
       ;[debug (ct/now)]
       ;[debug (today)]
       ;[debug {:a 1 :b 2}]
       [debug {:username (:username @re-frame.db/app-db)}]
       [debug {:detail-user-info (:detail-user-info @re-frame.db/app-db)}]
       [debug {:password (:password @re-frame.db/app-db)}]
       [debug {:authkey (:authkey @re-frame.db/app-db)}]
       [debug {:page (:page @re-frame.db/app-db)}]
       [highcharts hi-config]
       ]
       )))

(defn video []
  (fn []
    [:div.embed-responsive.embed-responsive-16by9
     [:iframe.embed-responsive-item {:src "https://www.youtube.com/embed/Q6omsDyFNlk"}]] ;; notice this is embed video
    ))
