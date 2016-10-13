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
            [cljs.pprint :refer [pprint]]
            ))

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
              :min       0
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

(defn reactable [data]
  [:div
   [:> Reactable.Table
    {:className       "table table-bordered table-striped"
     :data            data
     :itemsPerPage    6
     :pageButtonLimit 5
     :filterable      (clj->js [:Name :Age])
     :sortable        true
     }
    ;[:> Reactable.Tr
    ; [:> Reactable.Td {:column "Name" :data "Griff"} "Griff"]
    ; [:> Reactable.Td {:column "Age"} 18]]
    ;[:> Reactable.Tr
    ; [:> Reactable.Td {:column "Name"} "Sale"]
    ; [:> Reactable.Td {:column "Age"} 98]]
    ]
   ]
  )

(defn info-button1 []
  (let [info [v-box
              :children [[:p.info-heading "Info Popup Heading"]
                         [:p "You can use the " [:span.info-bold "info-bold"] " class to make text bold."]
                         [:p "Use the " [:span.info-bold "code"] " element to display source code:"]
                         [:code
                          "(defn square [n] (* n n))" [:br]
                          "=> #'user/square" [:br]
                          "(square 45)" [:br]
                          "=> 2025" [:br]
                          ]
                         [:p.info-subheading "Sub heading"]
                         [:p
                          "Note: Styles copied from "
                          [hyperlink-href
                           :label "ClojureScript Cheatsheet"
                           :href "http://cljs.info/cheatsheet"
                           :target "_blank"]
                          "."]]]]
    (fn []
      [info-button
       :info info])))

(defn info-button2 []
  (let [info [v-box
              :children [[:p "You can touch my github here"]
                         [:p
                          [hyperlink-href
                           :label "Github"
                           :href "http://www.github.com/colorgmi"
                           :target "_blank"]]]]]
    (fn []
      [info-button
       :info info])))

(defn radio1 []
  (let [color (r/atom "green")]
    (fn []
      [v-box
       :children [(doall (for [c ["red" "green" "blue"]]    ;; Notice the ugly "doall"
                           ^{:key c}                        ;; key should be unique among siblings
                           [radio-button
                            :label c
                            :value c
                            :model color
                            :label-style (if (= c @color) {:color       c
                                                           :font-weight "bold"})
                            :on-change #(reset! color c)]))]])))

(defn default []                                            ;; todo for test
  (fn []
    [:div

     [:div [:input {:type "text"}]]
     [:div [:input {:type "text" :value "v2"}]]
     [debug {:a 1 :b 2}]
     [debug {:username (:username @re-frame.db/app-db)}]
     [debug {:password (:password @re-frame.db/app-db)}]
     [debug {:authkey (:authkey @re-frame.db/app-db)}]
     [debug {:page (:page @re-frame.db/app-db)}]
     [debug {:firstuser (first (:users @re-frame.db/app-db))}]
     ;[debug (filter #(not (nil? (re-find (re-pattern "co") (% "email")))) (:users @re-frame.db/app-db))]

     [radio1]

     [info-button1]

     [button
      :label "Clicke me!"
      :tooltip "I'm a tooltip!"
      :tooltip-position :right-center]

     [debug (first (filter #(= "george.zhu@baichanghui.com" (% "email")) (:users @re-frame.db/app-db)))]
     ;[:div "default"]
     ;[reactable (clj->js (let [n 10]
     ;                      (repeatedly n (fn [] {:Name "ccd" :Age (rand-int n)}))))]
     ;[highcharts hi-config]
     ;[:div (str (js/Date. 1473436800000))]
     [:div (sec-to-format 1472659200)]]))

(defn about []
  (fn []
    [:div

     [info-button2]

     ;[:button.btn.btn-default
     ; {:on-click (fn [e] (js/console.log (-> e .-target .-innerText)))}
     ; "boid"]

     ;[:div.input-group.input-goup-lg
     ; [:span.input-group-addon "Search"]
     ; [:input.form-control {:type "text" :placeholder "Search"}]]

     ;[:form
     ; [:input {:type "radio" :checked "true" :name "A"}]
     ; [:input {:type "radio" :name "B"}]
     ; ]

     ;[:div "dfdf"]

     ;[:div.btn-group {:data-toggle "buttons"}
     ; [:label.btn.btn-primary.active
     ;  [:input {:type "radio" :name "options" :id "option1" :autocomplete "off" :checked "checked"} "Radio 1 (preselected)"]]
     ; [:label.btn.btn-primary
     ;  [:input {:type "radio" :name "options" :id "option2" :autocomplete "off"} "Radio 2"]]
     ; ]

     ]
    ))

(defn video []
  (fn []
    [:div.embed-responsive.embed-responsive-16by9
     [:iframe.embed-responsive-item {:src "https://www.youtube.com/embed/Q6omsDyFNlk"}]] ;; notice this is embed video
    ))

(defn localvideo []
  (fn []
    [:video {:src      "SongOfTheSea/SongOfTheSea.mp4"
             :controls "controls"
             :width    "720"
             :height   "480"}]))


(defn users-table2 []
  (let [users (subscribe [:users])]
    (fn []
      [:div.panel.panel-default
       [:div.panel-heading "Panel heading"]
       [:div.panel-body
        [:p "blabla"]]
       [:table.table.table-bordered.table-striped.table-condensed.table-responsive
        [:thead
         [:tr
          [:th "账户"]
          [:th "创建日期"]
          [:th "注册来源"]
          [:th "手机号"]
          [:th "黑名单"]
          [:th "API"]
          [:th "MAU"]
          [:td "付费详情"]]]
        [:tbody
         (for [u @users]
           [:tr
            [:td (u "email")]
            [:td (f/unparse (f/formatters :date-time-no-ms) (c/from-long (* 1000 (u "created_at"))))]
            [:td ((u "operation_info") "from")]
            [:td ((u "operation_info") "phone")]
            [:td [:button.btn.btn-primary {:on-click #(js/console.log "拉黑")} "拉黑"]]
            [:td (last (last (u "api")))]
            [:td (last (last (u "mau")))]
            [:td [:a {:href (str "#/user/" (u "id"))} "付费详情"]]])]]
       [:div.panel-body
        [:button.btn.btn-default "click me!"]]])))

; [rich-table data]
(comment
  (defn rich-table [data]                                   ;; add page nav buttons
    [:table.table.table-bordered.table-striped
     [:thead>tr
      (for [th (keys (first data))]
        [:th (str th)])
      [:tbody
       (for [d data]
         [:tr [:td (val d)]])]]]))

(defn highcharts [config]
  (r/create-class
    {:reagent-render
     (fn []
       [:div {:style {:min-width "310px" :max-width "2000px"
                      :height    "400px" :margin "0 auto"}}])
     :component-did-mount
     (fn [this]
       (js/Highcharts.Chart. (r/dom-node this) (clj->js config)))}))
