(ns mcc.grammar.return
  (:refer-clojure :exclude [cat comment function string?])
  (:require 
   [instaparse.combinators :refer :all]
   [mcc.util :refer :all]))

(def return
  {:mcc.statement/return
   (cat (string "return")
        (nt? :mcc/expression)
        (hs ";"))})

