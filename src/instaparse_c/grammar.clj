(ns instaparse-c.grammar
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
   [instaparse-c.expression :refer [expression]]
   [instaparse-c.for :refer [for]]
   [instaparse-c.function :refer [function]]
   [instaparse-c.goto :refer [goto]]
   [instaparse-c.if :refer [if]]
   [instaparse-c.literal :refer [literal]]
   [instaparse-c.macro :refer [macro]]
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
                comment
                data-type
                expression
                literal
                preprocessor
                symbol
                ])

(def statements '[
                  continue
                  do
                  enum
                  expression
                  for
                  function
                  goto
                  if
                  return
                  struct
                  switch
                  typedef
                  variable
                  while
                  ])

(def grammar
  (let [statement-keywords
        (map #(keyword (str "mcc.statement/" (name %))) statements)
        alts (apply altnt statement-keywords)
        starter {:mcc/start (nt :mcc/statements)
                 :mcc/statements
                 (star (nt :mcc/statement))
                 :mcc/statement alts}]
    (apply merge
           (concat [starter]
                   (eval statements)
                   (eval language)))))
