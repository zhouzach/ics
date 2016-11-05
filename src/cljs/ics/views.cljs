(ns ics.views
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [cljs-http.client :as http]
            [cljs.core.async :refer [chan <! >! put! take!]]
            [re-frame.core :refer [reg-event-db path reg-sub dispatch subscribe]]
            [ajax.core :refer [GET POST]]
            [ics.db :as db]
            [re-com.core :refer [button info-button v-box hyperlink-href radio-button throbber]]
            [reagent.core :as r]
            [cljs-time.core :as t]
            [cljs-time.periodic :as p]
            [cljs-time.format :as f]
            [cljs-time.coerce :as c]
            [cljsjs.reactable]
            [cljsjs.nprogress]
            [ics.common :refer [validate-email sec-to-date date-to-format sec-to-format]]
            [ics.backup-view :refer [default debug video highcharts]]
            [cljs.pprint :refer [pprint]]
            [cljs-time.core :as ct]
            [cljs-time.format :as cf]
            [reagent-material-ui.core :refer [TextField DatePicker RaisedButton RadioButtonGroup RadioButton
                                              Table TableBody TableHeader TableHeaderColumn TableRow TableRowColumn
                                              AppBar AutoComplete MenuItem Menu Paper
                                              Tabs Tab LeftNav]] #_(LeftNav is Drawer)
            ))

(defn async-get
  [url authkey]
  (let [ch (chan)]
    (GET url {:headers {"Auth-Key" authkey}
              :handler (fn [res] (put! ch res))})
    ch))

