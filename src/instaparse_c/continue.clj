(ns instaparse-c.continue
  (:refer-clojure :exclude [cat comment string?])
  (:require 
   [instaparse.combinators :refer :all]
   [instaparse-c.util :refer :all]))

(def continue
  {:c11.statement/continue (cat (string "continue") (string ";"))})
