(ns ics.db)

(def default-db
  {
   :username    "limenglong@appadhoc.com"
   ;:username nil
   :password    "a1111111"
   :authkey     "fe62abea-ba7a-47f3-a68a-ac2672140b29" ;; this is only for dev, notice it will expire of 7 days

   :page        :default
   :users       nil
   :apusers     nil

   :detail-user nil
   })
