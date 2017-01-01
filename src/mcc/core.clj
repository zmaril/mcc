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

(defn string->db [s]
  (let [chunked (-> s into-bundles into-chunks)]))
(def sample (slurp  "dev-resources/sample.c"))

(def bundled (into-bundles sample))
(def chunked (into-chunks bundled))
(map join (cart (map produce-text chunked)))

(s/explain :mcc/bundles bundled)
(s/conform :mcc/bundles bundled)

(s/explain (s/* :mcc/chunked) bundled)
(s/conform (s/* :mcc/chunked) bundled)

(apply diff (map clean-parse (produce-strings chunked)))

(def parsed
 (clean-parse (first (produce-strings chunked))))

(def datums-sample (enlive-output->datascript-datums parsed))
(def conn (d/create-conn schema))
(d/transact! conn [datums-sample])
(d/q '[:find  ?prize
       :where [?value :value "printf"]
              [?symbol :content ?value]
              [?fun-call :content ?symbol]
              [?fun-call :content ?args]
              [?args :order 1]
              [?args :content ?temp1]
              [?temp1 :content ?temp2]
              [?temp2 :value ?prize]]
     @conn)
