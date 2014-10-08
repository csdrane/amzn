(defproject hackerati-interval-web-app "0.1.0-SNAPSHOT"
  :description "Unnamed Interval Web App for Hackerati Application"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/java.jdbc "0.3.5"]
                 [compojure "1.1.9"]
                 [crypto-password "0.1.3"]
                 [hiccup "1.0.5"]
                 [korma "0.4.0"] 
                 [mysql/mysql-connector-java "5.1.32"]
                 [ring "1.3.1"]
                 [ring/ring-defaults "0.1.1"]]
  :plugins [[lein-ring "0.8.11"]]
  :ring {:handler hackerati-interval-web-app.core/app}
  :uberjar "amzn.jar"
  :main ^:skip-aot hackerati-interval-web-app.core
  :target-path "target/%s"
  :resource-paths ["resources"]
  :profiles {:uberjar {:aot :all}
             :dev {:plugins [[cider/cider-nrepl "0.7.0"]]}})
