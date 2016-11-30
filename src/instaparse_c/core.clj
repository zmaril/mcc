(ns instaparse-c.core
  (:refer-clojure :exclude [cat comment symbol if while for string?])
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
   [instaparse-c.expression :refer [expression remove-cruft]]
   [instaparse-c.assignment :refer [assignment]]
   ))

;;Symbols serve both macros and the c language proper
(def symbol
  {:c11/symbol (cat (neg (nt :c11/reserved))
                    (regexp "[a-zA-Z_][a-zA-Z_0-9]*"))
   :c11/reserved
   (alt (nt :c11/data-type)
        (alts "extern" "void" "if" "NULL")
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
  {:c11/literal
   (altnt :c11.literal/int :c11.literal/string :c11.literal/null)
   :c11.literal/string (regexp "\"[^\"]*\"")
   :c11.literal/int (regexp "[0-9]+")
   :c11.literal/null (string "NULL")})


(def language '[
                data-type
                literal
                symbol
                expression
                comment
                macro
                ])

(def statements '[
                  variable
                  function
                  if
                  for
                  while
                  expression
                  ])

(def c11-grammar
  (let [statement-keywords
        (map #(keyword (str "c11.statement/" (name %))) statements)
        alts (apply altnt statement-keywords)
        starter {:c11/start (nt :c11/statements)
                 :c11/statements
                 (star (altnt :c11/statement :c11/comment :c11/macro))
                 :c11/statement alts}]
    (apply merge
           (concat [starter]
                   (eval statements)
                   (eval language)))))

(def whitespace
  (insta/parser {:whitespace (regexp "[\\s]+") }
                :start :whitespace))

(def parse
  (insta/parser c11-grammar :start :c11/start :auto-whitespace whitespace))

(defn clean-parse [& args]
  (let [parsed (apply parse args)]
    (if (insta/failure? parsed)
      parsed
      (remove-cruft parsed))))

(defn clean-parses [& args]
  (let [parsed (apply insta/parses (cons parse args))]
    (map remove-cruft parsed)))
