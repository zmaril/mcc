(ns instaparse-c.return
  (:refer-clojure :exclude [cat comment function string?])
  (:require 
   [instaparse.combinators :refer :all]
   [instaparse-c.util :refer :all]))

(def return
  {:mcc.statement/return
   (cat (string "return")
        (nt? :mcc/expression)
        (hs ";"))})

