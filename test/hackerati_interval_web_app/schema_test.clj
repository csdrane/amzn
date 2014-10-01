(ns hackerati-interval-web-app.scrape-test
  (:require [clojure.test :refer :all]
            [hackerati-interval-web-app.scrape :refer :all]
            [hackerati-interval-web-app.dbutility :refer :all]
            [korma.db :refer :all]
            [korma.core :refer :all]))

;;;;;;;;;;;;;;;;;;;; DEFINITIONS ;;;;;;;;;;;;;;;;;;;;

(def test-db "hackerati_test")

(defdb db (mysql {:host "127.0.0.1"
                  :db "hackerati-test"
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

;;;;;;;;;;;;;;;;;;;; TEST SETUP ;;;;;;;;;;;;;;;;;;;;

(if (database-exist? test-db)
  (do 
    (drop-database! test-db)
    (create-database! test-db))
  (create-database! test-db))
