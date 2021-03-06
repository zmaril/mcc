(ns mcc.grammar.if
  (:refer-clojure :exclude [cat comment function if string?])
  (:require 
   [instaparse.combinators :refer :all]
   [mcc.util :refer :all]))

(def if 
  {:mcc.statement/if
   (cat
    (hs "if")
    (parens (nt :mcc/expression))
    (nt :mcc.if/body)
    (star (cat
           (string "else if")
           (parens (nt :mcc/expression))
           (nt :mcc.if/body)))
    (cat? (string "else")
          (nt :mcc.if/body)))
   :mcc.if/body
   (alt
    (nt :mcc/statement)
    (cat (hs "{")
         (star (nt :mcc/statement))
         (hs "}")))})
