(ns vald-seed-operator.system
  (:require
   [com.stuartsierra.component :as component]
   [taoensso.timbre :as timbre]
   [vald-seed-operator.seeder :as seeder]
   [vald-seed-operator.server :as server]
   [vald-seed-operator.operator :as operator]))

(defn system [{:keys [liveness operator seeder readiness] :as conf}]
  (component/system-map
   :liveness (server/start-server liveness)
   :seeder (component/using
              (seeder/start-seeder seeder)
              {:liveness :liveness})
   :operator (component/using
              (operator/start-operator operator)
              {:seeder :seeder})
   :readiness (component/using
               (server/start-server readiness)
               {:operator :operator})))
