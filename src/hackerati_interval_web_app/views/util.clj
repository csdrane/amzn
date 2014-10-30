(ns hackerati-interval-web-app.views.util
  (:require [clojure.string :refer [lower-case]]))

(defn valid-email? [e]
  (re-find #"^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,4}$" e))

(def l "www.amazon.com/gp/aw/d/0060512806/ref=mp_s_a_1_1?qid=1414698255&sr=8-1")

(defn valid-link? [lin]
  (re-find #"\bamazon[a-zA-Z/\\\.-?=_&]+\d{10}[a-zA-Z/\\\.-?=_&]*$" (lower-case lin)))
(valid-link? l)
(defn valid-username? [u]
  (re-find #"^\w{1,30}$" u))

