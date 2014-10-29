(ns hackerati-interval-web-app.views.views
  (:require [clojure.data.json :as json] 
            [hackerati-interval-web-app.views.template :as template]
            [hackerati-interval-web-app.schema :as db]
            [korma.core :as k]
            [hiccup.core :refer :all]
            [hiccup.def :refer :all]
            [hiccup.element :refer :all]
            [hiccup.form :refer :all]
            [hiccup.page :refer :all]
            [ring.util.anti-forgery :refer :all])
  (:use [ring.util.response :only [response]])
  (:import java.io.StringWriter))

(def ^:dynamic *debug-mode* false)

(defn debug [x]
  (if *debug-mode* (str x)))

(defn- get-username-from-request-map [request]
  {:pre [(or (contains? (request :session) :username)
             (contains? (request :params) :username))]
   :post [((comp not empty?) %)]}
  (if-let [username (-> request :session :username)]
    username
    (if-let [username (-> request :params :username)]
      username)))

;; TODO add message indicating successful operation; currently returns 404
(defn delete-link! [request]
  (let [{session :session} request
        {username :username} session
        {params :params} request
        {actionid :actionid} params]
    (if (db/authorized-link? {:username username :actionid actionid})
      (try  
        (do (db/delete-link! username actionid)
            (str "Delete link successful!"))
        (catch Exception e "Error: deletion failed!")))))

(defn link-view 
  "Display prices after clicking on tracked link"
  [request]
  (let [{session :session} request
        {username :username} session
        {params :params} request
        {productid :productid} params]
    (template/site-template
     (if (db/authorized-link? {:username username :productid productid}) 
       (html 
        [:h3 "Replace with :description"]
        [:div {:id "graphdiv" :productid productid}]
        [:div
         [:table {:class "table table-striped table-condensed"}
          [:thead
           [:tr [:th "Date"] [:th "Price"]]
           [:tbody
            (for [{date :date price :price} (db/get-prices productid)]
              [:tr [:td date] [:td price]])]]]] 
        [:a {:href "/" :class "small"} "back"] 
        (include-js "/dygraph-combined.js") 
        (include-js "/amzn-chart.js"))
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
             productid :productid
             actionid :actionid} 
            (db/get-links username)]
        [:tr {:id actionid}
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
     :body (template/site-template (html 
                           [:h3 "Links you're following, " (str username)] [:br] [:br]
                           (tracked-links-html username)
                           [:div {:class "small" :id "editing-fields"} 
                             [:a {:href "#" :class "add-links"} "add"] 
                             " " 
                             [:a {:href "#" :class "edit-links"} "edit"]] 
                           [:br]
                           [:div {:id "msg" :class "alert hide"}]
                           (debug request)))
     :session (assoc session :username username)}))

;; TODO
(defn valid-link? [link]
  true)

(defn new-link [request] 
  (let [{session :session} request
        {params :params} request
        {link :link} params
        {description :description} params
        {username :username} session]
    (if (valid-link? link)
      (try 
        (assoc (response {:success true 
                          :actionid (:generated_key 
                                     (db/add-link! 
                                      (db/get-user-id username) link description))})
          :headers {"Content-Type" "text/javascript;charset=UTF-8"})
        (catch Exception e (str "Add link failed! " e)))
      (str "Not a valid link!"))))

(defn input-field [n]
  [:input {:type "text" :name n}])

(defn input-field-with-label [n]
  [:p [:label (str n \:)] (input-field n)])

(defn password-field-with-label [n]
  [:p [:label (str n \:)] (password-field n)])

(defn not-logged-in [& {:keys [message] :or {message ""}}] 
  "View of site when not logged in. Accepts optional request parameter for use when debugging is enabled."
  (template/site-template
   (html [:div {:class "logged-out-container"}
          (form-to [:post "/login"]
                   (input-field-with-label "username") 
                   (password-field-with-label "password")
                   (submit-button "log in"))]
    [:br] [:br]
    [:div {:class "logged-out-container"} 
     (form-to [:post "/register"]
              (input-field-with-label "username")
              (password-field-with-label "password")
              (input-field-with-label "email")
              (anti-forgery-field)
              (submit-button "submit"))]
    [:br] [:br] 
    (if (seq message) 
      [:div {:class "alert alert-info"} message]))))

(defn index [request]
  (let [{session :session} request] 
    (if (logged-in? session)
      (if (db/user-exists? (session :username))
        (logged-in request))
      (not-logged-in))))

(defn login 
  [request]
  (let [{params :params} request
        {username :username} params  
        {password :password} params
        {session :session} request
        message "Error: username/password invalid!"]
    (if (db/valid-user? username password)
      (logged-in request)
      (not-logged-in :message message))))

(defn registration-failed []
  (->> [:h2 "Sorry, registration failed!"]
       template/site-template))

(defn user-exists []
  (->> [:h2 "Sorry, user exists!"]
       template/site-template))

(defn registration-successful []
  (->> [:h2 "Registration successful!"]
       template/site-template))

(defn attempt-register [{params :params}]
  (let [username (:username params)
       pw (:password params)
       email (:email params)]
   (if (db/user-exists? username)
     (user-exists)
    (do
      (db/add-user! username pw email)
      (registration-successful)))))
