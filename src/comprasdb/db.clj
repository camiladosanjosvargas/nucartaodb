(ns comprasdb.db
  (:use clojure.pprint)
  (:require [datomic.api :as d]
            [comprasdb.db :as db]
            [comprasdb.modelo :as model]))

(def db-uri "datomic:dev://localhost:4334/comprasdb")

(defn abre-conexao []
  (d/create-database db-uri)
  (d/connect db-uri))

;(abre-conexao)
(def conn (db/abre-conexao))

(defn apaga-banco []
  (d/delete-database db-uri))

(def schema-cartao [{:db/ident       :compras/cartao
                     :db/valueType   :db.type/bigint
                     :db/cardinality :db.cardinality/one}
                    {:db/ident       :compras/cliente
                     :db/valueType   :db.type/bigint
                     :db/cardinality :db.cardinality/one}
                    {:db/ident       :compras/numero
                     :db/valueType   :db.type/string
                     :db/cardinality :db.cardinality/one}
                    {:db/ident       :compras/cvv
                     :db/valueType   :db.type/string
                     :db/cardinality :db.cardinality/one}
                    {:db/ident       :compras/validade
                     :db/valueType   :db.type/instant
                     :db/cardinality :db.cardinality/one}
                    {:db/ident       :compras/limite
                     :db/valueType   :db.type/bigdec
                     :db/cardinality :db.cardinality/one}])

(def schema-compra [
                    ;Compras
                    {:db/ident       :compras/id
                     :db/valueType   :db.type/uuid
                     :db/cardinality :db.cardinality/one
                     :db/unique      :db.unique/identity}
                    {:db/ident     :compras/cartao
                     :db/valueType :db.type/bigint}
                    {:db/ident       :compras/data
                     :db/valueType   :db.type/instant
                     :db/cardinality :db.cardinality/one}
                    {:db/ident       :compras/valor
                     :db/valueType   :db.type/bigdec
                     :db/cardinality :db.cardinality/one}
                    {:db/ident       :compras/estabelecimento
                     :db/valueType   :db.type/string
                     :db/cardinality :db.cardinality/one}
                    {:db/ident       :compras/categoria
                     :db/valueType   :db.type/string
                     :db/cardinality :db.cardinality/one}
                    {:db/ident        :compras/categoria1
                     :db/valueType   :db.type/ref
                     :db/cardinality :db.cardinality/one}

                    ;categorias
                    {:db/ident       :categoria/nome
                     :db/valueType   :db.type/string
                     :db/cardinality :db.cardinality/one}
                    {:db/ident       :categoria/id
                     :db/valueType   :db.type/uuid
                     :db/cardinality :db.cardinality/one
                     :db/unique      :db.unique/identity}])

(defn cria-schema [conn]
  (d/transact conn schema-cartao)
  (d/transact conn schema-compra))




