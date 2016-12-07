(ns instaparse-c.return
  (:refer-clojure :exclude [cat comment function string?])
  (:require 
   [instaparse.combinators :refer :all]
   [instaparse-c.util :refer :all]))

(def return
  {:c11.statement/return
   (cat (string "return")
        (nt? :c11/expression)
        (hs ";"))})

