(ns mcc.grammar.enum
  (:refer-clojure :exclude [cat string?])
  (:require 
   [instaparse.combinators :refer :all]
   [mcc.util :refer :all]))

(def enum 
  ;;TODO: differentiate between typeless single use enum and an enum declaration
  ;;that creates a type
  {:mcc.statement/enum
   (cat (string "enum")
        (nt? :mcc/symbol)
        (brackets (list-of (nt :mcc/symbol)))
        (nt? :mcc/symbol)
        (hs ";")
        )

   })
