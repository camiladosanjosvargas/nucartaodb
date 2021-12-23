(ns comprasdb.core
  (:use [clojure pprint])
  (:require [datomic.api :as d]
            [comprasdb.db :as db]
            [comprasdb.modelo :as model]
            [comprasdb.adiconar-no-bd :as add]
            [comprasdb.core :as c]
            [java-time :as j]))

(def conn (db/abre-conexao))

(db/cria-schema conn)

(def educacao (model/nova-categoria "Educação"))
(def saude (model/nova-categoria "Saúde"))
(def beleza (model/nova-categoria "Beleza"))

(pprint @(d/transact conn [educacao, saude, beleza]))

(defn uuid [] (java.util.UUID/randomUUID))

(def cliente-1 (model/novo-cliente (uuid) "Maria" "12346434567" "maria@gmail.com"))
(def cliente-2 (model/novo-cliente (uuid) "Carlos" "12346434567" "carlos@gmail.com"))
(def cliente-3 (model/novo-cliente (uuid) "Paula" "12346434567" "paula@gmail.com"))
(def cliente-4 (model/novo-cliente (uuid) "Joao" "12346434567" "joao@gmail.com"))
(d/transact conn [cliente-1, cliente-2, cliente-3, cliente-4])


(def cartao-10 (model/novo-cartao (uuid) 10N "908976" "989" (j/sql-timestamp (j/local-date-time)) 100M))
(def cartao-50 (model/novo-cartao (uuid) 50N "7646378" "546" (j/sql-timestamp (j/local-date-time)) 100M))
(def cartao-20 (model/novo-cartao (uuid) 20N "4563575" "764" (j/sql-timestamp (j/local-date-time)) 100M))
(def cartao-30 (model/novo-cartao (uuid) 30N "753567" "643" (j/sql-timestamp (j/local-date-time)) 100M))

(d/transact conn [cartao-10, cartao-50, cartao-20, cartao-30])

(def compra-10-escola (model/nova-compra (uuid) (j/sql-timestamp (j/local-date-time)) 80M "EscolaABC"))
(def compra-10-farmacia (model/nova-compra (uuid) (j/sql-timestamp (j/local-date-time)) 5M "FarmaciaABC"))
(def compra-20-escola (model/nova-compra (uuid) (j/sql-timestamp (j/local-date-time)) 50M "EscolaABC"))
(def compra-20-escola-abc (model/nova-compra (uuid) (j/sql-timestamp (j/local-date-time)) 50M "EscolaABC"))
(def compra-30-salao (model/nova-compra (uuid) (j/sql-timestamp (j/local-date-time)) 30M "SalaoABC"))
(def compra-30-educacao (model/nova-compra (uuid) (j/sql-timestamp (j/local-date-time)) 40M "EscolaABC"))

(d/transact conn [compra-10-escola compra-10-farmacia compra-20-escola compra-20-escola-abc compra-30-salao compra-30-educacao])

(defn db-adds-de-atribuicao-de-cliente [cartao cliente]
  [[:db/add
    [:cartao/id (:cartao/id cartao)]
    :cartao/cliente
    [:cliente/id (:cliente/id cliente)]]])

(defn atribui-cliente [conn cartao cliente]
  (let [a-transacionar (db-adds-de-atribuicao-de-cliente cartao cliente)]
    (d/transact conn a-transacionar)))

(atribui-cliente conn cartao-10 cliente-1)
(atribui-cliente conn cartao-50 cliente-2)
(atribui-cliente conn cartao-20 cliente-3)
(atribui-cliente conn cartao-30 cliente-4)


(defn db-adds-de-atribuicao-de-cartao [compra cartao]
  [[:db/add
    [:compras/id (:compras/id compra)]
    :compras/cartao
    [:cartao/id (:cartao/id cartao)]]])

(defn atribui-cartao [conn compra cartao]
  (let [a-transacionar (db-adds-de-atribuicao-de-cartao compra cartao)]
    (d/transact conn a-transacionar)))


(atribui-cartao conn compra-10-escola cartao-10)
(atribui-cartao conn compra-10-farmacia cartao-10)
(atribui-cartao conn compra-20-escola cartao-20)
(atribui-cartao conn compra-20-escola-abc cartao-20)
(atribui-cartao conn compra-30-salao cartao-30)
(atribui-cartao conn compra-30-educacao cartao-30)


(defn atribui-categorias [conn detalhes categoria]
  (let [a-transacionar (reduce (fn[db-adds compra] (conj db-adds [:db/add
                                                                  [:compras/id (:compras/id compra)]
                                                                  :compras/categoria
                                                                  [:categoria/id (:categoria/id categoria)]]))
                               []
                               detalhes)]
    (d/transact conn a-transacionar)))


(atribui-categorias conn [compra-10-escola, compra-20-escola, compra-20-escola-abc compra-30-educacao] educacao)
(atribui-categorias conn [compra-10-farmacia] saude)
(atribui-categorias conn [compra-30-salao] beleza)


(def db (d/db conn))


(defn todas-as-categorias [db]
  (d/q '[:find (pull ?categoria [*])
         :where [?categoria :categoria/id]] db))

(defn todas-as-compras [db]
  (d/q '[:find (pull ?entidade [*])
         :where [?entidade :compras/id]] db))

(defn todas-os-clientes [db]
  (d/q '[:find (pull ?entidade [*])
         :where [?entidade :cliente/id]] db))

(defn todas-os-cartoes [db]
  (d/q '[:find (pull ?entidade [*])
         :where [?entidade :cartao/id]] db))

(defn relatorio-gasto-total-por-categoria [db]
  (d/q '[:find ?nome (sum ?valor)
         :keys categoria valor-total
         :with ?compra
         :where [?compra :compras/valor ?valor]
         [?compra :compras/categoria ?categoria]
         [?categoria :categoria/nome ?nome]] db))

(defn uma-compra [db compras-id]
  (d/pull db '[*] [:compras/id compras-id]))

(defn relatorio-por-cartao [db cartao]
  (d/q '[:find (pull ?cartao [:cartao/cartao]) ?nome (sum ?valor) (pull ?compra [*])
         :keys cartao categoria total-de-gastos-por-categoria compra-realizada
         :in $ ?cartao-procurado
         :where [?cartao :cartao/cartao ?cartao-procurado]
         [?compra compras/cartao ?cartao]
         [?compra :compras/valor ?valor]
         [?compra :compras/categoria ?categoria]
         [?categoria :categoria/nome ?nome]] db cartao))

;consultas
(todas-as-compras db)
(todas-os-cartoes db)
(todas-os-clientes db)

;consulta que retorna uma compra dado um id
(uma-compra db (:compras/id compra-10-escola))

;consulta que retorna todas as categorias
(todas-as-categorias db)

;consulta que retorna o gasto total por categoria
(relatorio-gasto-total-por-categoria db)

;consulta que retorna relatório de compras por cliente
(relatorio-por-cartao db 10N)


;(db/apaga-banco)

