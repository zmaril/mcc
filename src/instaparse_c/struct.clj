(ns instaparse-c.struct
  (:refer-clojure :exclude [cat comment function string? struct])
  (:require 
   [instaparse.combinators :refer :all]
   [instaparse-c.util :refer :all]))

(def struct 
  {:c11.statement/struct (altnt :c11.struct/declaration :c11.struct/definition)

   :c11.struct/member 
   (cat (altnt :c11/data-type :c11/symbol)
        (string? "*")
        (nt :c11/symbol)
        (hs ";"))

   :c11.struct/declaration
   (cat (string "struct")
        (nt? :c11/symbol)
        (brackets (star (nt :c11.struct/member))))

   :c11.struct/definition
   (cat (nt :c11.struct/declaration)
        (nt? :c11/symbol)
        (string ";")
        )})
