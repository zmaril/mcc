(ns mcc.grammar.for
  (:refer-clojure :exclude [cat comment function for string?])
  (:require 
   [instaparse.combinators :refer :all]
   [mcc.util :refer :all]))

(def for 
  {:mcc.statement/for
   (cat
    (hs "for")
    (parens
     (cat (nt? :mcc/expression)
          (string ";")
          (nt? :mcc/expression)
          (string ";")
          (nt? :mcc/expression)))
    (alt (nt :mcc/statement)
         (cat (hs "{")
              (nt :mcc/statements)
              (hs "}")))
    )
   })
