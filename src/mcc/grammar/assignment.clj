(ns mcc.grammar.assignment
  (:refer-clojure :exclude [cat comment function for string?])
  (:require 
   [instaparse.combinators :refer :all]
   [mcc.util :refer :all]))

;;This is both here and in expressions. strange
(def assignment 
  {:mcc/assignment
   (cat
    (nt :mcc/expression)
    (hs "=")
    (nt :mcc/expression)
    (hs ";"))
   })
