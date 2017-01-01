(ns instaparse-c.grammar.assignment
  (:refer-clojure :exclude [cat comment function for string?])
  (:require 
   [instaparse.combinators :refer :all]
   [instaparse-c.util :refer :all]))

;;This is both here and in expressions. strange
(def assignment 
  {:mcc/assignment
   (cat
    (nt :mcc/expression)
    (hs "=")
    (nt :mcc/expression)
    (hs ";"))
   })
