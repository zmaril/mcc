(ns mcc.core
  (:refer-clojure :exclude [cat comment struct do printf])
  (:require
   [clojure.string :refer [split-lines trim join]]
   [clojure.spec :as s]
   [mcc.parse :refer [clean-parse]]
   [mcc.bundle :refer [into-bundles]]
   [mcc.chunk  :refer [into-chunks produce-text produce-strings]]
   [mcc.datascript  :refer [enlive-output->datascript-datums schema]]
   [mcc.util :refer [altnt cart]]
   [clojure.data :refer [diff]]
   [com.rpl.specter :refer :all]
   [datascript.core :as d]))

#_(map join (cart (map produce-text chunked)))
#_(s/explain :mcc/bundles bundled)
#_(s/conform :mcc/bundles bundled)
#_(s/explain (s/* :mcc/chunked) bundled)
#_(s/conform (s/* :mcc/chunked) bundled)
#_(apply diff (map clean-parse (produce-strings chunked)))

(defn string->db [s]
  (let [chunked (-> s into-bundles into-chunks)
        parsed  (clean-parse (first (produce-strings chunked)))
        datums  (enlive-output->datascript-datums parsed)
        db      (d/create-conn schema)]
       (d/transact! db [datums])
       db))

;;Dev code
(def sample (slurp  "dev-resources/sample.c"))
(def db (string->db sample))

(d/q '[:find  ?prize
       :where [?value :value "printf"]
              [?symbol :content ?value]
              [?fun-call :content ?symbol]
              [?fun-call :content ?args]
              [?args :order 1]
              [?args :content ?temp1]
              [?temp1 :content ?temp2]
              [?temp2 :value ?prize]]
     @db)
