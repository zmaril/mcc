(ns instaparse-c.function
  (:refer-clojure :exclude [cat comment function])
  (:require 
   [instaparse.combinators :refer :all]
   [instaparse-c.util :refer :all]))

(def function 
  {:c11/function
   (altnt :c11.function/declaration
          :c11.function/definition
          :c11.function/call)
   :c11.function/argument
   (cat (altnt :c11/symbol :c11/data-type)
        (opt (string "*"))
        (nt :c11/symbol))
   :c11.function/declaration
   (cat
    (opt (string "static"))
    (alt (string "void")
         (nt :c11/data-type)
         (nt :c11/symbol))
    (nt :c11/symbol)
    (parens (list-of (nt :c11.function/argument)))
    (opt (hs ";")))
   :c11.function/definition
   (cat (nt :c11.function/declaration)
        (hs "{")
        (nt :c11)
        (hs "}"))
   :c11.function/call
   (cat (nt :c11/symbol)
        (parens (list-of (nt :c11/expression)))
        (opt (string ";")))
   })
