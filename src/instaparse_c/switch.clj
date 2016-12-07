(ns instaparse-c.switch
  (:refer-clojure :exclude [cat comment function if while string?])
  (:require 
   [instaparse.combinators :refer :all]
   [instaparse-c.util :refer :all]))

(def switch 
  {:c11.statement/switch
   (cat
    (hs "switch")
    (parens (nt :c11/expression))
    (brackets  
     (cat 
      ;;TODO: should comments be considered whitespace?
      (nt? :c11/comment)
      (star (nt :c11.switch/case))
      (nt? :c11.switch/default))))
   :c11.switch/case
   (cat (string "case") (nt :c11/expression) (string ":")
        (star (altnt :c11/statement :c11/macro :c11/comment)))
   :c11.switch/default
   (cat (string "default") (string ":")
        (star (altnt :c11/statement :c11/macro :c11/comment)))
   })
