(ns instaparse-c.typedef
  (:refer-clojure :exclude [cat string?])
  (:require 
   [instaparse.combinators :refer :all]
   [instaparse-c.util :refer :all]))

(def typedef 
  {:c11.statement/typedef
   (cat (string "typedef")
        (alt
         (cat (nt :c11/data-type) (nt :c11/symbol))
         (cat (nt :c11.struct/declaration)
               (nt? :c11/symbol)
               (hs ";"))
         )
        )

   })
