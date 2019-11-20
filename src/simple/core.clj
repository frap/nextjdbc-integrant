(ns simple.core
  (:require
   [aero.core :refer [reader read-config]]
   [clojure.java.io :as io]
   [clojure.tools.logging :as log]
   [integrant.core :as ig])
  (:gen-class))

(set! *warn-on-reflection* true)

(Thread/setDefaultUncaughtExceptionHandler
 (reify Thread$UncaughtExceptionHandler
   (uncaughtException [_ thread ex]
     (log/errorf "Exception non interceptée sur [%s]." (.getName thread))
     (log/errorf ex))))

(defn my-spy
  [message]
  (log/info message))

(add-tap my-spy)

(def system nil)

(let [lock (Object.)]
  (defn load-namespaces
    [system-config]
    (locking lock
      (ig/load-namespaces system-config))))

(defn config
  [{:keys [filename] :as opts}]
  (-> (io/resource (or filename "config/config.edn"))
      (read-config opts)))

(defn system-config
  [opts]
  (let [config (config opts)
        system-config (:ig/system config)]
    (load-namespaces system-config)
    (ig/prep system-config)))

(defn -main
  [profile]
  (let [system-config (system-config {:profile (or (keyword profile) :local)})
        sys (ig/init system-config)]
    (.addShutdownHook
     (Runtime/getRuntime)
     (Thread.
      (fn []
        (ig/halt! sys))))
    (alter-var-root #'system (constantly sys)))
  @(promise))

;; aero needs to know how to intepret integrant references (ig/ref)
(defmethod reader 'ig/ref [_ _ value]
  (ig/ref value))

(comment

 (require
  '[integrant.repl :as ir]
  '[integrant.repl.state :refer [system]]
  '[integrant.core :as ig]
  '[simple.core :as core])
 (ir/set-prep! #(core/system-config {:profile :local :filename "config/config.edn"}))

 #_+)
