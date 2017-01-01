(ns instaparse-c.grammar.struct
  (:refer-clojure :exclude [cat comment function string? struct])
  (:require 
   [instaparse.combinators :refer :all]
   [instaparse-c.util :refer :all]))

(def struct 
  {:mcc.statement/struct (altnt :mcc.struct/declaration :mcc.struct/definition)

   :mcc.struct/member 
   (cat (altnt :mcc/data-type :mcc/symbol)
        (string? "*")
        (nt :mcc/symbol)
        (hs ";"))

   :mcc.struct/declaration
   (cat (string "struct")
        (nt? :mcc/symbol)
        (brackets (star (nt :mcc.struct/member))))

   :mcc.struct/definition
   (cat (nt :mcc.struct/declaration)
        (nt? :mcc/symbol)
        (string ";")
        )})