(defn mytest []
  (let [state (r/atom false)]
    (fn []
      [:div
       [:h2 "MY-TEST"]
       [RaisedButton {:label      "Toggle Drawer"
                      :onTouchTap #(swap! state not)}]

       [MenuItem "Menu Item"]
       [MenuItem "Menu Item2"]
       ])))

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

(defn users-table []
  (let [users (subscribe [:users])
        _ (print "deok" @users)]
    (fn []
      [:div
       [:h2 "USERS"]
       [Table {:selectable false}
        [TableHeader {:displaySelectAll  false
                      :adjustForCheckbox false}
         [TableRow
          [TableHeaderColumn "账户"]
          [TableHeaderColumn "创建日期"]
          [TableHeaderColumn "手机号"]
          [TableHeaderColumn "API"]
          [TableHeaderColumn "MAU"]
          [TableHeaderColumn "黑名单"]
          [TableHeaderColumn "付费详情"]]]
        [TableBody {:displayRowCheckbox false
                    :showRowHover       true}
         (for [u @users]
           [TableRow
            [TableRowColumn (u "email")]
            [TableRowColumn (f/unparse (f/formatters :date-time-no-ms) (c/from-long (* 1000 (u "created_at"))))]
            [TableRowColumn ((u "operation_info") "phone")]
            [TableRowColumn (last (last (u "api")))]
            [TableRowColumn (last (last (u "mau")))]
            [TableRowColumn [RaisedButton {:label "拉黑" :on-click #(print "拉黑")}]]
            [TableRowColumn [:a {:href (str "#/detail-user/" (u "id"))
                                 :target "_blank"} "付费详情"]]])]]])))

(defn apusers-table []
  (let [apusers (subscribe [:apusers])]
    (fn []
      [:div
       [:h2 "APUSERS"]
       [Table {:selectable false}
        [TableHeader {:displaySelectAll  false
                      :adjustForCheckbox false}
         [TableRow
          [TableHeaderColumn "申请时间"]
          [TableHeaderColumn "公司 / 产品"]
          [TableHeaderColumn "姓名"]
          [TableHeaderColumn "邮件"]
          [TableHeaderColumn "手机号"]
          [TableHeaderColumn "MAU"]
          [TableHeaderColumn "发送邀请码"]
          [TableHeaderColumn "丢弃"]
          [TableHeaderColumn "处理状态"]]]
        [TableBody {:displayRowCheckbox false
                    :showRowHover       true}
         (for [u @apusers]
           [TableRow
            [TableRowColumn (u "created_at")]
            [TableRowColumn (u "company")]
            [TableRowColumn (u "name")]
            [TableRowColumn (u "email")]
            [TableRowColumn (u "phone")]
            [TableRowColumn (u "mau")]
            [TableRowColumn [RaisedButton {:label "发送" :on-click #(println "发送")}]]
            [TableRowColumn [RaisedButton {:label "丢弃" :on-click #(println "丢弃")}]]
            [TableRowColumn (u "discard")]])]]])))

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

(defn detail-user-graph []
  (let [detail-user-info (subscribe [:detail-user-info])
        _ (print "de4:" @detail-user-info)]
    (fn []
      [:div
       [:h2 "付费详情"]
       (when @detail-user-info #_(this when clause is very important otherwise error)
         [highcharts (line-chart-config @detail-user-info)])])))

(defn testin-api-sum
  "display testin all user's api sum,
   render UI year-month-day and local date,
   but http get utc time of that date,
   will send handreds of request to sum this number."
  []
  (let [authkey (subscribe [:authkey])
        result-sum (r/atom 0)
        clear #(reset! result-sum 0)
        from (r/atom (t/local-date-time 2016 5 25))
        to (r/atom (ct/time-now))
        acc-result (fn []
                     (go
                       (let [users ((<! (async-get "http://auth.appadhoc.com/users" @authkey)) "users")
                             testin-users (filter #(= "testin" (% "third_party_from")) users)
                             testin-users-ids (set (map #(% "id") testin-users))
                             apps ((<! (async-get "http://auth.appadhoc.com/apps" @authkey)) "apps")
                             testin-apps (filter #(contains? testin-users-ids (% "author_id")) apps)
                             testin-apps-ids (map #(% "id") testin-apps)]
                         (doseq [appid testin-apps-ids]
                           (let [v ((<! (async-get (str "http://data.appadhoc.com/apps/" appid
                                                        "/daily_api_count?"
                                                        "from_hour=" (f/unparse (f/formatters :date-time) (t/to-utc-time-zone @from))
                                                        "&to_hour=" (f/unparse (f/formatters :date-time) (t/to-utc-time-zone @to)))
                                                   @authkey))
                                     "daily_api_count")
                                 nums (map #(% "api_count" 0) v)
                                 sum (reduce + nums)]
                             (swap! result-sum #(+ % sum)))))))
        ]
    (fn []
      [:div
       [:h2 "TESTIN-API"]
       [DatePicker {:defaultDate @from
                    :floatingLabelText "from"
                    :onChange (fn [_ date]
                                (reset! from date)
                                (clear))}]
       [DatePicker {:defaultDate @to
                    :floatingLabelText "from"
                    :onChange (fn [_ date]
                                (reset! to date)
                                (clear))}]
       [RaisedButton {:label "GET DATA"
                      :secondary true
                      :on-click acc-result}]
       [:p]
       [debug @result-sum]])))

(defn data-fast-uv []
  (let [authkey (subscribe [:authkey])
        result (r/atom nil)
        clear #(reset! result nil)
        appidSource ["ac66bf61-7608-4a5f-9bc4-9e7cf0f9694f"
                     "c32e989f-5d8e-4b92-a00a-5ca1752a808d"
                     "b5047bfb-928d-4e9b-a194-4349d2777044"]
        selected-radio-value (r/atom "monthly")
        from (r/atom (t/local-date-time 2016 9 25))
        to (r/atom (ct/time-now))
        get-daily-data (fn []
                         (go
                           (let [v ((<! (async-get (str "http://data.appadhoc.com/apps/" (.-value (js/document.getElementById "appid"))
                                                        "/daily_uv?"
                                                        "from_hour=" (f/unparse (f/formatters :date-time) (t/to-utc-time-zone @from))
                                                        "&to_hour=" (f/unparse (f/formatters :date-time) (t/to-utc-time-zone @to)))
                                                   @authkey))
                                     "daily_uv")
                                 nums (map (fn [e] {(e "hour") (e "client_sum")}) v)]
                             (reset! result nums))))
        get-monthly-data (fn []
                           (go
                             (let [v ((<! (async-get (str "http://data.appadhoc.com/apps/" (.-value (js/document.getElementById "appid"))
                                                          "/monthly_uv?"
                                                          "from_hour=" (f/unparse (f/formatters :date-time) (t/to-utc-time-zone @from))
                                                          "&to_hour=" (f/unparse (f/formatters :date-time) (t/to-utc-time-zone @to)))
                                                     @authkey))
                                       "monthly_uv")
                                   nums (map (fn [e] {(e "hour") (e "client_sum")}) v)]
                               (reset! result nums))))
        ]
    (fn []
      [:div
       [:h2 "UV"]
       [AutoComplete {:id                "appid"
                      :dataSource        appidSource
                      :floatingLabelText "AppId"
                      :fullWidth         true}]

       [:p]

       [RadioButtonGroup {:defaultSelected @selected-radio-value
                          :onChange        (fn [event value]
                                             (reset! selected-radio-value value)
                                             (clear))}
        [RadioButton {:value "monthly" :label "Monthly"}]
        [RadioButton {:value "daily" :label "Daily"}]]

       [DatePicker {:defaultDate       @from
                    :onChange          (fn [_ date]
                                         (reset! from date)
                                         (clear))
                    :floatingLabelText "from"}]

       [DatePicker {:defaultDate       @to
                    :onChange          (fn [_ date]
                                         (reset! to date)
                                         (clear))
                    :floatingLabelText "to"}]

       [:p]

       [RaisedButton {:label     "GET DATA"
                      :secondary true
                      :on-click  (fn []
                                   (case @selected-radio-value
                                     "monthly" (get-monthly-data)
                                     "daily" (get-daily-data)))}]

       [Table {:selectable false}
        [TableHeader {:displaySelectAll  false
                      :adjustForCheckbox false}
         [TableRow
          [TableHeaderColumn "Date"]
          [TableHeaderColumn "Number"]]]
        [TableBody {:displayRowCheckbox false
                    :showRowHover       true}
         (for [r @result
               :let [d (first r)]]
           [TableRow
            [TableRowColumn (key d)]
            [TableRowColumn (val d)]])]]

       [debug @result]

       ])))

(defn data-fast-api []
  (let [authkey (subscribe [:authkey])
        result (r/atom nil)
        clear #(reset! result nil)
        from (r/atom (t/local-date-time 2016 9 25))
        to (r/atom (ct/time-now))
        appidSource ["ac66bf61-7608-4a5f-9bc4-9e7cf0f9694f"
                     "c32e989f-5d8e-4b92-a00a-5ca1752a808d"
                     "b5047bfb-928d-4e9b-a194-4349d2777044"]
        get-daily-data (fn []
                         (go
                           (let [v ((<! (async-get (str "http://data.appadhoc.com/apps/" (.-value (js/document.getElementById "appid"))
                                                        "/daily_api_count?"
                                                        "from_hour=" (f/unparse (f/formatters :date-time) (t/to-utc-time-zone @from))
                                                        "&to_hour=" (f/unparse (f/formatters :date-time) (t/to-utc-time-zone @to)))
                                                   @authkey))
                                     "daily_api_count")
                                 nums (let [v2 (map (fn [e] {(e "hour") (e "api_count")}) v)] #_(sum app exps api-count)
                                        (reduce (fn [acc e]
                                                  (let [[k v] (first e)]
                                                    (if (acc k)
                                                      (assoc acc k (+ v (acc k)))
                                                      (assoc acc k v))))
                                                (sorted-map)
                                                v2))
                                 nums (into [] nums)
                                 ]
                             (reset! result nums))))
        ]
    (fn []
      [:div
       [:h2 "API"]
       [AutoComplete {:id                "appid"
                      :dataSource        appidSource
                      :floatingLabelText "AppId"
                      :fullWidth         true}]
       [:p]
       [DatePicker {:defaultDate @from
                    :floatingLabelText "from"
                    :onChange (fn [_ date]
                                (reset! from date)
                                (clear))}]
       [DatePicker {:defaultDate @to
                    :floatingLabelText "to"
                    :onChange (fn [_ date]
                                (reset! to date)
                                (clear))}]
       [:p]
       [RaisedButton {:label "GET DATE"
                      :secondary true
                      :on-click get-daily-data}]
       [:p]
       ;[Table {:selectable false}
       ; [TableHeader {:displaySelectAll  false
       ;               :adjustForCheckbox false}
       ;  [TableRow
       ;   [TableHeaderColumn "Date"]
       ;   [TableHeaderColumn "Number"]]]
       ; [TableBody {:displayRowCheckbox false
       ;             :showRowHover       true}
       ;  (for [r @result
       ;        :let [d (first r)]]
       ;    [TableRow
       ;     [TableRowColumn (key d)]
       ;     [TableRowColumn (val d)]])]]
       [debug @result]])))

(defn main []
  (let [page (subscribe [:page])]
    (fn []
      [:div.main
       (case @page
         :default [default]
         :users [users-table]
         :apusers [apusers-table]
         :detail-user [detail-user-graph]
         :video [video]
         :testin-api-sum [testin-api-sum]
         :data-fast-uv [data-fast-uv]
         :data-fast-api [data-fast-api]
         :mytest [mytest]
         )])))

(defn main-panel []
  (let [username (subscribe [:username])]
    (fn []
      (if (nil? @username)
        [:div.container
         [:div.page-header [:h1 "ICS"]]
         [:div.jumbotron [login-in]]]

        [:div
         [AppBar {:title "ICS"}]

         [:table
          [:tr
           [:td {:width "300px"}
            [LeftNav
             [MenuItem {:primaryText "Default" :href "#/nav/default"}]
             [MenuItem {:primaryText "UV" :href "#/nav/data-fast-uv"}]
             [MenuItem {:primaryText "API" :href "#/nav/data-fast-api"}]
             [MenuItem {:primaryText "TESTIN-API" :href "#/nav/testin-api-sum"}]
             [MenuItem {:primaryText "VIDEO" :href "#/nav/video"}]
             [MenuItem {:primaryText "USERS" :href "#/nav/users"}]
             [MenuItem {:primaryText "AP-USERS" :href "#/nav/apusers"}]
             [MenuItem {:primaryText "MY-TEST" :href "#/nav/mytest"}]
             ]]
           [:td {:width "auto"}
            [main]]]]]
        ))))