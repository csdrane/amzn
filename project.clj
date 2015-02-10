(defproject amzn "0.1.0-SNAPSHOT"
  :description "Amazon.com price tracker"
  :url "https://github.com/csdrane/amzn"
  :license {:name "MIT License"
            :url "http://opensource.org/licenses/MIT"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/java.jdbc "0.3.5"]
                 [org.clojure/data.csv "0.1.2"]
                 [org.clojure/data.json "0.2.5"]
                 [compojure "1.2.1"]
                 [crypto-password "0.1.3"]
                 [environ "1.0.0"]
                 [hiccup "1.0.5"]
                 [korma "0.4.0"] 
                 [mysql/mysql-connector-java "5.1.32"]
                 [ring "1.3.1"]
                 [ring/ring-defaults "0.1.1"]
                 [ring/ring-json "0.3.1"]]
  :plugins [[lein-ring "0.8.13"]]
  :ring {:handler amzn.core/app :port 3000 :main amzn.core}
  :uberjar-name "amzn.jar"
  :main ^:skip-aot amzn.core
  :min-lein-version "2.0.0"
  :aot [amzn.core]
  :target-path "target/%s"
  :resource-paths ["resources"]
  :profiles {:uberjar {:aot :all}
             :dev {:plugins [[cider/cider-nrepl "0.8.0-SNAPSHOT"]]}})
