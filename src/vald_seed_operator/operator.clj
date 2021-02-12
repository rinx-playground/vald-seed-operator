(ns vald-seed-operator.operator
  (:require
   [com.stuartsierra.component :as component]
   [taoensso.timbre :as timbre]
   [vald-seed-operator.controller :as controller])
  (:import
   [io.fabric8.kubernetes.client
    ConfigBuilder
    DefaultKubernetesClient]
   [io.javaoperatorsdk.operator
    Operator]))

(defn start [seeder]
  (let [config (-> (ConfigBuilder.)
                   (.withNamespace nil)
                   (.build))
        client (DefaultKubernetesClient. config)
        operator (Operator. client (controller/configuration-service))]
    (-> operator
        (.register (controller/new {:client client
                                    :seeder seeder})))
    operator))

(defrecord OperatorComponent [options seeder]
  component/Lifecycle
  (start [this]
    (let [operator-name (:name options)
          seeder (:seeder seeder)]
      (timbre/infof "Starting operator: %s..." operator-name)
      (let [operator (start seeder)]
        (timbre/infof "Starting operator %s completed." operator-name)
        (assoc this :operator operator))))
  (stop [this]
    (let [operator-name (:name options)]
      (timbre/infof "Stopping operator %s..." operator-name)
      (let [operator (:operator this)]
        (when operator
          (timbre/infof "stop")))
      (timbre/infof "Stopping operator %s completed." operator-name)
      (assoc this :operator nil))))

(defn start-operator [options]
  (map->OperatorComponent
   {:options options}))
