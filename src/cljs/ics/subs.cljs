(ns ics.subs
    (:require-macros [reagent.ratom :refer [reaction]])
    (:require [re-frame.core :as re-frame :refer [reg-sub path]]))

(reg-sub
  :username
  (fn [db]
    (:username db)))

(reg-sub
  :select
  (fn [db]
    (:select db)))

(reg-sub
  :users
  (fn [db]
    (get (:users db) "users")))

(reg-sub
  :apusers
  (fn [db]
    (get (:apusers db) "applied_users")))

(reg-sub
  :current-pay-user
  (fn [db]
    (:current-pay-user db)))