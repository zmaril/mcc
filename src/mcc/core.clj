(ns mcc.core
  (:refer-clojure :exclude [cat comment struct do printf])
  (:require
   [clojure.string :refer [split-lines trim join]]
   [clojure.spec :as s]
   [mcc.parse :refer [clean-parse]]
   [mcc.bundle :refer [into-bundles]]
   [mcc.chunk  :refer [into-chunks produce-text produce-strings]]
   [mcc.datascript  :refer [enlive-output->datascript-datums schema]]
   [mcc.expression :refer [remove-cruft]]
   [mcc.util :refer [altnt cart]]
   [clojure.data :refer [diff]]
   [com.rpl.specter :refer :all]
   [datascript.core :as d]))

(def testtest (slurp  "dev-resources/corpus/openssh-portable/testtest.c"))

(def bundled (into-bundles testtest))
(def tags (map :type bundled))
(def test-chunks (into-chunks bundled))
(map join (cart (map produce-text test-chunks)))


(s/explain :mcc/bundles bundled)
(s/conform :mcc/bundles bundled)

(s/explain (s/* :mcc/chunked) bundled)
(s/conform (s/* :mcc/chunked) bundled)
(apply diff (map clean-parse (produce-strings test-chunks)))

(def testparse
 (clean-parse (first (produce-strings test-chunks))))
(def metad (enlive-output->datascript-datums testparse))
(def conn (d/create-conn schema))
(d/transact! conn [metad])

(def printf
 (d/q '[:find  ?prize
        :where [?value :value "printf"]
               [?symbol :content ?value]
               [?fun-call :content ?symbol]
               [?fun-call :content ?args]
               [?args :order 1]
               [?args :content ?temp1]
               [?temp1 :content ?temp2]
               [?temp2 :value ?prize]]
      @conn))
