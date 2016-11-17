(ns instaparse-c.comment
  (:refer-clojure :exclude [cat comment])
  (:require 
   [instaparse.combinators :refer :all]
   [instaparse-c.util :refer :all]))

(def comment
  {:c11/comment (altnt :c11.comment/mutliple-lines
                       :c11.comment/single-line)
   :c11.comment/mutliple-lines
   (regexp "/\\*([\\s\\S])*?(?=\\*/)\\*/")
   :c11.comment/single-line
   (cat (hs "//")
        (regexp ".*")
        (hs "\n"))})
