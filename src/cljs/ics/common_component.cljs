(ns ics.common-component
  (:require [cljs-time.format :as cf]
            [cljs-time.core :as ct]))

(defn parse-date [date] (cf/parse (cf/formatter "yyyy-MM-dd") date))
(defn unparse-date [date] (cf/unparse (cf/formatter "yyyy-MM-dd") date))
(defn today [] (unparse-date (ct/now)))
(defn offset-today [n] (unparse-date (ct/plus (ct/now) (ct/days n))))
(defn yesterday [] (offset-today -1))

(defn date-component [value #_"value is a r/atom"]
  [:input {:type "date"
           :value @value
           :on-change (fn [e]
                        (reset! value (-> e .-target .-value)))}])