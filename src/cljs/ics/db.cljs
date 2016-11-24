(ns ics.db)

(def default-db
  {
   :username         nil                                    ; "limenglong@appadhoc.com"
   :password         nil                                    ; "a1111111"
   :authkey          nil                                    ; "ebb001b6-d046-49c2-b7f7-ecad5856d4ea" ;; this is only for dev, notice it will expire of 7 days

   :page             :default
   :users            nil
   :apusers          nil
   :detail-user-info nil
   })
