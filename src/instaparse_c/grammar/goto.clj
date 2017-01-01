(ns instaparse-c.grammar.goto
  (:refer-clojure :exclude [cat comment function string?])
  (:require 
   [instaparse.combinators :refer :all]
   [instaparse-c.util :refer :all]))

(def goto
  {:mcc.statement/goto (altnt :mcc.goto/goto :mcc.goto/label)
   :mcc.goto/goto
   (cat (nt :mcc/symbol) (string ":") (nt :mcc/statement))
   :mcc.goto/label
   (cat (string "goto") (nt :mcc/symbol) (string ";"))})

