(ns medegas.database
  (:require [datomic.api :as d]))

(def db-uri (System/getenv "DB_URL"))

(defn create-db [] (d/create-database db-uri))
(defn delete-db [] (d/delete-database db-uri))

#_(delete-db)
#_(create-db)

(defn conn [] (d/connect db-uri))

(def schema
  [{:db/ident :pitch/id
    :db/valueType :db.type/uuid
    :db/unique :db.unique/identity
    :db/cardinality :db.cardinality/one}
   {:db/ident :pitch/user
    :db/valueType :db.type/string
    :db/cardinality :db.cardinality/one
    :db/doc "id from chat"}
   {:db/ident :pitch/sound-id
    :db/valueType :db.type/string
    :db/cardinality :db.cardinality/one}
   {:db/ident :pitch/result
    :db/valueType :db.type/double
    :db/cardinality :db.cardinality/one
    :db/doc "result from pitch detection"}
   {:db/ident :pitch/type
    :db/valueType :db.type/keyword
    :db/cardinality :db.cardinality/one
    :db/doc ":calibration/full :calibration/empty :calibration/default"}])

(defn tx-schema []
  (d/transact (conn) schema))

(defn tx-pitch
  [data]
  (let [{:keys [type user id result]} data]
    (d/transact (conn) [{:pitch/result result
                       :pitch/type type
                       :pitch/user user
                       :pitch/id id}])))

(defn tx-pitch-type
  [data]
  (let [{:keys [id type]} data]
    (d/transact (conn) [{:pitch/id id
                       :pitch/type type}])))

(defn get-result
  [user]
  (d/q '[:find ?e ?result ?type
         :in $ ?user
         :where
         [?e :pitch/user ?user]
         [?e :pitch/result ?result]
         (or [?e :pitch/type ?type]
             [?e :pitch/type ?type])] (d/db (conn)) user))
