(ns simple.db
  (:require
   [clojure.tools.logging :as log]
   [integrant.core :as ig]
   [honeysql.format :as fmt]
   [honeysql.helpers :as helpers :refer [defhelper insert-into from limit sset where values]]
   [next.jdbc :as jdbc]
   [next.jdbc.connection :as connection]
   [next.jdbc.result-set :as rs])
  (:import
   [com.zaxxer.hikari HikariDataSource]))

(set! *warn-on-reflection* true)

(def datasource (atom nil))

(defmethod fmt/format-clause :on-conflict-do-nothing [[_ _] _]
  (str "ON CONFLICT DO NOTHING"))

(defhelper on-conflict-do-nothing [m _]
  (assoc m :on-conflict-do-nothing []))

(defn select
  [sql]
  (log/debugf "Executing JDBC [%s]." sql)
  (try
   (let [results (jdbc/execute-one! @datasource sql {:builder-fn rs/as-unqualified-lower-maps})]
     (when (not= nil results)
       (log/debugf "JDBC Results [%s]." results))
     results)
   (catch Exception e (log/error e))))

(defn select-many
  [sql]
  (log/debugf "Executing JDBC [%s]." sql)
  (try
   (jdbc/plan @datasource sql {:builder-fn rs/as-unqualified-lower-maps})
   (catch Exception e (log/error e))))

(defn execute!
  [sql]
  (log/debugf "Executing JDBC [%s]." sql)
  (try
   (let [results (jdbc/execute-one! @datasource sql)]
     (when (not= nil results)
       (log/debugf "JDBC Results [%s]." results)
       results))
   (catch Exception e (log/error e))))

(defn connection-pool-start
  [config]
  (let [ds (connection/->pool HikariDataSource config)]
    (reset! datasource ds)
    @datasource))

(defn connection-pool-stop
  []
  (.close @datasource))

(defmethod ig/init-key ::datasource [_ config]
  (connection-pool-start config))

(defmethod ig/halt-key! ::datasource [_ _]
  (connection-pool-stop))
