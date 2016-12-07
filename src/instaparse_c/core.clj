(ns instaparse-c.core
  (:refer-clojure :exclude [cat comment symbol if while for string? struct do])
  (:require 
   [instaparse.combinators :refer :all]
   [instaparse.core :as insta]

   [instaparse-c.assignment :refer [assignment]]
   [instaparse-c.comment :refer [comment]]
   [instaparse-c.continue :refer [continue]]
   [instaparse-c.do :refer [do]]
   [instaparse-c.enum :refer [enum]]
   [instaparse-c.expression :refer [expression remove-cruft]]
   [instaparse-c.for :refer [for]]
   [instaparse-c.function :refer [function]]
   [instaparse-c.goto :refer [goto]]
   [instaparse-c.if :refer [if]]
   [instaparse-c.macro :refer [macro]]
   [instaparse-c.return :refer [return]]
   [instaparse-c.struct :refer [struct]]
   [instaparse-c.switch :refer [switch]]
   [instaparse-c.typedef :refer [typedef]]
   [instaparse-c.util :refer :all]
   [instaparse-c.variable :refer [variable]]
   [instaparse-c.while :refer [while]]
   ))

;;Symbols serve both macros and the c language proper
(def symbol
  {:c11/symbol (cat (neg (nt :c11/reserved))
                    (regexp "[a-zA-Z_][a-zA-Z_0-9]*"))
   :c11/reserved
   (alt (alts "extern" "void" "if" "NULL" "return")
        (nt :c11.data-type/storage)
        (nt :c11.data-type/qualifier)
        (nt :c11.data-type/specifier-keywords)
        )})

(def data-type
  {:c11/data-type
   (cat 
    (star (nt :c11.data-type/storage))
    (star (nt :c11.data-type/qualifier))
    (nt :c11.data-type/specifier))

   :c11.data-type/storage (alts "typedef"
                                "extern"
                                "static"
                                "auto"
                                "register")

   :c11.data-type/qualifier
   (alts "restrict" "volatile" "const")

   ;;TODO: this breaks validation, it produces combinations of specifiers that
   ;;aren't valid according to the c11 specification. 
   :c11.data-type/specifier
    (alt
     (plus (nt :c11.data-type/specifier-keywords))
     (altnt :c11/symbol :c11.data-type/struct :c11.data-type/enum))

   :c11.data-type/specifier-keywords
   (alts "void" "char" "short" "int" "long" "float" "double" "signed" "unsigned"
         "_Bool" "_Complex")

   :c11.data-type/struct (cat (string "struct") (nt :c11/symbol))
   :c11.data-type/enum (cat (string "enum") (nt :c11/symbol))
   })

(def literal
  {:c11/literal
   (altnt :c11.literal/int :c11.literal/string
          :c11.literal/null :c11.literal/char
          :c11.literal/octal)
   :c11.literal/char (regexp "'\\\\?.'")
   :c11.literal/octal (regexp "'\\\\0[0-7]+'")
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
                  typedef
                  struct
                  variable
                  function
                  if
                  continue
                  for
                  while
                  expression
                  switch
                  return
                  goto
                  enum
                  do
                  ])

(def c11-grammar
  (let [statement-keywords
        (map #(keyword (str "c11.statement/" (name %))) statements)
        alts (apply altnt statement-keywords)
        starter {:c11/start (nt :c11/statements)
                 :c11/statements
                 (star (altnt :c11/statement :c11/macro))
                 :c11/statement alts}]
    (apply merge
           (concat [starter]
                   (eval statements)
                   (eval language)))))

(def whitespace
  (merge comment
         {:c11/whitespace
          (plus (altnt :c11.whitespace/characters :c11/comment))
          :c11.whitespace/characters
          (regexp "[\\s]+") })
  )

(def parse
  (insta/parser c11-grammar
                :start :c11/start
                :auto-whitespace
                (insta/parser whitespace
                              :start :c11/whitespace)))

(defn clean-parse [& args]
  (let [parsed (apply parse args)]
    (if (insta/failure? parsed)
      parsed
      (remove-cruft parsed))))

(defn clean-parses [& args]
  (let [parsed (apply insta/parses (cons parse args))]
    (map remove-cruft parsed)))
