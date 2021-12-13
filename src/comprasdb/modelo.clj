(ns comprasdb.modelo)

(defn novo-cartao
  [cartao cliente numero cvv validade limite]
  {:compras/cartao   cartao
   :compras/cliente  cliente
   :compras/numero   numero
   :compras/cvv      cvv
   :compras/validade validade
   :compras/limite   limite})

(defn uuid [] (java.util.UUID/randomUUID))

(defn nova-compra
  ([cartao data valor estabelecimento categoria]
   (nova-compra (uuid) cartao data valor estabelecimento categoria))
  ([uuid cartao data valor estabelecimento categoria]
   {:compras/id uuid
    :compras/cartao          cartao
    :compras/data            data
    :compras/valor           valor
    :compras/estabelecimento estabelecimento
    :compras/categoria       categoria}))

(defn nova-categoria
  ([nome]
   (nova-categoria (uuid) nome))
  ([uuid nome]
   {:categoria/id uuid
    :categoria/nome nome}))


