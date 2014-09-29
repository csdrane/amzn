(ns hackerati-interval-web-app.views
  (:require [hackerati-interval-web-app.schema :as db]
            [korma.core :as k]
            [hiccup.core :refer :all]
            [hiccup.form :refer :all]
            [ring.util.anti-forgery :refer :all]))

(defn index [session]
  (str session)
  #_(if (db/valid-user? (:username session) (:pw session))
    (logged-in session)
    (not-logged-in session)))

(defn site-template
  "Takes hiccup html and wraps it in site-global template" 
  [h]
  (html 
   [:html 
    [:body
     [:h1 "amzn scrpr"] h]]))

(defn logged-in [session]
  (->> [:h3 "Links you're following."]
       ;; TODO get-links
       site-template))

(defn not-logged-in [] 
  (site-template
   (html 
    (form-to [:post "/login"]
              "username:" [:input {:type "text" :name "username"}] [:br]
              "password:" (password-field "password") [:br]
              (submit-button "log in"))
     (html [:br] [:br])
     (form-to [:post "/submit"]
              "username:" [:input {:type "text" :name "username"}] [:br]
              "password:" (password-field "password") [:br]
              "email:" [:input {:type "text" :name "email"}] [:br]
              (anti-forgery-field)
              (submit-button "submit")))))

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
