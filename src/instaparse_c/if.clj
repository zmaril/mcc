(ns instaparse-c.if
  (:refer-clojure :exclude [cat comment function if string?])
  (:require 
   [instaparse.combinators :refer :all]
   [instaparse-c.util :refer :all]))

(def if 
  {:c11.statement/if
   (cat
    (hs "if")
    (parens (nt :c11/expression))
    (nt :c11.if/body)
    (star (cat
           (string "else if")
           (parens (nt :c11/expression))
           (nt :c11.if/body)))
    (cat? (string "else")
          (nt :c11.if/body)))
   :c11.if/body
   (alt
    (nt :c11/macro)
    (nt :c11/statement)
    (cat (hs "{")
         (star (nt :c11/statement))
         (hs "}")))})
