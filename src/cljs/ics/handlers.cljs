(ns ics.handlers
  (:require [re-frame.core :refer [reg-event-db path reg-sub dispatch subscribe]]
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
  :select
  (fn [db [_ select]]
    (case select
      :default nil
      :users (GET
               "http://auth.appadhoc.com/users"
               {:headers {"Auth-Key" (:authkey db)}
                :handler #(dispatch [:users %])})
      :apusers (GET
                 "http://auth.appadhoc.com/applied_users"
                 {:headers {"Auth-Key" (:authkey db)}
                  :handler #(dispatch [:apusers %])})
      :paydetails nil
      :about nil)
    (assoc db :select select)))

(reg-event-db
  :paydetails
  (fn [db [_ email]]
    (let [users (get (:users db) "users")]
      (assoc db :current-pay-user
                (first
                  (filter
                    #(= email (get % "email"))
                    users))))))