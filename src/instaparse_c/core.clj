(ns instaparse-c.core
  (:refer-clojure :exclude [cat comment symbol if while for])
  (:require 
   [instaparse.core :as insta]
   [instaparse.combinators :refer :all]
   [instaparse-c.util :refer :all]
   [instaparse-c.comment :refer [comment]]
   [instaparse-c.macro :refer [macro]]
   [instaparse-c.variable :refer [variable]]
   [instaparse-c.function :refer [function]]
   [instaparse-c.if :refer [if]]
   [instaparse-c.for :refer [for]]
   [instaparse-c.while :refer [while]]
   [instaparse-c.expression :refer [expression]]
   [instaparse-c.assignment :refer [assignment]]
   ))

(def symbol
  {:c11/symbol (cat (neg (nt :c11/reserved))
                    (regexp "[a-zA-Z_][a-zA-Z_0-9]*"))
   :c11/reserved
   (alt (nt :c11/data-type)
        )})

(def data-type
  {:c11/data-type
   (altnt :c11.data-type/char
          :c11.data-type/long
          :c11.data-type/int)

   :c11.data-type/char (string "char")
   :c11.data-type/long
   (cat (opt (string "long")) (string "long"))
   :c11.data-type/int (string "int")})

(def literal
  {:c11/literal (altnt :c11.literal/int :c11.literal/macro :c11.literal/string)
   :c11.literal/string (regexp "\"[^\"]*\"")
   :c11.literal/macro (regexp "[A-Z_]+")
   :c11.literal/int (regexp "-?[0-9]+")})


(def language '[
                data-type
                literal
                symbol
                expression
                ])

(def statements '[
                  comment
                  macro
                  variable
                  function
                  if
                  for
                  while
                  assignment
                  ])

(def c11-grammar
  (let [statement-keywords
        (map #(keyword (str "c11/" (name %))) statements)
        alts (apply altnt statement-keywords)
        starter {:c11 (star (nt :c11/statement))
                 :c11/statement alts}]
    (apply merge
           (concat [starter]
                   (eval statements)
                   (eval language)))))

(def whitespace
  (insta/parser {:whitespace (regexp "[\\s]+") }
                :start :whitespace))

(def parse
  (insta/parser c11-grammar :start :c11 :auto-whitespace whitespace))

