(ns comprasdb.util)

(defn formata-com-duas-casas-decimais [valor] (clojure.core/format "R$ %.2f" (float valor)))