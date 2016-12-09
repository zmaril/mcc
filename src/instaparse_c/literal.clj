(ns instaparse-c.literal 
  (:refer-clojure :exclude [cat comment string?])
  (:require 
   [instaparse.combinators :refer :all]
   [instaparse-c.util :refer :all]))

(def literal
  {:c11/literal
   (altnt :c11.literal/int :c11.literal/string
          :c11.literal/null :c11.literal/char
          :c11.literal/octal)
   :c11.literal/char (regexp "'\\\\?.'")
   :c11.literal/octal (regexp "'\\\\0[0-7]+'")
   :c11.literal/string (regexp "\"[^\"]*\"")
   :c11.literal/int (regexp "[0-9]+")
   :c11.literal/null (string "NULL")})
