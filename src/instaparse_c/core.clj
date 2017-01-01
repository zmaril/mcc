(ns instaparse-c.core
  (:refer-clojure :exclude [cat comment struct do])
  (:require
   [clojure.string :refer [split-lines trim join]]
   [clojure.spec :as s]
   [instaparse.combinators :refer :all]
   [instaparse.core :as insta]
   [instaparse-c.grammar :refer [grammar]]
   [instaparse-c.comment :refer [comment]]
   [instaparse-c.bundle :refer [into-bundles]]
   [instaparse-c.chunk  :refer [into-chunks]]
   [instaparse-c.datascript  :refer [enlive-output->datascript-datums schema]]
   [instaparse-c.expression :refer [remove-cruft]]
   [instaparse-c.util :refer [altnt cart]]
   [clojure.data :refer [diff]]
   [com.rpl.specter :refer :all]
   [datascript.core :as d]))

(def preprocessor-whitespace
  {:mcc.macro/whitespace (plus (regexp "([ \t]|(\\\\\n))+"))})

(def preprocess
  (insta/parser grammar
                :start :mcc/macro
                :auto-whitespace
                (insta/parser preprocessor-whitespace
                              :start :mcc.macro/whitespace)))

(def clean-preprocess
   (comp remove-cruft preprocess))

(insta/set-default-output-format! :enlive)

(def whitespace
  (merge comment
         {:mcc/whitespace
          (plus (altnt :mcc.whitespace/characters :mcc/comment))
          :mcc.whitespace/characters
          (regexp "[\\s]+")}))

(def parse
  (insta/parser grammar
                :start :mcc/start
                :auto-whitespace
                (insta/parser whitespace
                              :start :mcc/whitespace)))

(defn clean-parse [& args]
  (let [parsed (apply parse args)
        parsed (insta/add-line-and-column-info-to-metadata (first args) parsed)]
    (if (insta/failure? parsed)
      parsed
      (remove-cruft parsed))))

(defn clean-parses [& args]
  (let [parsed (apply insta/parses (cons parse args))]
    (map remove-cruft parsed)))

;;;Developing code


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
