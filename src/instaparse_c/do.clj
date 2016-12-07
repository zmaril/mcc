(ns instaparse-c.do
  (:refer-clojure :exclude [cat comment function if while string? do])
  (:require 
   [instaparse.combinators :refer :all]
   [instaparse-c.util :refer :all]))

(def do 
  {:c11.statement/do
   (cat
    (hs "do")
    (alt (nt :c11/statement)
         (cat (hs "{")
              (star (nt :c11/statement))
              (hs "}")))
    (hs "while")
    (parens (nt :c11/expression))
    (hs ";")
    )

   })
