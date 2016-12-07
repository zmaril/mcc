(ns instaparse-c.goto
  (:refer-clojure :exclude [cat comment function string?])
  (:require 
   [instaparse.combinators :refer :all]
   [instaparse-c.util :refer :all]))

(def goto
  {:c11.statement/goto (altnt :c11.goto/goto :c11.goto/label)
   :c11.goto/goto
   (cat (nt :c11/symbol) (string ":") (nt :c11/statement))
   :c11.goto/label
   (cat (string "goto") (nt :c11/symbol) (string ";"))})

