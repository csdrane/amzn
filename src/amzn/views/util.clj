(ns amzn.views.util
  (:require [clojure.string :refer [lower-case]]))

(defn valid-email? [e]
  (re-find #"^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,4}$" e))

(defn valid-link? [link]
  "Returns vector if successful of [link amzn-productid]"
  (re-find #"[a-z:/.-]+amazon[a-z/.-]+([a-z0-9]{10})[a-z/=_?&-]*" (lower-case link)))

(defn process-link [link]
  (if-let [valid-link (valid-link? link)] 
    (let [mobile-stub "http://www.amazon.com/gp/aw/d/"
          productid (second valid-link)]
      (str mobile-stub productid))))

(defn valid-username? [u]
  (re-find #"^\w{1,30}$" u))

