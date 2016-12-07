(ns instaparse-c.enum
  (:refer-clojure :exclude [cat string?])
  (:require 
   [instaparse.combinators :refer :all]
   [instaparse-c.util :refer :all]))

(def enum 
  ;;TODO: differentiate between typeless single use enum and an enum declaration
  ;;that creates a type
  {:c11.statement/enum
   (cat (string "enum")
        (nt? :c11/symbol)
        (brackets (list-of (nt :c11/symbol)))
        (nt? :c11/symbol)
        (hs ";")
        )

   })
