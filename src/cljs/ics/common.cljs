(ns ics.common
  (:require [cljs-time.coerce :as c]
            [cljs-time.format :as f]
            [cljs-time.core :as t]))

(defn validate-email
  [email]
  (let [pattern #"[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?"]
    (and (string? email) (re-matches pattern email))))

(defn sec-to-date [sec]
  (t/to-default-time-zone
    (c/from-long (* sec 1000))))

(defn date-to-format [date]
  (f/unparse (f/formatters :year-month-day) date))

(defn sec-to-format [sec]
  (date-to-format (sec-to-date sec)))