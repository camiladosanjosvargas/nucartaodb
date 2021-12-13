(ns comprasdb.core
  (:use [clojure pprint])
  (:require [datomic.api :as d]
            [comprasdb.db :as db]
            [comprasdb.modelo :as model]
            [comprasdb.adiconar-no-bd :as add]
            [comprasdb.util :as n.u]
            [comprasdb.core :as c]
            [java-time :as j]))

(def formata-com-duas-casas-decimais n.u/formata-com-duas-casas-decimais)

(def conn (db/abre-conexao))

(db/cria-schema conn)

(def educacao (model/nova-categoria "Educação"))
(def saude (model/nova-categoria "Saúde"))
(def beleza (model/nova-categoria "Beleza"))

(pprint @(d/transact conn [educacao, saude, beleza]))

(let [cartao1 (model/novo-cartao 10N 1N "908976" "989" (j/sql-timestamp (j/local-date-time)) 100M)
      cartao2 (model/novo-cartao 50N 1N "7646378" "546" (j/sql-timestamp (j/local-date-time)) 100M)
      cartao3 (model/novo-cartao 20N 2N "4563575" "764" (j/sql-timestamp (j/local-date-time)) 100M)
      cartao4 (model/novo-cartao 30N 3N "753567" "643" (j/sql-timestamp (j/local-date-time)) 100M)
      cartao5 (model/novo-cartao 30N 3N "845354" "123" (j/sql-timestamp (j/local-date-time)) 100M)]
  (d/transact conn [cartao1, cartao2, cartao3, cartao4, cartao5]))

(defn uuid [] (java.util.UUID/randomUUID))

(def compra-10-escola (model/nova-compra (uuid) 10N (j/sql-timestamp (j/local-date-time)) 80M "EscolaABC" "Educação"))
(def compra-10-farmacia (model/nova-compra (uuid) 10N (j/sql-timestamp (j/local-date-time)) 5M "FarmaciaABC" "Saúde"))
(def compra-20-escola (model/nova-compra (uuid) 20N (j/sql-timestamp (j/local-date-time)) 50M "EscolaABC" "Educação"))
(def compra-20-escola-abc (model/nova-compra (uuid) 20N (j/sql-timestamp (j/local-date-time)) 50M "EscolaABC" "Educação"))
(def compra-30-salao (model/nova-compra (uuid) 30N (j/sql-timestamp (j/local-date-time)) 30M "SalaoABC" "Beleza"))
(def compra-30-educacao (model/nova-compra (uuid) 30N (j/sql-timestamp (j/local-date-time)) 40M "EscolaABC" "Educação"))

(d/transact conn [compra-10-escola compra-10-farmacia compra-20-escola compra-20-escola-abc compra-30-salao compra-30-educacao])


(pprint (d/transact conn [[:db/add [:compras/id (:compras/id compra-10-escola)]
                           :compras/categoria1
                           [:categoria/id (:categoria/id educacao)]]]))
(pprint (d/transact conn [[:db/add [:compras/id (:compras/id compra-10-farmacia)]
                           :compras/categoria1
                           [:categoria/id (:categoria/id saude)]]]))
(pprint (d/transact conn [[:db/add [:compras/id (:compras/id compra-20-escola)]
                           :compras/categoria1
                           [:categoria/id (:categoria/id educacao)]]]))
(pprint (d/transact conn [[:db/add [:compras/id (:compras/id compra-20-escola-abc)]
                           :compras/categoria1
                           [:categoria/id (:categoria/id educacao)]]]))
(pprint (d/transact conn [[:db/add [:compras/id (:compras/id compra-30-salao)]
                           :compras/categoria1
                           [:categoria/id (:categoria/id beleza)]]]))
(pprint (d/transact conn [[:db/add [:compras/id (:compras/id compra-30-educacao)]
                           :compras/categoria1
                           [:categoria/id (:categoria/id educacao)]]]))

(def db (d/db conn))

(defn todos-os-registros [db]
  (d/q '[:find ?entidade
         :where [?entidade :compras/cartao]] db))

(defn busca-cliente-por-cartao [db cartao]
  (d/q '[:find ?cliente
         :keys cliente
         :in $ ?cartao-a-ser-buscado
         :where [?compra :compras/cartao ?cartao-a-ser-buscado]
         [?compra :compras/cliente ?cliente]] db cartao))

(defn busca-compras-por-cartao [db cartao]
  (d/q '[:find ?data ?valor ?estabelecimento ?categoria
         :keys data valor estabelecimento categoria
         :in $ ?cartao-a-ser-buscado
         :where [?compra :compras/cartao ?cartao-a-ser-buscado]
         [?compra :compras/data ?data]
         [?compra :compras/valor ?valor]
         [?compra :compras/estabelecimento ?estabelecimento]
         [?compra :compras/categoria ?categoria]] db cartao))

(defn todas-as-categorias [db]
  (d/q '[:find (pull ?categoria [*])
         :where [?categoria :categoria/id]] db))

(defn todas-as-compras [db]
  (d/q '[:find (pull ?entidade [*])
         :where [?entidade :compras/cartao]] db))

(defn uma-compra [db compras-id]
  (d/pull db '[*] [:compras/id compras-id]))

(defn total-dos-gastos
  [elementos]
  (formata-com-duas-casas-decimais (reduce + (map :valor elementos))))

(defn todos-os-gastos
  [[chave valor]]
  {chave (total-dos-gastos valor)})

(defn todas-as-compras-por-cartao
  [cartao]
  (let [todas-compras (busca-compras-por-cartao db cartao)]
    {:cliente                       (map :cliente (busca-cliente-por-cartao db cartao))
     :quantidade-total-de-compras   (count (map :valor todas-compras))
     :total-de-gastos               (total-dos-gastos todas-compras)
     :total-de-gastos-por-categoria (map todos-os-gastos (group-by :categoria todas-compras))
     :compras-realizadas            todas-compras}))


;consulta que retorna todas as compras
(todas-as-compras db)

;consulta que retorna uma compra dado um id
(pprint (uma-compra db (:compras/id compra-50-escola)))

;consulta que retorna todas as categorias
(todas-as-categorias db)

;consulta que retorna um cliente por cartao
(busca-cliente-por-cartao db 10N)

;consulta que retorna todas as compras de um cartao
(busca-compras-por-cartao db 10N)

;consulta que retorna o gasto total por categoria
(defn relatorio-gasto-total-por-categoria [db]
  (d/q '[:find ?nome (sum ?valor)
         :keys categoria valor-total
         :with ?compra
         :where [?compra :compras/valor ?valor]
         ;[?compra :compras/categoria1 ?categoria1]
         [?categoria :categoria/nome ?nome]] db))

(relatorio-gasto-total-por-categoria db)

;lista todas as compras por cartão
(todas-as-compras-por-cartao 10N)


;(db/apaga-banco)

