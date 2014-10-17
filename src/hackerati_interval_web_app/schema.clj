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

;; TODO refactor
;; Using ids and ps to respectively only represent productids and prices makes this function fragile. If a get-price fails it will corrupt the results for successive entries.
(defn refresh-prices []
  (let [products (select products)
        ids (map :productid products)
        urls (map :url products)
        ps (apply scrape/get-price-from-urls urls)
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
