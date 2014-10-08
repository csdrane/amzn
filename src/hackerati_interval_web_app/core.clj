; Run test server with `lein ring server-headless`
(ns hackerati-interval-web-app.core
  (:require  [compojure.core :refer :all]
             [compojure.handler :as handler]
             [compojure.route :as route]
             [hackerati-interval-web-app.views :as views]
             [ring.middleware.defaults :refer :all]
             [ring.middleware.session.cookie]))

(defroutes main-routes
  (GET "/" {session :session} (views/index session)) 
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

 
