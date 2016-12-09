(ns instaparse-c.core
  (:refer-clojure :exclude [cat comment symbol if while for string? struct do])
  (:require 
   [instaparse.combinators :refer :all]
   [instaparse.core :as insta]
   [instaparse-c.assignment :refer [assignment]]
   [instaparse-c.comment :refer [comment]]
   [instaparse-c.continue :refer [continue]]
   [instaparse-c.data-type :refer [data-type]]
   [instaparse-c.do :refer [do]]
   [instaparse-c.enum :refer [enum]]
   [instaparse-c.expression :refer [expression remove-cruft]]
   [instaparse-c.for :refer [for]]
   [instaparse-c.function :refer [function]]
   [instaparse-c.goto :refer [goto]]
   [instaparse-c.if :refer [if]]
   [instaparse-c.literal :refer [literal]]
   [instaparse-c.preprocessor :refer [preprocessor]]
   [instaparse-c.return :refer [return]]
   [instaparse-c.struct :refer [struct]]
   [instaparse-c.switch :refer [switch]]
   [instaparse-c.symbol :refer [symbol]]
   [instaparse-c.typedef :refer [typedef]]
   [instaparse-c.util :refer :all]
   [instaparse-c.variable :refer [variable]]
   [instaparse-c.while :refer [while]]))


(def language '[
                data-type
                literal
                symbol
                expression
                comment
                macro
                preprocessor
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

(def mcc-grammar
  (let [statement-keywords
        (map #(keyword (str "mcc.statement/" (name %))) statements)
        alts (apply altnt statement-keywords)
        starter {:mcc/start (nt :mcc/statements)
                 :mcc/statements
                 (star (altnt :mcc/statement :mcc/macro))
                 :mcc/statement alts}]
    (apply merge
           (concat [starter]
                   (eval statements)
                   (eval language)))))

(def whitespace
  (merge comment
         {:mcc/whitespace
          (plus (altnt :mcc.whitespace/characters :mcc/comment))
          :mcc.whitespace/characters
          (regexp "[\\s]+") })
  )

(def parse
  (insta/parser mcc-grammar
                :start :mcc/start
                :auto-whitespace
                (insta/parser whitespace
                              :start :mcc/whitespace)))

(def preprocess
  (insta/parser mcc-grammar
                :start :mcc/raw
                :auto-whitespace
                (insta/parser whitespace
                              :start :mcc/whitespace)
                ))

(defn clean-parse [& args]
  (let [parsed (apply parse args)]
    (if (insta/failure? parsed)
      parsed
      (remove-cruft parsed))))

(defn clean-parses [& args]
  (let [parsed (apply insta/parses (cons parse args))]
    (map remove-cruft parsed)))

(defn clean-preprocess [& args]
  (let [parsed (apply preprocess args)]
    (if (insta/failure? parsed)
      parsed
      (remove-cruft parsed))))

;;TODO: weird function names
(defn clean-preprocess* [& args]
  (let [parsed (apply insta/parses (cons preprocess args))]
    (map remove-cruft parsed)))
