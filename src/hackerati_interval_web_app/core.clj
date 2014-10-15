; Run test server with `lein ring server-headless`
(ns hackerati-interval-web-app.core
  (:require  [compojure.core :refer :all]
             [compojure.handler :as handler]
             [compojure.route :as route]
             [hackerati-interval-web-app.views :as views]
             [hackerati-interval-web-app.schema :as db]
             [ring.adapter.jetty :as jetty]
             [ring.middleware.defaults :refer :all]
             [ring.middleware.session.cookie])
  (:import (java.util.concurrent ScheduledThreadPoolExecutor TimeUnit)) 
  (:gen-class))

(defroutes main-routes
  (GET "/" request (views/index request))
  (GET "/link/:productid" request (views/link-view request))
  (GET "/login" {session :session}  (views/not-logged-in session))
  (POST "/login" request (views/login request))
  (POST "/register" request (views/attempt-register request))
  (GET "/test" request (views/session-test request))
  (route/resources "/bootstrap")
  (route/not-found "<h1>Page not found</h1>"))

;; TODO Add back anti-forgery when site goes live.
;; TODO replace cookie key
(def app
  (wrap-defaults 
   (handler/site main-routes)
   (-> (assoc-in site-defaults [:security :anti-forgery] false)  
       (assoc-in [:store] (ring.middleware.session.cookie/cookie-store {:key "a 16-byte secret"})))))

(defn- scheduled-task [f]
  (-> (ScheduledThreadPoolExecutor. 10)
      (.scheduleAtFixedRate f 0 24 TimeUnit/HOURS)))

;; Scheduled task must go first, otherwise won't get executed.
(defn -main [& [port]]
  (scheduled-task db/refresh-prices)
  (jetty/run-jetty app 
                   {:port (if port 
                            (Integer/parseInt port)
                            (Integer/parseInt (System/getenv "PORT")))}))
