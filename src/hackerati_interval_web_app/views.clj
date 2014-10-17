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
   (include-js "/main.js")
   (include-css "/main.css")
   [:body
    [:div {:class "container-fixed"} 
     [:h1 "amzn scrpr"] h]]))

(defn link-view 
  "Display prices after clicking on tracked link"
  [request]
  (let [{session :session} request
        {username :username} session
        {params :params} request
        {productid :productid} params]
    (site-template
     (if (db/authorized-link? username productid) 
       (html
        [:div
         [:table {:class "table table-striped table-condensed"}
          [:thead
           [:tr [:th "Date"] [:th "Price"]]
           [:tbody
            (for [{date :date price :price} (db/get-prices productid)]
              [:tr [:td date] [:td price]])]]]] [:a {:href "/"} "back"])
       (html [:h1 "Not authorized for access!"])))))

(defn logged-in? [session]
  (contains? session :username))

(defn- tracked-links-html [username]
  (html
   [:div
    [:table {:class "table table-striped table-condensed" :id "links-table"}
     [:thead [:tr [:th "Link to Amazon"] [:th "Description"] [:th {:class "delete-button"}]]]
     [:tbody
      (for [{url :url 
             description :description
             productid :productid} 
            (db/get-links username)]
        [:tr
         [:td [:a {:href url} url]]
         [:td [:a {:href (str "link/" productid)} description]]
         [:td {:class "delete-button"} 
           [:button {:type "button" :class "close" 
                     :onclick "deleteRow(this)"} "&times;"]]])]]]))

;; TODO add third column that uses bootstrap's Close icon
(defn logged-in [request]
  (let [username (get-username-from-request-map request)
        {session :session} request]
    {:status 200
     :headers {"Content-Type" "text/html"}
     :body (site-template (html 
                           [:h3 "Links you're following, " (str username)] [:br] [:br]
                           (tracked-links-html username)
                           [:a {:href "" :class "edit-links"} "edit"] [:br]
                           (debug request)))
     :session (assoc session :username username)}))
 
(defn not-logged-in [request] 
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
    (html [:br] [:br] (debug request)))))

(defn index [request]
  (let [{session :session} request] 
    (if (logged-in? session)
      (if (db/user-exists? (session :username))
        (logged-in request))
      (not-logged-in request))))

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
