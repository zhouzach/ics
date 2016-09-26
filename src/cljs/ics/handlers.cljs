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
  :detail-user
  (fn [db [_ value]]
    (assoc db :detail-user value)))

(reg-event-db
  :page
  (fn [db [_ value]]
    (case value
      :users (GET
               "http://auth.appadhoc.com/users"
               {:headers {"Auth-Key" (:authkey db)}
                :handler #(dispatch [:users (% "users")])})
      :apusers (GET
                 "http://auth.appadhoc.com/applied_users"
                 {:headers {"Auth-Key" (:authkey db)}
                  :handler #(dispatch [:apusers (% "applied_users")])})
      nil)
    (assoc db :page value)))

(reg-event-db
  :user
  (fn [db [_ id]]
    (println "de7:" (:authkey db))
    (GET
      (str "http://auth.appadhoc.com/user/" id)
      {:headers {"Auth-Key" (:authkey db)}
       :handler #(dispatch [:detail-user %])})
    (assoc db :page :user)))
