(ns instaparse-c.comment
  (:refer-clojure :exclude [cat comment string?])
  (:require 
   [instaparse.combinators :refer :all]
   [instaparse-c.util :refer :all]))

(def comment
  {:mcc/comment (altnt :mcc.comment/mutliple-lines
                       :mcc.comment/single-line)
   :mcc.comment/mutliple-lines
   (regexp "/\\*([\\s\\S])*?(?=\\*/)\\*/")
   :mcc.comment/single-line
   (cat (hs "//")
        (regexp ".*")
        (hs "\n"))})
