(ns hackerati-interval-web-app.views.views
  (:require [clojure.data.json :as json]
            [hackerati-interval-web-app.views.template :as template]
            [hackerati-interval-web-app.views.util :as util]
            [hackerati-interval-web-app.schema :as db]
            [korma.core :as k]
            [hiccup.core :refer :all]
            [hiccup.def :refer :all]
            [hiccup.element :refer :all]
            [hiccup.form :refer :all]
            [hiccup.page :refer :all]
            [hiccup.util :refer :all]
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

(defn add-link! [username link description]
  (if (util/valid-link? link)
    (try
      (assoc (response {:success true
                        :actionid (:generated_key
                                   (db/add-link!
                                    (db/get-user-id username)
                                    link (escape-html description)))})
        :headers {"Content-Type" "text/javascript;charset=UTF-8"})
      (catch Exception e (str "Add link failed! " e)))
    (assoc (response {:success false, :msg "<strong>Error: Not a valid link!</strong>"})
      :status 500)))

;; TODO add message indicating successful operation; currently returns 404
(defn delete-link! [username actionid]
  (if (db/authorized-link? {:username username :actionid actionid})
    (try
      (do (db/delete-link! username actionid)
          (str "Delete link successful!"))
      (catch Exception e "Error: deletion failed!"))))

(defn edit-link! [username actionid description]
  (if (db/authorized-link? {:username username :actionid actionid})
    (try
      (do
        (db/edit-link! actionid (escape-html description))
        (str "{success: true}"))
      (catch Exception e "Error: deletion failed"))))

(defn link-view
  "Display prices after clicking on tracked link"
  [username productid]
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
     (html [:h1 "Not authorized for access!"]))))

(defn logged-in? [username]
  (some username))

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
         [:td [:a {:href (str "link/" productid) :class "description-editable"
                   :data-url "/editlink" :data-pk actionid} description]]
         [:td {:class "delete-button"}
           [:button {:type "button" :class "close"
                     :onclick "deleteRow(this)"} "&times;"]]])]]]))

(defn logged-in [username]
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body (template/site-template (html
                                  [:h3 "Links you're following, " (str username)] [:br] [:br]
                                  (tracked-links-html username)
                                  [:div {:class "small" :id "editing-fields"}
                                   [:a {:href "#" :id "add-links"} "add"]
                                   " "
                                   [:a {:href "#" :id "edit-links"} "edit"]]
                                  [:br]
                                  [:div {:id "msg" :class "alert hide"}]))})

(defn logged-out [message]
  "View of site when not logged in. Accepts optional request parameter for use when debugging is enabled."
  (template/site-template
   (html [:div {:class "logged-out-container"}
          (form-to [:post "/login"]
                   (template/input-field-with-label "username")
                   (template/password-field-with-label "password")
                   (anti-forgery-field)
                   (submit-button "log in"))]
    [:br] [:br]
    [:div {:class "logged-out-container"}
     (form-to [:post "/register"]
              (template/input-field-with-label "username")
              (template/password-field-with-label "password")
              (template/input-field-with-label "email")
              (anti-forgery-field)
              (submit-button "submit"))]
    [:br] [:br]
    (if (seq message)
      [:div {:class "alert alert-info"} message]))))

(defn index [username]
  (if (and username (db/exists? :username username))
    (logged-in username)
    (logged-out nil)))

(defn login [username password]
  (let [message "Error: username/password invalid!"]
    (if (db/valid-user? username password)
      {:response-map (logged-in username) :username username}
      (logged-out (str message "username: " username "password: " password)))))

(defn email-exists []
  (logged-out :message "Sorry, email already exists!"))

(defn invalid-email []
  (logged-out :message "Invalid email address! Please try again."))

(defn invalid-username []
  (logged-out :message "Invalid username! Please try again."))

(defn registration-failed []
  (logged-out :message "Sorry, registration failed!"))

(defn registration-successful []
  (logged-out :message "Registration successful!"))

(defn user-exists []
  (logged-out :message "Sorry, user exists!"))

(defn attempt-register [username pw email]
  (cond
   ((comp not util/valid-username?) username) (invalid-username)
   ((comp not util/valid-email?) email) (invalid-email)
   (db/exists? :username username) (user-exists)
   (db/exists? :email email) (email-exists)
   :else (do
           (db/add-user! username pw email)
           (registration-successful))))
