(ns instaparse-c.continue
  (:refer-clojure :exclude [cat comment string?])
  (:require 
   [instaparse.combinators :refer :all]
   [instaparse-c.util :refer :all]))

(def continue
  {:mcc.statement/continue (cat (string "continue") (string ";"))})
