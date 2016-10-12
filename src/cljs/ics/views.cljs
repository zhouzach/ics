(ns ics.views
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
            [ics.backup-view :refer [about default debug video localvideo highcharts]]
            [cljs.pprint :refer [pprint]]
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
   [:p [:input {:type "submit" :value "login" :class "btn bt-default btn-lg"}]] [:br]])

(defn users-table [config]
  (let [page (:page config)
        items-per-page (:items-per-page config)
        search (:search config)
        users (subscribe [:users])
        ;users @users
        ;
        ;;users (filter (fn [u]
        ;;                (boolean
        ;;                  (re-find (re-pattern search) (u "email"))))
        ;;              users)
        ;
        ;users (subvec users
        ;              (* items-per-page (dec page))
        ;              (* items-per-page page))
        ]
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
         (for [u (take 10 @users)]
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

(defn apusers-table []
  (let [apusers (subscribe [:apusers])]
    (fn []
      [:table.table.table-bordered.table-striped
       [:thead
        [:tr
         [:th "申请时间"]
         [:th "公司 / 产品"]
         [:th "姓名"]
         [:th "邮件"]
         [:th "手机号"]
         [:th "MAU"]
         [:th "发送邀请码"]
         [:th "丢弃"]
         [:th "处理状态"]]]
       [:tbody
        (for [u (take 10 @apusers)]
          ;(for [u @apusers]
          [:tr
           [:td (u "created_at")]
           [:td (u "company")]
           [:td (u "name")]
           [:td (u "email")]
           [:td (u "phone")]
           [:td (u "mau")]
           [:td [:button.btn.btn-primary {:on-click #(println "send1")} "发送"]]
           ;[:td [:button.btn.btn-primary {:on-click #(println "send2")} "丢弃"]]
           [:td [button
                 :class "btn btn-primary"
                 :label "丢弃"
                 :tooltip "小心!"
                 :tooltip-position :right-center
                 :on-click #(println "丢弃")]]
           [:td (u "discard")]])]])))

(defn apusers-table2 []
  (let [apusers (subscribe [:apusers])
        page-num (r/atom 1)
        items-per-page 10
        search (r/atom "")
        ]
    (r/create-class
      {:reagent-render
       (fn []
         [:div.container
          [:div.input-group.input-goup-lg
           [:span.input-group-addon "Search"]
           [:input.form-control
            {:type      "text" :placeholder "Search" :value @search
             :on-change (fn [e]
                          (js/console.log (-> e .-target .-value))
                          (reset! search (-> e .-target .-value))
                          (dispatch [:apusers {"applied_users"
                                               (filter (fn [apuser]
                                                         (boolean
                                                           (or
                                                             (re-find (re-pattern @search) (get apuser "company"))
                                                             (re-find (re-pattern @search) (get apuser "name")))))
                                                       @apusers)}]))}]]
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
                      {:on-click #(dispatch [:apusers {"applied_users"
                                                       (sort-by (fn [e] (get e "mau")) @apusers)}])}
                      "降序"]]]
             [:th "发送邀请码"]
             [:th "丢弃"]
             [:th "处理状态"]]]
           [:tbody
            (for [apuser
                  ;(subvec
                  ;  (filter (fn [apuser]
                  ;            (boolean (or
                  ;                       (re-find (re-pattern @search) (get apuser "company"))
                  ;                       (re-find (re-pattern @search) (get apuser "name"))
                  ;                       )))
                  ;          @apusers)
                  ;  ;@apusers
                  ;  (* items-per-page (dec @page-num))
                  ;  (* items-per-page @page-num)
                  ;  )

                  (filter (fn [apuser]
                            (boolean (or
                                       (re-find (re-pattern @search) (get apuser "company"))
                                       (re-find (re-pattern @search) (get apuser "name"))
                                       )))
                          @apusers)
                  ]
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

(defn line-chart-config [user]
  (let [api (user "api")
        email (user "email")
        pay (user "pay")
        api-series {:name "api"
                    :data (vec (for [{:strs [day num]} api]
                                 [(sec-to-format day) num]))}
        pay-series (for [{:strs [begin end num]} pay]
                     {:name      "pay"
                      :lineWidth 5
                      :data      (map (fn [d] [(date-to-format d) num])
                                      (take (inc (t/in-days (t/interval (sec-to-date begin) (sec-to-date end))))
                                            (iterate (fn [d] (t/plus d (t/days 1))) (sec-to-date begin))))})]
    {:chart    {:type "line"}
     :title    {:text (str "email: " email)
                :x    -20}
     :subtitle {:text ""
                :x    -20}
     ;:xAxis       {:categories (mapv #(format-date (% "day")) api)}
     :yAxis    {:title     {:text "数量"}
                :min       0
                :plotLines [{:value 0 :width 1 :color "#808080"}]}
     :tooltip  {:valueSuffix ""}
     :legend   {:layout        "vertial"
                :align         "right"
                :verticalAlign "middle"
                :borderWidth   0}
     ;:series   (vec (cons api-series pay-series)) ; todo not fit of string "2016-10-10"
     :series   [api-series]}))

(defn user-graph []
  (let [user (subscribe [:detail-user])]
    (fn []
      [:div
       [:pre "付费详情"]
       [highcharts (line-chart-config @user)]])))

;; display testin all user's api sum
(defn testin-api-sum []
  (let [testin-api-sum (subscribe [:testin-api-sum])]
    [:div
     [:div "from_hour: "
      [:input#from_hour {:type "text"}]
      [:button.btn.btn-default {:on-click #(set! (.-value (js/document.getElementById "from_hour")) "2016-04-10T17:48:31.953z")} "reset"]]
     [:div "tooo_hour: "
      [:input#to_hour {:type "text"}]
      [:button.btn.btn-default {:on-click #(set! (.-value (js/document.getElementById "to_hour")) "2016-10-10T17:48:31.953z")} "reset"]]
     [:button.btn.btn-primary {:on-click
                               #(dispatch [:acc-testin-api
                                           (.-value (js/document.getElementById "from_hour"))
                                           (.-value (js/document.getElementById "to_hour"))])}
      "acc"]
     [debug @testin-api-sum]]))

(defn script []
  [:div "i dfl i"])

(defn main []
  (let [page (subscribe [:page])]
    (fn []
      [:div.main
       (case @page
         :default [default]
         :users [users-table {:items-per-page 10
                              :page           1
                              :search         "adc"}]
         :apusers [apusers-table]
         :about [about]
         :user [user-graph]
         :video [video]
         :localvideo [localvideo]
         :script [script]
         :testin-api-sum [testin-api-sum]
         )])))

(defn main-panel []
  (let [username (subscribe [:username])
        page (subscribe [:page])]
    (fn []
      (if (nil? @username)
        [:div.container
         [:div.page-header [:h1 "ICS"]]
         [:div.jumbotron [login-in]]]
        [:div.container
         [:nav.navbar.navbar-default
          [:div.container-fluid
           [:div.collapse.navbar-collapse
            [:ul.nav.nav-pills
             [:li {:class (when (= @page :default) "active")}
              [:a {:href "#/nav/default"} "Default"]]
             [:li {:class (when (= @page :users) "active")}
              [:a {:href "#/nav/users"} "用户"]]
             [:li {:class (when (= @page :apusers) "active")}
              [:a {:href "#/nav/apusers"} "申请用户"]]
             [:li {:class (when (= @page :about) "active")}
              [:a {:href "#/nav/about"} "About"]]
             [:li {:class (when (= @page :script) "active")}
              [:a {:href "#/nav/script"} "Script"]]
             [:li {:class (when (= @page :testin-api-sum) "active")}
              [:a {:href "#/nav/testin-api-sum"} "testin-api-sum"]]
             [:li.dropdown
              [:a.dropdown-toggle {:href          "#/nav/default" :data-toggle "dropdown" :role "button"
                                   :aria-haspopup "true" :aria-expanded "false"}
               "More Example" [:span.caret]]
              [:ul.dropdown-menu
               [:li [:a {:href "#/nav/video"} "video from youtube"]]
               [:li [:a {:href "#/nav/localvideo"} "local video"]]
               [:li.divider {:role "separator"} [:a {:href "#"}]]
               [:li [:a {:href "#"} "ZZ"]]]]]]]]
         [main]]))))