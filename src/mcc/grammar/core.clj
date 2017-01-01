(ns mcc.grammar.core
  (:refer-clojure :exclude [cat comment symbol if while for string? struct do])
  (:require
   [instaparse.combinators :refer :all]
   [instaparse.core :as insta]
   [mcc.util :refer :all]
   [mcc.grammar.assignment :refer [assignment]]
   [mcc.grammar.comment :refer [comment]]
   [mcc.grammar.continue :refer [continue]]
   [mcc.grammar.data-type :refer [data-type]]
   [mcc.grammar.do :refer [do]]
   [mcc.grammar.enum :refer [enum]]
   [mcc.grammar.expression :refer [expression]]
   [mcc.grammar.for :refer [for]]
   [mcc.grammar.function :refer [function]]
   [mcc.grammar.goto :refer [goto]]
   [mcc.grammar.if :refer [if]]
   [mcc.grammar.literal :refer [literal]]
   [mcc.grammar.macro :refer [macro]]
   [mcc.grammar.preprocessor :refer [preprocessor]]
   [mcc.grammar.return :refer [return]]
   [mcc.grammar.struct :refer [struct]]
   [mcc.grammar.switch :refer [switch]]
   [mcc.grammar.symbol :refer [symbol]]
   [mcc.grammar.typedef :refer [typedef]]
   [mcc.grammar.variable :refer [variable]]
   [mcc.grammar.while :refer [while]]))

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
