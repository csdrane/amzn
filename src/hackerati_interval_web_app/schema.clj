(ns hackerati-interval-web-app.schema
  (:require [korma.db :refer :all]
            [korma.core :refer :all]
            [crypto.password.bcrypt :as password]))

(defdb db (mysql {:host "127.0.0.1"
                  :db "hackerati"
                  :user "project"
                  :password "project"}))

(declare user tracked-links products productid url prices dates)

(defentity users
  (pk :userid)
  (entity-fields :username :password :email)
  (has-many tracked-links))

(defentity tracked-links
  (table :trackedlinks)
  (pk :actionid)
  (belongs-to users)
  (has-one products))

(defentity products 
  (pk :productid)
  (entity-fields :url)
  (has-many tracked-links))

(defentity prices
  (pk :priceid)
  (belongs-to products)
  (entity-fields :date :price))

(defn add-user! [username pw email]
  (insert users
          (values  {:username username
                    :password (password/encrypt pw)
                    :email email})))

;; TODO delete?
;; Might not be necessary
;; (defn get-user-id [username]
;;   (->> (select users
;;                (where {:username username}))
;;        first
;;        :userid))

;; TODO work in progress
(defn get-links [username]
  (select tracked-links
          (with users)
          (join products.)
          (where {:username username})
          (fields :url)))

(defn get-user-pw [username]
  (->> (select users
               (where {:username username}))
       first
       :password))

(defn user-exists? [username]
  (seq (select users (where {:username username}))))

(defn valid-user? [username password]
  (password/check password (get-user-pw username)))

;; TODO
;; (defn delete-user [username])

