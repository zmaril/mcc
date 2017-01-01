(ns instaparse-c.grammar.literal 
  (:refer-clojure :exclude [cat comment string?])
  (:require 
   [instaparse.combinators :refer :all]
   [instaparse-c.util :refer :all]))

(def literal
  {:mcc/literal
   (altnt :mcc.literal/int :mcc.literal/string
          :mcc.literal/null :mcc.literal/char
          :mcc.literal/octal)
   :mcc.literal/char (regexp "'\\\\?.'")
   :mcc.literal/octal (regexp "'\\\\0[0-7]+'")
   :mcc.literal/string (regexp "\"[^\"]*\"")
   :mcc.literal/int (regexp "[0-9]+")
   :mcc.literal/null (string "NULL")})
