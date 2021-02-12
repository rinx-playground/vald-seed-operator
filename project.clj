(defproject vald-seed-operator "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.10.2"]
                 [org.clojure/core.async "1.3.610"]
                 [com.taoensso/timbre "5.1.0"]
                 [com.stuartsierra/component "1.0.0"]
                 [http-kit "2.5.1"]
                 [compojure "1.6.2"]
                 [io.javaoperatorsdk/operator-framework "1.7.0"]
                 [org.bouncycastle/bcpkix-jdk15on "1.68"]
                 [io.grpc/grpc-okhttp "1.35.0" :exclusions [io.grpc/grpc-core]]
                 [vald-client-clj "v1.0.1"]]
  :java-source-paths ["src/java"]
  :repl-options {:init-ns vald-seed-operator.core}
  :profiles {:uberjar {:aot :all
                       :main vald-seed-operator.core}})
