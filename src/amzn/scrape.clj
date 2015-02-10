(ns amzn.scrape
  (:require [clojure.string :as str])
  (:gen-class))

(def ^:dynamic *user-agent*
  "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_2) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/36.0.1944.0 Safari/537.36")

(def ^:dynamic *accept* "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8")

(def ^:dynamic *accept-encoding* "")

(def ^:dynamic *accept-language* "en-US,en;q=0.8")

;; Below is used for debugging HTTP request using netcat.
;; e.g. `nc -k -l 80` 
;; (def ^:dynamic *product-url* "http://localhost")

(def price-regex 
  "Amazon inconsistently includes List Price before Price.
  This regex avoids List Price."
  #"(?s).*?<b>Price.*?(\$\d+\.?\d*).*")

(defn fetch-url [url]
  (with-open [inputstream  
              (-> (java.net.URL. url)
                  .openConnection
                  (doto (.setRequestProperty "Accept" *accept*)
                    (.setRequestProperty "User-Agent" *user-agent*)
                    (.setRequestProperty "Accept-Encoding" *accept-encoding*)
                    (.setRequestProperty "Accept-Language" *accept-language*))
                  .getContent)]
    (slurp inputstream)))

(defn get-price-from-url
  "URL should be from Amazon's mobile website
  http://www.amazon.com/gp/aw/h.html"
  [url]
  (let [parse-$ #(if (= (first %1) \$)
                   (Float/parseFloat (apply str (rest %1)))
                   (Float/parseFloat %1))] 
    (try (->> url 
              fetch-url
              (re-matches price-regex) 
              second
              parse-$)
         (catch Exception e (str "Warning: couldn't not fetch price for " url)))))

(defn get-price-from-urls
  "URL should be from Amazon's mobile website
  http://www.amazon.com/gp/aw/h.html"
  [& urls]
  (do 
    #_(println urls) 
    (apply pmap get-price-from-url urls)))

(defn get-price-from-file 
  "File should be HTML from Amazon's mobile website
  http://www.amazon.com/gp/aw/h.html"
  [file]
  (second (with-open [rdr (clojure.java.io/reader file)]          
            (re-find price-regex (slurp rdr)))))

(defn mobile-url? [url]
  (if (re-matches #".*amazon\.\w+/gp/aw/.*" url)
    true
    false))

(defn get-mobile-url 
  "Returns equivalent mobile URL given non-mobile URL."
  [url]
  (let [product-id (->> url 
                        (re-matches #".*(product|dp)/([\w\d]+).*")
                        last)
        url-template "http://www.amazon.com/gp/aw/d/"]
    (str url-template product-id)))

