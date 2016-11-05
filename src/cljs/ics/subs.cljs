(ns ics.subs
    (:require-macros [reagent.ratom :refer [reaction]])
    (:require [re-frame.core :as re-frame :refer [reg-sub path]]))

(reg-sub
  :username
  (fn [db]
    (:username db)))

(reg-sub
  :authkey
  (fn [db]
    (:authkey db)))

(reg-sub
  :page
  (fn [db]
    (:page db)))

(reg-sub
  :users
  (fn [db]
    (:users db)))

(reg-sub
  :apusers
  (fn [db]
    (:apusers db)))

(reg-sub
  :detail-user-info
  (fn [db]
    (:detail-user-info db)))