(ns hackerati-interval-web-app.views
  (:require [hackerati-interval-web-app.schema :as db]
            [korma.core :as k]
            [hiccup.core :refer :all]
            [hiccup.def :refer :all]
            [hiccup.element :refer :all]
            [hiccup.form :refer :all]
            [hiccup.page :refer :all]
            [ring.util.anti-forgery :refer :all]))

(def ^:dynamic *debug-mode* false)

(defn debug [x]
  (if *debug-mode* (str x)))

(defn- include-bootstrap []
  (list
   (include-css "/bootstrap/css/bootstrap.min.css")
   (include-js "http://ajax.googleapis.com/ajax/libs/jquery/1.11.1/jquery.min.js")
   (include-js "/bootstrap/js/bootstrap.min.js")))

(defn- get-username-from-request-map [request]
  {:pre [(or (contains? (request :session) :username)
             (contains? (request :params) :username))]
   :post [((comp not empty?) %)]}
  (if-let [username (-> request :session :username)]
    username
    (if-let [username (-> request :params :username)]
      username)))

(defhtml site-template
  "Takes hiccup html and wraps it in site-global template" 
  [h] 
  (html5 
   [:meta {:name "viewport" :content "width=device-width, initial-scale=1"}]
   (include-bootstrap)
   [:body
    [:div {:class "container"} 
     [:h1 "amzn scrpr"] h]]))

(defn- tracked-links-html [username]
  (html
   [:div  
    [:table {:class "table table-striped"}
     [:tbody 
      (for [url (db/get-links username)]
        [:tr
         [:td
          [:a {:href url} url]]])]]]))

(defn logged-in? [session]
  (contains? session :username))

(defn logged-in [request]
  (let [username (get-username-from-request-map request)
        {session :session} request]
    {:status 200
     :headers {"Content-Type" "text/html"}
     :body (site-template (html 
                           [:h3 "Links you're following, " (str username)] [:br] [:br]
                           (tracked-links-html username)
                           (debug request)))
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
  (if (logged-in? session)
      (if (db/user-exists? (session :username))
        (logged-in session))
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
