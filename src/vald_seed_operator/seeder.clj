(ns vald-seed-operator.seeder
  (:require
   [clojure.edn :as edn]
   [clojure.core.async :as async :refer [<! >! <!! >!!]]
   [com.stuartsierra.component :as component]
   [taoensso.timbre :as timbre]
   [jsonista.core :as json]
   [camel-snake-kebab.core :as csk]
   [vald-client-clj.core :as vald])
  (:import
    [java.util UUID Random]))


(def json-mapper
  (json/object-mapper
    {:encode-key-fn name
     :decode-key-fn csk/->kebab-case-keyword}))

(defn box [v]
  (reify
    clojure.lang.IDeref
    (deref [this]
      (if (instance? Throwable v)
        (throw v)
        v))))

(defn ->vectors [{:keys [type] :as source}]
  (case type
    :raw (get source :vectors)
    :file (let [{:keys [url format]} source
                file (slurp url)]
            (case format
              :edn (edn/read-string file)
              :json (json/read-value file json-mapper)
              []))
    :random (let [{:keys [dimension number]} source
                  generator (Random.)
                  rand (fn []
                         (-> generator
                             (.nextGaussian)))
                  generate (fn []
                             {:id (-> (UUID/randomUUID)
                                      (.toString))
                              :vector (vec
                                        (take dimension
                                              (repeatedly rand)))})]
              (vec
                (take number
                      (repeatedly generate))))
    []))

(defn insert [client {:keys [sources] :as edn}]
  (let [vectors (->> sources
                     (map ->vectors)
                     (flatten)
                     (vec))]
    (doall
     (map (fn [{:keys [id vector]}]
            (try
              (vald/insert client {} id vector)
              (catch Throwable e
                (let [cause (:cause (Throwable->map e))]
                  (if (re-matches #"^ALREADY_EXISTS.*" cause)
                    (vald/update client {} id vector)
                    e))))) vectors))
    (mapv :id vectors)))

(defn delete [client {:keys [ids] :as edn}]
  (timbre/debugf "try to delete ids: %s" ids)
  (doall
    (map #(vald/remove-id client {} %) ids))
  ids)

(defn seed [{:keys [host port op edn]}]
  (try
    (timbre/debugf "try to connect to %s:%d" host port)
    (let [client (vald/vald-client host port)
          result (case op
                   :delete (delete client edn)
                   (insert client edn))]
      (vald/close client)
      result)
    (catch Throwable e
      e)))

(defn start []
  (let [in-ch (async/chan 1)
        out-ch (async/chan 1)
        ax (fn [input ch]
             (async/go
               (->> (seed input)
                    (box)
                    (>! ch))
               (async/close! ch)))]
    (async/pipeline-async 4 out-ch ax in-ch)
    {:in-ch in-ch
     :out-ch out-ch}))

(defrecord SeederComponent [options]
  component/Lifecycle
  (start [this]
    (timbre/info "Starting seeder...")
    (let [seeder (start)]
      (timbre/info "Starting seeder completed.")
      (assoc this :seeder seeder)))
  (stop [this]
    (timbre/info "Stopping seeder...")
    (let [seeder (:seeder this)]
      (when seeder
        (timbre/info "stop")))
    (timbre/info "Stopping seeder completed.")
    (assoc this :seeder nil)))

(defn start-seeder [options]
  (map->SeederComponent
   {:options options}))
