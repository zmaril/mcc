(ns instaparse-c.grammar.while
  (:refer-clojure :exclude [cat comment function if while string?])
  (:require 
   [instaparse.combinators :refer :all]
   [instaparse-c.util :refer :all]))

(def while 
  {:mcc.statement/while
   (cat
    (hs "while")
    (parens (nt :mcc/expression))
    (alt (nt :mcc/statement)
          (cat (hs "{")
               (star (nt :mcc/statement))
               (hs "}"))))

   })
