(ns hackerati-interval-web-app.views.template
  (:require [hiccup.def :refer :all]
            [hiccup.page :refer :all]))

(defn- load-css []
  (list
   (include-css "/bootstrap/css/bootstrap.min.css")
   (include-css "/bootstrap3-editable/css/bootstrap-editable.css")
   (include-css "/main.css")))

(defn- load-js []
  (list 
   (include-js "http://ajax.googleapis.com/ajax/libs/jquery/1.11.1/jquery.min.js")
   (include-js "/bootstrap/js/bootstrap.min.js")
   (include-js "/bootstrap3-editable/js/bootstrap-editable.min.js")
   (include-js "/main.js")))

(defhtml site-template
  "Takes hiccup html and wraps it in site-global template" 
  [h] 
  (html5 
   [:meta {:name "viewport" :content "width=device-width, initial-scale=1"}]
   (load-css)
   [:body
    [:div {:class "container-fixed"} 
     [:h1 "amzn scrpr"] h]]
   (load-js)))
