(ns instaparse-c.grammar.core
  (:refer-clojure :exclude [cat comment symbol if while for string? struct do])
  (:require
   [instaparse.combinators :refer :all]
   [instaparse.core :as insta]
   [instaparse-c.util :refer :all]
   [instaparse-c.grammar.assignment :refer [assignment]]
   [instaparse-c.grammar.comment :refer [comment]]
   [instaparse-c.grammar.continue :refer [continue]]
   [instaparse-c.grammar.data-type :refer [data-type]]
   [instaparse-c.grammar.do :refer [do]]
   [instaparse-c.grammar.enum :refer [enum]]
   [instaparse-c.grammar.expression :refer [expression]]
   [instaparse-c.grammar.for :refer [for]]
   [instaparse-c.grammar.function :refer [function]]
   [instaparse-c.grammar.goto :refer [goto]]
   [instaparse-c.grammar.if :refer [if]]
   [instaparse-c.grammar.literal :refer [literal]]
   [instaparse-c.grammar.macro :refer [macro]]
   [instaparse-c.grammar.preprocessor :refer [preprocessor]]
   [instaparse-c.grammar.return :refer [return]]
   [instaparse-c.grammar.struct :refer [struct]]
   [instaparse-c.grammar.switch :refer [switch]]
   [instaparse-c.grammar.symbol :refer [symbol]]
   [instaparse-c.grammar.typedef :refer [typedef]]
   [instaparse-c.grammar.variable :refer [variable]]
   [instaparse-c.grammar.while :refer [while]]))

(def language '[
                comment
                data-type
                expression
                literal
                preprocessor
                symbol])


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
                  while])


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
