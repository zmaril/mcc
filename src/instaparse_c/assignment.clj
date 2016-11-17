(ns instaparse-c.assignment
  (:refer-clojure :exclude [cat comment function for])
  (:require 
   [instaparse.combinators :refer :all]
   [instaparse-c.util :refer :all]))

;;This is both here and in expressions. strange
(def assignment 
  {:c11/assignment
   (cat
    (nt :c11/expression)
    (hs "=")
    (nt :c11/expression)
    (hs ";"))
   })
