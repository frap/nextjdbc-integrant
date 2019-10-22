(ns simple.operations
  (:require
   [clojure.tools.logging :as log]
   [honeysql.core :as sql]
   [honeysql.helpers :as helpers :refer [insert-into values select from where limit]]
   [integrant.core :as ig]
   [simple.db :as db]))

(defn insert-a-row
  []
  (let [{:next.jdbc/keys [update-count]} (db/execute! (-> (insert-into :simple)
                                                          (values [{:firstname "foo"}])
                                                          (db/on-conflict-do-nothing)
                                                          sql/format))]
    (when (> update-count 0)
      (log/debugf "Inserted some records."))))

(defn select-a-row
  []
  (db/select (-> (select :id :firstname)
                 (from :simple)
                 (where [:= :firstname "foo"])
                 (limit 1)
                 sql/format)))

(defmethod ig/init-key ::operations [_ _]
  (log/info "Initialised."))

(comment

  (require
   '[simple.operations :as o])

  (o/insert-a-row)

  (o/select-a-row)

 #_+)
