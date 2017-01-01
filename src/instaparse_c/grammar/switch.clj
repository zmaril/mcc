(ns instaparse-c.switch
  (:refer-clojure :exclude [cat comment function if while string?])
  (:require 
   [instaparse.combinators :refer :all]
   [instaparse-c.util :refer :all]))

(def switch 
  {:mcc.statement/switch
   (cat
    (hs "switch")
    (parens (nt :mcc/expression))
    (brackets  
     (cat 
      ;;TODO: should comments be considered whitespace?
      (nt? :mcc/comment)
      (star (nt :mcc.switch/case))
      (nt? :mcc.switch/default))))
   :mcc.switch/case
   (cat (string "case") (nt :mcc/expression) (string ":")
        (star (altnt :mcc/statement :mcc/comment)))
   :mcc.switch/default
   (cat (string "default") (string ":")
        (star (altnt :mcc/statement :mcc/comment)))
   })
