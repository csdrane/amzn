(ns hackerati-interval-web-app.schema
  (:require [hackerati-interval-web-app.scrape :as scrape]
            [korma.db :refer :all]
            [korma.core :refer :all]
            [crypto.password.bcrypt :as password]))

;;;;;;;;;;;;;;;;;;;; DEFINITIONS ;;;;;;;;;;;;;;;;;;;;

(defdb db (mysql {:host "127.0.0.1"
                  :db "hackerati"
                  :user "project"
                  :password "project"}))

(declare user tracked-links products productid url prices dates)

(defentity users
  (pk :userid)
  (entity-fields :userid :username :password :email)
  (has-many tracked-links {:fk :userid}))

(defentity tracked-links
  (table :trackedlinks)
  (pk :actionid)
  (entity-fields :userid :actionid :productid)
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
(defn add-link! [userid url]
  (let [productid ((insert products (values {:url url})) :generated_key)]
    (insert tracked-links (values {:userid userid :productid productid}))))

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

(defn get-user-id [username]
  (->> (select users
               (where {:username username}))
       first
       :userid))

(defn get-links
  ([] 
     (->> (select products) 
          (map :url)))
  ([username]
     (->> (select users 
                  (fields [:products.url]) 
                  (join [tracked-links :trackedlinks] {:users.userid :trackedlinks.userid}) 
                  (join [products :products] {:products.productid :trackedlinks.productid}) 
                  (where {:username username}))
          (map :url))))

(defn get-user-pw [username]
  (->> (select users
               (where {:username username}))
       first
       :password))

;; TODO refactor
(defn refresh-prices []
  (let [products (select products)
        ids (map :productid products)
        urls (map :url products)
        ps (scrape/get-price-from-urls urls)
        m (reduce #(conj %1 (assoc {} :productid (first %2) :price (second %2)))  [] (partition 2 (interleave ids ps)))]
;; TODO optimize by including all values within one insert statement, instead of mapping across the bunch
;; Although, one advantage of doing it this way is that one error doesn't ruin the whole insertion.
   #_(println m)
    (doseq [entry m]
      #_(println entry)
      (add-price! entry))))

(defn user-exists? [username]
  (seq (select users (where {:username username}))))

(defn valid-user? [username password]
  (password/check password (get-user-pw username)))
