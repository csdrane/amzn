(ns amzn.dbutility
  (:require [clojure.java.jdbc :refer :all]))

(def db-spec {:classname "mysql-connector-java"
              :subprotocol "mysql"
              :subname "//127.0.0.1:3306/"
              :user "project"
              :password "project"})

(defn drop-database!
  [name]
  (if-let [^java.sql.Connection con (get-connection db-spec)]
    (with-open [stmt (.createStatement con)]
      (.addBatch stmt (str "drop database " name))
      (seq (.executeBatch stmt)))
    false))

(defn create-database!  
  [name]
  (if-let [^java.sql.Connection con (get-connection db-spec)]
    (with-open [stmt (.createStatement con)]
      (.addBatch stmt (str "create database " name))
      (seq (.executeBatch stmt)))
    false))

(defn database-exist? 
  [name]
  (query db-spec (str "SELECT SCHEMA_NAME FROM INFORMATION_SCHEMA.SCHEMATA WHERE SCHEMA_NAME = " "'" name "'")))

