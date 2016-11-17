(ns instaparse-c.while
  (:refer-clojure :exclude [cat comment function if while])
  (:require 
   [instaparse.combinators :refer :all]
   [instaparse-c.util :refer :all]))

(def while 
  {:c11/while
   (cat
    (hs "while")
    (parens (nt :c11/expression))
    (alt (nt :c11/statement)
         (cat (hs "{")
              (nt :c11)
              (hs "}")))
    )
   })
