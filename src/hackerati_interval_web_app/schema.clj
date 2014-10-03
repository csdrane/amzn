(ns hackerati-interval-web-app.schema
  (:require [korma.db :refer :all]
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

(defn get-links [username]
  (->> (select users 
               (fields [:products.url]) 
               (join [tracked-links :trackedlinks] {:users.userid :trackedlinks.userid}) 
               (join [products :products] {:products.productid :trackedlinks.productid}) 
               (where {:username username}))
       (map :url)))

(defn get-user-pw [username]
  (->> (select users
               (where {:username username}))
       first
       :password))

(defn user-exists? [username]
  (seq (select users (where {:username username}))))

(defn valid-user? [username password]
  (password/check password (get-user-pw username)))
