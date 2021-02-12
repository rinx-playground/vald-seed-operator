(ns vald-seed-operator.controller
  (:require
   [clojure.edn :as edn]
   [clojure.core.async :as async :refer [<! >! <!! >!!]]
   [taoensso.timbre :as timbre])
  (:import
   [io.fabric8.kubernetes.client
    CustomResource]
   [io.javaoperatorsdk.operator
    ControllerUtils]
   [io.javaoperatorsdk.operator.api
    DeleteControl
    ResourceController
    UpdateControl]
   [io.javaoperatorsdk.operator.api.config
    ControllerConfiguration]
   [io.javaoperatorsdk.operator.config.runtime
    DefaultConfigurationService]
   [org.vdaas.vald.rinx.seeder
    Seeder
    SeederStatus]))

(defn dispatch-seeder [{:keys [in-ch out-ch] :as seeder} input]
  (>!! in-ch input)
  (when-some [result (<!! out-ch)]
    (try
      (let [value @result]
        (timbre/debugf "operation succeeded: %s" value)
        value)
      (catch Throwable e
        (timbre/warnf "An error occurred during operation, data: %s" e)
        e))))

(defn new [{:keys [client seeder]}]
  (reify
    ResourceController
    (createOrUpdateResource
      [this resource context]
      (try
        (let [ns (-> resource
                     (.getMetadata)
                     (.getNamespace))
              name (-> resource
                       (.getMetadata)
                       (.getName))
              spec (-> resource
                       (.getSpec))
              status (-> resource
                         (.getStatus))
              host (-> spec
                       (.getHost))
              port (-> spec
                       (.getPort))
              edn (edn/read-string
                    (or (-> spec
                            (.getEdn))
                        "{}"))]
          (timbre/debugf "Execution createOrUpdateResource for %s" name)
          (when (or (nil? status)
                    (not= (keyword (.getStatus status)) :ok))
            (timbre/debugf "Condition match for %s" name)
            (let [result (dispatch-seeder
                           seeder
                           {:host host
                            :port port
                            :op :insert
                            :edn edn})
                  status (if (instance? Throwable result)
                           (doto (SeederStatus.)
                             (.setHost host)
                             (.setPort port)
                             (.setStatus "ng")
                             (.setError (.toString result)))
                           (doto (SeederStatus.)
                             (.setHost host)
                             (.setPort port)
                             (.setStatus "ok")
                             (.setIds result)))]
              (-> resource
                  (.setStatus status)))
            (UpdateControl/updateCustomResource resource)))
        (catch Throwable e
          (timbre/errorf
            "An error occurred during execution of createOrUpdateResource: %s"
            e))))
    (deleteResource
      [this resource context]
      (try
        (let [ns (-> resource
                     (.getMetadata)
                     (.getNamespace))
              name (-> resource
                       (.getMetadata)
                       (.getName))
              spec (-> resource
                       (.getSpec))
              status (-> resource
                         (.getStatus))
              host (-> spec
                       (.getHost))
              port (-> spec
                       (.getPort))
              edn (edn/read-string
                    (or (-> spec
                            (.getEdn))
                        "{}"))]
          (timbre/debugf "Execution deleteResource for %s" name)
          (when (and status
                     (= (keyword (.getStatus status)) :ok))
            (timbre/debugf "Condition match for %s" name)
            (let [ids (vec (.getIds status))
                  result (dispatch-seeder
                           seeder
                           {:host host
                            :port port
                            :op :delete
                            :edn (assoc edn :ids ids)})]
              (when (instance? Throwable result)
                (timbre/warnf "deleting operation failed: %s" result))))
          (DeleteControl/DEFAULT_DELETE))
        (catch Throwable e
          (timbre/errorf
            "An error occurred during execution of deleteResource: %s"
            e))))))

(defn configuration-service []
  (proxy [DefaultConfigurationService] []
    (getConfigurationFor [controller]
      (reify
        ControllerConfiguration
        (getName [this]
          (ControllerUtils/getNameFor controller))
        (getCRDName [this]
          (CustomResource/getCRDName Seeder))
        (getFinalizer [this]
          (ControllerUtils/getDefaultFinalizerName
            (CustomResource/getCRDName Seeder)))
        (isGenerationAware [this]
          true)
        (getCustomResourceClass [this]
          Seeder)
        (getAssociatedControllerClassName [this]
          (-> controller
              (.getClass)
              (.getCanonicalName)))))))
