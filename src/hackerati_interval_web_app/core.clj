; Run test server with `lein ring server-headless`
; Run -main with `lein run 3000`
; If lein run is crashing, you are probably forgetting the port number.
(ns hackerati-interval-web-app.core
  (:require  [compojure.core :refer :all]
             [compojure.route :as route]
             [hackerati-interval-web-app.views.csv :as csv]
             [hackerati-interval-web-app.views.views :as views]
             [hackerati-interval-web-app.schema :as db]
             [hackerati-interval-web-app.util :refer :all]
             [ring.adapter.jetty :as jetty]
             [ring.middleware.defaults :refer :all]
             [ring.middleware.session.cookie])
  (:use [ring.middleware.json :only [wrap-json-response]])
  (:import (java.util.concurrent ScheduledThreadPoolExecutor TimeUnit))
  (:gen-class))

(defroutes POST-routes
  (POST "/login" {{username :username} :params
                  {password :password} :params}
    ((fn [v] (assoc (:response-map v)
               :session {:username (:username v)}))
     (views/login username password)))
  (POST "/newlink" {{username :username} :session
                    {link :link} :params
                    {description :description} :params}
    (views/add-link! username link description))
  (POST "/register" {{username :username} :params
                     {password :password} :params
                     {email :email} :params}
    (views/attempt-register username password email))
  (POST "/delete-link" {{username :username} :session
                        {actionid :actionid} :params}
    (views/delete-link! username actionid))
  (POST "/editlink" {{username :username} :session
                     {actionid :pk} :params
                     {description :value} :params}
    (views/edit-link! username actionid description)))

(defroutes GET-routes
  (GET "/link/:productid" {{username :username} :session
                           {productid :productid} :params}
    (views/link-view username productid))
  (GET "/csv/:productid" {{productid :productid} :params} (csv/chart-csv productid))
  (GET "/" {{:keys [username]} :session} (views/index username))
  (GET "/login" {{:keys [message]} :request} (views/logged-out message)))

(defroutes main-routes
  GET-routes
  POST-routes
  (route/resources "/bootstrap")
  (route/not-found "<h1>Page not found</h1>"))

;; TODO Add back anti-forgery when site goes live.
;; TODO replace cookie key
(def app
  (wrap-json-response
   (wrap-defaults
    main-routes
    (-> (assoc-in site-defaults [:security :anti-forgery] false)
        (assoc-in [:store] (ring.middleware.session.cookie/cookie-store {:key "a 16-byte secret"}))))
   :pretty))

(defn- scheduled-task [f]
  (-> (ScheduledThreadPoolExecutor. 10)
      (.scheduleAtFixedRate f 0 24 TimeUnit/HOURS)))

;; Scheduled task must go first, otherwise won't be executed.
(defn -main [& [port]]
  (if-let [port (try
                  (Integer/parseInt port)
                  (catch Exception e))]
    (do
      (System/setProperty "java.awt.headless" "true")
      (scheduled-task db/refresh-prices)
      (stay-alive 1)
      (jetty/run-jetty app
                       {:port port :auto-reload? true}))
    (println "Useage: lein run port")))
