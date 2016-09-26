(ns ics.subs
    (:require-macros [reagent.ratom :refer [reaction]])
    (:require [re-frame.core :as re-frame :refer [reg-sub path]]))

(reg-sub
  :username
  (fn [db]
    (:username db)))

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
  :detail-user
  (fn [db]
    (:detail-user db)))