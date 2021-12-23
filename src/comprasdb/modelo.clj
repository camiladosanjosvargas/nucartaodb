(ns comprasdb.modelo)

(defn uuid [] (java.util.UUID/randomUUID))

(defn novo-cliente
  ([nome cpf email]
   (novo-cartao (uuid) nome cpf email))
  ([uuid nome cpf email]
   {:cliente/id    uuid
    :cliente/nome  nome
    :cliente/cpf   cpf
    :cliente/email email}))

(defn novo-cartao
  ([cartao numero cvv validade limite]
   (novo-cartao (uuid) cartao numero cvv validade limite))
  ([uuid cartao numero cvv validade limite]
   {:cartao/id       uuid
    :cartao/cartao   cartao
    :cartao/numero   numero
    :cartao/cvv      cvv
    :cartao/validade validade
    :cartao/limite   limite}))

(defn nova-compra
  ([data valor estabelecimento]
   (nova-compra (uuid) data valor estabelecimento))
  ([uuid data valor estabelecimento]
   {:compras/id              uuid
    :compras/data            data
    :compras/valor           valor
    :compras/estabelecimento estabelecimento}))

(defn nova-categoria
  ([nome]
   (nova-categoria (uuid) nome))
  ([uuid nome]
   {:categoria/id   uuid
    :categoria/nome nome}))


