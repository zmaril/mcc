(ns mcc.grammar.continue
  (:refer-clojure :exclude [cat comment string?])
  (:require 
   [instaparse.combinators :refer :all]
   [mcc.util :refer :all]))

(def continue
  {:mcc.statement/continue (cat (string "continue") (string ";"))})
