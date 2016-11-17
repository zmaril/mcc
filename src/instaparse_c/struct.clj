(ns instaparse-c.struct
  (:refer-clojure :exclude [cat comment function])
  (:require 
   [instaparse.combinators :refer :all]
   [instaparse-c.util :refer :all]))

;;not finished at all
(def struct 
  {:c11/struct (altnt :c11.struct/declaration)
   :c11.struct/declaration
   (cat (altnt :c11/data-type :c11/symbol)
        (nt :c11/symbol)
        (parens (list-of (nt :c11.function/argument)))
        (hs ";")
        )
   })
