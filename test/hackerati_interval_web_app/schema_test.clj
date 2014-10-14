(ns hackerati-interval-web-app.schema-test
  (:require [clojure.test :refer :all]
            [hackerati-interval-web-app.schema :as db]
            [hackerati-interval-web-app.jdbc :as jdbc]
            [hackerati-interval-web-app.dbutility :as dbutil]
            [korma.db :as kdb]
            [korma.core :as kcore]))

;;;;;;;;;;;;;;;;;;;; DEFINITIONS ;;;;;;;;;;;;;;;;;;;;

(def db-host "127.0.0.1")
(def test-db "hackerati_test")
(def db-user "project")
(def db-pw "project")

(kdb/defdb korma-dbdef (kdb/mysql
                        {:host db-host
                         :db "hackerati_test"
                         :user db-user
                         :password db-pw}))

(def jdbc-dbspec {:classname "mysql-connector-java"
                  :subprotocol "mysql"
                  :subname (str "//" db-host ":3306/" test-db)
                  :user db-user
                  :password db-pw})

;;;;;;;;;;;;;;;;;;;; TEST DB SETUP ;;;;;;;;;;;;;;;;;;;;

(if (dbutil/database-exist? test-db)
  (do 
    (dbutil/drop-database! test-db)
    (dbutil/create-database! test-db))
  (dbutil/create-database! test-db))

(jdbc/create-users-table jdbc-dbspec)
(jdbc/create-tracked-links-table jdbc-dbspec)
(jdbc/create-products-table jdbc-dbspec)
(jdbc/create-prices-table jdbc-dbspec)

;;;;;;;;;;;;;;;;;;;; ADD DATA ;;;;;;;;;;;;;;;;;;;;

(db/add-user! "chris" "chris-pw" "foo@baz.com")
(db/add-user! "nick" "nick-pw" "baz@bar.com")
(db/add-user! "andy" "andy-pw" "blick@blat.net")

(let [urls [["http://foo.com" "Description 1"] 
             ["http://baz.com" "Description 2"] 
             ["http://bar.com" "Description 3"]]
        userid (map db/get-user-id ["chris" "nick" "andy"])]
  (doseq [links (mapv (comp flatten vector) userid urls)] 
    (apply db/add-link! links)))

;;;;;;;;;;;;;;;;;;;; TESTS  ;;;;;;;;;;;;;;;;;;;;

(deftest select-user-names-test
  (is (= (map #(get % :username) 
              (kcore/select db/users))
         ["chris" "nick" "andy"])))


;;;; TODO add test case to return all links
(deftest get-links-test
  (is (= (db/get-links "chris")
         (list {:url "http://foo.com" :description "Description 1"}))))

(deftest user-exists?-test
  (is (some? (db/user-exists? "chris")))
  (is (empty? (db/user-exists? "chrsi"))))

(deftest valid-user?-test 
  (is (every? true? (map (partial apply db/valid-user?) 
      [["chris" "chris-pw"] 
       ["nick" "nick-pw"] 
       ["andy" "andy-pw"]]))))


