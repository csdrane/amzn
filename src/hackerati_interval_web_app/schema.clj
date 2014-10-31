(ns hackerati-interval-web-app.schema
  (:require [environ.core :refer :all]
            [hackerati-interval-web-app.scrape :as scrape]
            [korma.db :refer :all]
            [korma.core :refer :all]
            [crypto.password.bcrypt :as password]))

;;;;;;;;;;;;;;;;;;;; DEFINITIONS ;;;;;;;;;;;;;;;;;;;;

(defdb db (mysql {:host (env :db-host)
                  :db (env :db-name)
                  :user (env :db-user)
                  :password (env :db-password)}))

(declare user tracked-links products productid url prices dates)

(defentity users
  (pk :userid)
  (entity-fields :userid :username :password :email)
  (has-many tracked-links {:fk :userid}))

(defentity tracked-links
  (table :trackedlinks)
  (pk :actionid)
  (entity-fields :userid :actionid :productid :description)
  (belongs-to users {:fk :userid})
  (has-one products {:fk :productid}))

(defentity products 
  (pk :productid)
  (entity-fields :productid :url)
  (has-many tracked-links {:fk :productid}))

(defentity prices
  (pk :priceid)
  (belongs-to products {:fk :productid})
  (entity-fields :priceid :productid :date :price))

;;;;;;;;;;;;;;;;;;;; FUNCTIONS ;;;;;;;;;;;;;;;;;;;;

;; TODO duplicate URLs should just not create multiple productids,
;; only multiple actionids
(defn add-link!
  ([userid url]
     (add-link! userid url "Description not provided."))
  ([userid url description]
     (let [productid ((insert products (values {:url url})) :generated_key)]
       (insert tracked-links (values {:userid userid :productid productid :description description})))))

(defn add-price! 
  ([{productid :productid price :price}] 
     (add-price! productid (java.util.Date.) price))
  ([productid date price]
     (try 
       (insert prices
               (values {:productid productid
                        :date date
                        :price price}))
       (catch com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException e "Error: date already exists!")
       (catch Exception e "Warning: unidentified error " (.getMessage e))))) 

(defn add-user! [username pw email]
  (insert users
          (values  {:username username
                    :password (password/encrypt pw)
                    :email email})))

;; TODO
;; (defn delete-user [username])

;; TODO
(defn authorized-link?
  "Returns true if username and actionid exist together in DB. Eventually want to replace username with userid." 
  [auth-map]
  true)

(defn delete-link! 
  [username actionid]
  (let [link-map (first (select tracked-links
                                (join [users :users] {:users.userid :trackedlinks.userid})
                                (where {:actionid actionid})))
        user-map (first (select users 
                                (where {:username username})))]
    (if (apply = (map :userid [link-map user-map]))
      (delete tracked-links 
              (where {:actionid actionid}))
      (println "Deletion failed: not authorized! " link-map user-map))))

(defn get-user-id [username]
  (->> (select users
               (where {:username username}))
       first
       :userid))

;; Would like to refactor the redundancy from below using an if statement on whether a username is present. Was having trouble getting Korma to accept this though.
(defn get-links
  ([]
     (->> (select users 
                  (fields :products.url :products.productid :trackedlinks.description :trackedlinks.actionid) 
                  (join [tracked-links :trackedlinks] {:users.userid :trackedlinks.userid}) 
                  (join [products :products] {:products.productid :trackedlinks.productid}))
          (map #(select-keys % [:url :description :productid :actionid]))))
  ([username]
     (->> (select users 
                  (fields :products.url :products.productid :trackedlinks.description :trackedlinks.actionid) 
                  (join [tracked-links :trackedlinks] {:users.userid :trackedlinks.userid}) 
                  (join [products :products] {:products.productid :trackedlinks.productid}) 
                  (where {:username username}))
          (map #(select-keys % [:url :description :productid :actionid])))))

(defn get-prices [productid]
  (->> (select prices
               (where {:productid productid}))
       (map #(select-keys % [:date :price]))))

(defn get-user-pw [username]
  (->> (select users
               (where {:username username}))
       first
       :password))

(defn refresh-prices []
  (try 
    (let [products (select products)
         agents (map #(agent %) (range (count products)))]
     (doseq [a agents]
       (send-off a (fn [_] (hash-map :productid (:productid (nth products @a)) :price (scrape/get-price-from-url (:url (nth products @a))))))) 
     (doseq [a agents]
       (await-for 4000 a))
     (doseq [a agents]
       (.println System/out @a))
     (doseq [a agents]
       (add-price! @a)))
    (catch Exception e (str "Error refreshing prices: " e))))

(defn exists? [k v]
  "Takes a key and a value. Returns true if key value pair exists in users table."
  (seq (select users (where {k v}))))

(defn valid-user? [username password]
  (if (exists? :username username)
      (password/check password (get-user-pw username))))
