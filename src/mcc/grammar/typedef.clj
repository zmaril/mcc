(ns mcc.grammar.typedef
  (:refer-clojure :exclude [cat string?])
  (:require 
   [instaparse.combinators :refer :all]
   [mcc.util :refer :all]))

(def typedef 
  {:mcc.statement/typedef
   (cat (string "typedef")
        (alt
         (cat (nt :mcc/data-type) (nt :mcc/symbol))
         (cat (nt :mcc.struct/declaration)
               (nt? :mcc/symbol)
               (hs ";"))
         )
        )

   })
