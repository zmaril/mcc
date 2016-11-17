(ns instaparse-c.if
  (:refer-clojure :exclude [cat comment function if])
  (:require 
   [instaparse.combinators :refer :all]
   [instaparse-c.util :refer :all]))

(def if 
  {:c11/if
   (cat
    (hs "if")
    (parens (nt :c11/expression))
    (alt (nt :c11/statement)
         (cat (hs "{")
              (nt :c11)
              (hs "}")))
    )
   })
