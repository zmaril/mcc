(ns instaparse-c.do
  (:refer-clojure :exclude [cat comment function if while string? do])
  (:require 
   [instaparse.combinators :refer :all]
   [instaparse-c.util :refer :all]))

(def do 
  {:mcc.statement/do
   (cat
    (hs "do")
    (alt (nt :mcc/statement)
         (cat (hs "{")
              (star (nt :mcc/statement))
              (hs "}")))
    (hs "while")
    (parens (nt :mcc/expression))
    (hs ";")
    )

   })
