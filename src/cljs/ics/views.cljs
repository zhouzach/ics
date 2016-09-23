(ns ics.views
  (:require [re-frame.core :as re-frame :refer [subscribe dispatch]]
            [reagent.core :as r]
            [cljs-time.core :as t]
            [cljs-time.periodic :as p]
            [cljs-time.format :as f]
            [cljs-time.coerce :as c]
            [cljsjs.reactable]
            [cljsjs.nprogress]
            [ics.common :refer [validate-email]]
            ))

(defn login-in []
  [:form.login {:onSubmit (fn [e]
                            (.preventDefault e)
                            (let [username (.-value (js/document.getElementById "username"))
                                  password (.-value (js/document.getElementById "password"))]
                              (if-not (validate-email username)
                                (js/alert "invalid email")
                                (dispatch [:login username password]))))}
   [:p "username: " [:input {:type "text" :placeholder "username" :id "username"}] [:br]]
   [:p "password: " [:input {:type "password" :placeholder "password" :id "password"}] [:br]]
   [:p [:input {:type "submit" :value "login" :class "btn bt-default btn-lg"}]] [:br]
   ])


(defn login-in-success []
  (let [username (subscribe [:username])]
    [:p "Welcome: " @username]))

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
               :data [3.9, 4.2, 5.7, 8.5, 11.9, 15.2, 17.0, 16.6, 14.2, 10.3, 6.6, 4.8]}]})

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

(defn highcharts []
  (r/create-class
    {:reagent-render
     (fn []
       [:div {:style {:min-width "310px" :max-width "1000px"
                      :height    "600px" :margin "0 auto"}}])
     :component-did-mount
     (fn [this]
       (js/Highcharts.Chart. (r/dom-node this) (clj->js hi-config)))}))

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

(defn default []                                            ;; todo for test
  (fn []
    [:div
     [:div "default"]
     [reactable (clj->js (let [n 10]
                           (repeatedly n (fn [] {:Name "ccd" :Age (rand-int n)}))))]
     ;[highcharts]
     ;[:div (str (js/Date. 1473436800000))]
     ;[:div (f/unparse (f/formatters :date-time-no-ms) (c/from-long 1473436800000))]
     ]))

(defn about []
  (fn []
    [:div
     [:button.btn.btn-default
      {:on-click (fn [e] (js/console.log (-> e .-target .-innerText)))}
      "boid"]

     ;[:form
     ; [:input {:type "radio" :checked "true" :name "A"}]
     ; [:input {:type "radio" :name "B"}]
     ; ]

     [:div "dfdf"]

     ;[:div.btn-group {:data-toggle "buttons"}
     ; [:label.btn.btn-primary.active
     ;  [:input {:type "radio" :name "options" :id "option1" :autocomplete "off" :checked "checked"} "Radio 1 (preselected)"]]
     ; [:label.btn.btn-primary
     ;  [:input {:type "radio" :name "options" :id "option2" :autocomplete "off"} "Radio 2"]]
     ; ]
     "about"]))

