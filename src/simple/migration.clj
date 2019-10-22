(ns simple.migration
  (:require
   [integrant.core :as ig])
  (:import
   [org.flywaydb.core Flyway]
   [org.flywaydb.core.api.configuration FluentConfiguration]))

(set! *warn-on-reflection* true)

(defn flyway
  [datasource migration-locations]
  (Flyway. (doto
             (FluentConfiguration.)
             (.dataSource datasource)
             (.locations (into-array migration-locations)))))

(defn migrate
  [datasource migration-locations]
  (.migrate (flyway datasource migration-locations)))

(defmethod ig/init-key ::migration [_ {:keys [datasource migration-locations]}]
  (migrate datasource migration-locations)
  {:datasource datasource :migration-locations migration-locations})
