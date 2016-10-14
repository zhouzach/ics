(ns ics.handlers
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [cljs-http.client :as http]
            [cljs.core.async :refer [chan <! >! put! take!]]
            [re-frame.core :refer [reg-event-db path reg-sub dispatch subscribe]]
            [ajax.core :refer [GET POST]]
            [ics.db :as db]))

(reg-event-db
  :initialize-db
  (fn [_ _]
    db/default-db))

;;
(reg-event-db
  :login-success
  (fn [db [_ username password authkey]]
    (assoc db :username username :password password :authkey authkey)))

(reg-event-db
  :login-fail
  (fn [db _]
    (assoc db :login-fail true)))

(reg-event-db
  :login
  (fn [db [_ username password]]
    (.log js/console "event login: " "username=" username " password=" password)
    (GET
      "http://auth.appadhoc.com/auth/login"
      {:params  {:email username :password password}
       :handler (fn [response]
                  (.log js/console "response: " response)
                  (let [[prefix authkey] (.-arr response)]
                    (case prefix
                      "auth_key" (dispatch [:login-success username password authkey])
                      "error_code" (do
                                     (js/alert "Invalid username or password !")
                                     (dispatch [:login-fail])))))})
    db))

;;
(reg-event-db
  :users
  (fn [db [_ value]]
    (assoc db :users value)))

(reg-event-db
  :apusers
  (fn [db [_ value]]
    (assoc db :apusers value)))

(reg-event-db
  :detail-user
  (fn [db [_ value]]
    (assoc db :detail-user value)))

(reg-event-db
  :testin-api-sum
  (fn [db [_ value]]
    (assoc db :testin-api-sum value)))

;; no depend, make request respectively
(reg-event-db
  :page
  (fn [db [_ value]]
    (let [authkey (:authkey db)]
      (case value
        :users (GET
                 "http://auth.appadhoc.com/users"
                 {:headers {"Auth-Key" authkey}
                  :handler #(dispatch [:users (% "users")])})
        :apusers (GET
                   "http://auth.appadhoc.com/applied_users"
                   {:headers {"Auth-Key" authkey}
                    :handler #(dispatch [:apusers (% "applied_users")])})
        nil))
    (assoc db :page value)))



;; how to write sync code?
;(defn sync-get
;  [url authkey]
;  (let [r (atom nil)]
;    (GET url {:headers {"Auth-Key" authkey}
;              :handler (fn [res] (reset! r res))})
;    @r))



;(reg-event-db
;  :acc-testin-api
;  (fn [db [_ from_hour to_hour]]
;    (let [authkey (:authkey db)]
;      (let [users ((sync-get "http://auth.appadhoc.com/users" authkey) "users")
;            _ (print "d1:" (count users))
;            testin-users (filter #(= "testin" (% "third_party_from")) users)
;            testin-users-ids (set (map #(% "id") testin-users))
;            apps ((sync-get "http://auth.appadhoc.com/apps" authkey) "apps")
;            testin-apps (filter #(contains? testin-users-ids (% "author_id")) apps)
;            testin-apps-ids (map #(% "id") testin-apps)
;            all (atom 0)]
;        (doseq [appid testin-apps-ids]
;          (let [v ((sync-get (str "http://data.appadhoc.com/apps/" appid
;                                  "/daily_api_count?"
;                                  "from_hour=" from_hour
;                                  "&to_hour=" to_hour)
;                             authkey)
;                    "daily_api_count")
;                nums (map #(% "api_count" 0) v)
;                sum (reduce + nums)]
;            (swap! all #(+ % sum))
;            (dispatch [:testin-api-sum @all])))))
;    db))

(reg-event-db
  :user
  (fn [db [_ id]]
    (GET
      (str "http://auth.appadhoc.com/user/" id)
      {:headers {"Auth-Key" (:authkey db)}
       :handler #(dispatch [:detail-user %])})
    (assoc db :page :user)))