(defn users-table []
  (let [users (subscribe [:users])
        items-per-page 15]
    (r/create-class
      {:reagent-render
       (fn []
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
            [:td "付费详情"]
            ]]
          [:tbody
           (for [user @users]
             [:tr
              [:td (get user "email")]
              [:td (f/unparse (f/formatters :date-time-no-ms) (c/from-long (* 1000 (get user "created_at"))))]
              [:td (get (get user "operation_info") "from")]
              [:td (get (get user "operation_info") "phone")]
              [:td [:button.btn.btn-primary {:on-click #(js/console.log "拉黑")} "拉黑"]]
              [:td (last (last (get user "api")))]
              [:td (last (last (get user "mau")))]
              [:td [:a {:href (str "#/paydetails/" (get user "email"))} "付费详情"]]
              ;[:td [:a {:href "#/paydetails" :on-click #(dispatch [:paydetails (get user "email")])} "付费详情"]]
              ])]]
         )
       })))

(defn apusers-table []
  (let [apusers (subscribe [:apusers])
        page-num (r/atom 1)
        items-per-page 10
        ]
    (r/create-class
      {:reagent-render
       (fn []
         [:div.container
          [:table.table.table-bordered.table-striped
           [:thead
            [:tr
             [:th "申请时间"]
             [:th "公司 / 产品"]
             [:th "姓名"]
             [:th "邮件"]
             [:th "手机号"]
             [:th "MAU"
              [:span [:button.btn.btn-default
                      {:on-click #(dispatch [:apusers {"applied_users" (sort-by (fn [e] (get e "mau")) @apusers)}])}
                      "降序"]]]
             [:th "发送邀请码"]
             [:th "丢弃"]
             [:th "处理状态"]]]
           [:tbody
            (for [apuser (subvec @apusers
                                 (* items-per-page (dec @page-num))
                                 (* items-per-page @page-num))]
              [:tr
               [:td (get apuser "created_at")]
               [:td (get apuser "company")]
               [:td (get apuser "name")]
               [:td (get apuser "email")]
               [:td (get apuser "mobile")]
               [:td (get apuser "mau")]
               [:td [:button.btn.btn-primary {:on-click #(js/console.log "send1")} "发送"]]
               [:td [:button.btn.btn-primary {:on-click #(js/console.log "send2")} "丢弃"]]
               [:td (get apuser "discard")]])]]
          (for [i (range 1 (js/Math.floor (/ (count @apusers) items-per-page)))]
            [:span [:button.btn.btn-default
                    {:on-click #(reset! page-num (js/parseInt (-> % .-target .-innerText)))}
                    (str i)]])
          ]
         )
       })))

(defn paydetails-graph []
  (let [user (subscribe [:current-pay-user])]
    (fn []
      [:div
       [:p "付款详情"]
       [:p (str "帐号" (get @user "email"))]
       [:p "收费设置"]
       ])))

(defn main []
  (let [select (subscribe [:select])]
    (r/create-class
      {:reagent-render
       (fn []
         [:div.main
          (case @select
            :default [default]
            :users [users-table]
            :apusers [apusers-table]
            :paydetails [paydetails-graph]
            :about [about]
            )])
       })))

(defn main-panel []
  (let [username (subscribe [:username])
        select (subscribe [:select])]

    (if (nil? @username)
      [:div.container
       [:div.page-header [:h1 "ICS"]]
       [:div.jumbotron [login-in]]]

      [:div.container
       ;[:div.jumbotron
       [:nav.navbar.navbar-default
        [:div.container-fluid
         ;[:div.navbar-header
         ; [:a.pull-left {:href "#/"} [:img {:src "cljs.png"}]]]
         [:div.collapse.navbar-collapse
          [:ul.nav.nav-pills
           [:li {:class (when (= @select :default) "active")}
            [:a {:href "#/"} "Default"]]
           [:li {:class (when (= @select :users) "active")}
            [:a {:href "#/users"} "用户"]]
           [:li {:class (when (= @select :apusers) "active")}
            [:a {:href "#/apusers"} "申请用户"]]
           [:li.dropdown
            [:a.dropdown-toggle {:href          "#/users" :data-toggle "dropdown" :role "button"
                                 :aria-haspopup "true" :aria-expanded "false"}
             "Contact Us" [:span.caret]]
            [:ul.dropdown-menu
             [:li [:a {:href "#"} "XX"]]
             [:li [:a {:href "#"} "YY"]]
             [:li.divider {:role "separator"} [:a {:href "#"}]]
             [:li [:a {:href "#"} "ZZ"]]]]
           [:li {:class (when (= @select :about) "active")}
            [:a {:href "#/about"} "About"]]]

          ;[:from.navbar-form.navbar-right {:role "search"}
          ; [:div.form-group
          ;  [:input.form-control {:type "text" :placeholder "Search"}]]
          ; [:button.btn.btn-default {:type "submit"} "Submit"]]
          ]]]
       [main]
       ])))
