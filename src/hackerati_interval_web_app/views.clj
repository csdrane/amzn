(ns hackerati-interval-web-app.views
  (:require [hackerati-interval-web-app.schema :as db]
            [korma.core :as k]
            [hiccup.core :refer :all]
            [hiccup.form :refer :all]
            [hiccup.element :refer :all]
            [ring.util.anti-forgery :refer :all]))

(def ^:dynamic *debug-mode* true)

(defn debug [x]
  (if *debug-mode* (str x)))

(defn site-template
  "Takes hiccup html and wraps it in site-global template" 
  [h]
  (html 
   [:html 
    [:body
     [:h1 "amzn scrpr"] h]]))

(defn logged-in [request]
  (let [{params :params} request
        {username :username} params
        {session :session} request]
      ;; TODO get-links
      {:status 200
       :headers {"Content-Type" "text/html"}
       :body (site-template (html [:h3 "Links you're following, " (:username session)] [:br] [:br]
                                  #_(db/get-links )
                                  (debug session)))
       :session (assoc session :username username)}))

(defn not-logged-in [session] 
  (site-template
   (html 
    (form-to [:post "/login"]
              "username:" [:input {:type "text" :name "username"}] [:br]
              "password:" (password-field "password") [:br]
              (submit-button "log in"))
     (html [:br] [:br])
     (form-to [:post "/register"]
              "username:" [:input {:type "text" :name "username"}] [:br]
              "password:" (password-field "password") [:br]
              "email:" [:input {:type "text" :name "email"}] [:br]
              (anti-forgery-field)
              (submit-button "submit"))
     (html [:br] [:br] (debug session)))))

(defn index [session]
  (html (debug session) (link-to session "/login" "log-in" ))
#_(if (db/user-exists? (session :username))
    (logged-in session)
    (not-logged-in session)))

(defn login 
  [request]
  (let [{params :params} request
        {username :username} params  
        {password :password} params
        {session :session} request]
    (if (db/valid-user? username password)
      (logged-in request)
      (not-logged-in))))

(defn registration-failed []
  (->> [:h2 "Sorry, registration failed!"]
       site-template))

(defn user-exists []
  (->> [:h2 "Sorry, user exists!"]
       site-template))

(defn registration-successful []
  (->> [:h2 "Registration successful!"]
       site-template))

(defn attempt-register [{params :params}]
 (let [username (:username params)
       pw (:password params)
       email (:email params)]
   (if (db/user-exists? username)
     (user-exists)
    (do
      (db/add-user! username pw email)
      (registration-successful)))))

(defn session-test [request]
  (let [{session :session} request] 
    {:status 200
     :headers {"Content-Type" "text/html"}
     :body (str request)
     :session (assoc session :counter 1)}))
