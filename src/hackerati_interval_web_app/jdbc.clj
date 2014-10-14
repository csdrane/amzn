(ns hackerati-interval-web-app.jdbc
  (:require [clojure.java.jdbc :refer :all])
  (:require [hackerati-interval-web-app.dbutility :as util]))

(def db-spec {:classname "mysql-connector-java"
              :subprotocol "mysql"
              :subname "//127.0.0.1:3306/hackerati"
              :user "project"
              :password "project"})

(defn create-users-table [db]
  (db-do-commands db
                  (create-table-ddl :users
                                    [:userid :integer "PRIMARY KEY" "AUTO_INCREMENT"]
                                    [:username "varchar(32)" "UNIQUE"]
                                    [:password "char(60)"] ; bcrypt hash
                                    [:email "varchar(255)" "UNIQUE"])))

(defn create-tracked-links-table [db]
  (db-do-commands db (create-table-ddl :trackedlinks
                                            [:userid :integer "references users (userid)"]
                                            [:actionid :integer "PRIMARY KEY" "AUTO_INCREMENT"]
                                            [:productid :integer "references products (productid)"]
                                            ;; TODO add length validation/sanitization on input
                                            [:description "varchar(100)"]))) 

;; URL datatype based on http://stackoverflow.com/questions/219569/best-database-field-type-for-a-url
(defn create-products-table [db]
  (db-do-commands db (create-table-ddl :products
                                            [:productid :integer "PRIMARY KEY" "AUTO_INCREMENT"]
                                            [:url "varchar(2083)"])))
;; TODO add unique (date, productid) constraint
(defn create-prices-table [db]
  (db-do-commands db (create-table-ddl :prices
                                            [:priceid :integer "PRIMARY KEY" "AUTO_INCREMENT"]
                                            [:productid :integer "references products (productid)"]
                                            [:date "date"]
                                            [:price "decimal(7,2), CONSTRAINT UNIQUE KEY `uc_price` (`productid`, `date`)" ])))

