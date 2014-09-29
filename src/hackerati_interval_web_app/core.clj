; Run test server with `lein ring server-headless`
(ns hackerati-interval-web-app.core
  (:require  [cider.nrepl :refer (cider-nrepl-handler)]
             [hackerati-interval-web-app.views :as views]
             [ring.middleware.defaults :refer :all]
             [compojure.core :refer :all]
             [compojure.handler :as handler]
             [compojure.route :as route]))

(defroutes main-routes
  (GET "/" session (views/index session)) 
  (POST "/submit" session (views/attempt-register session))
  (GET "/temp" []  (views/not-logged-in))
  (route/not-found "<h1>Page not found</h1>"))

;; Add back anti-forgery when site goes live.
(def app
  (->> (assoc-in site-defaults [:security :anti-forgery] false)  
       (wrap-defaults (handler/site main-routes))))
