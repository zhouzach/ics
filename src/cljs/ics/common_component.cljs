(ns ics.common-component
  (:require [cljs-time.format :as cf]
            [cljs-time.core :as ct]))

(defn parse-date [date] (cf/parse (cf/formatter "yyyy-MM-dd") date))
(defn unparse-date [date] (cf/unparse (cf/formatter "yyyy-MM-dd") date))
(defn today [] (unparse-date (ct/now)))
(defn offset-today [n] (unparse-date (ct/plus (ct/now) (ct/days n))))
(defn yesterday [] (offset-today -1))

;; value is a r/atom
(defn date-component [value]
  [:input {:type "date"
           :value @value
           :on-change (fn [e]
                        (reset! value (-> e .-target .-value)))}])