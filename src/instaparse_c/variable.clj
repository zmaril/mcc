(ns instaparse-c.variable
  (:refer-clojure :exclude [cat comment string?])
  (:require 
   [instaparse.combinators :refer :all]
   [instaparse-c.util :refer :all]))

(def variable 
  {:c11.statement/variable
   (altnt :c11.variable/declaration :c11.variable/definition)
   ;;For lack of a better term, using `description` here to mean a declaration
   ;; but not a statement. Makes code reuse easier and removes ambiguity.
   :c11.variable/description
   (cat (opt (string "extern"))
        (opt (string "struct"))
        (altnt :c11/data-type :c11/symbol)
        (opt (string "*"))
        (list-of 
         (cat 
          (nt :c11/symbol)
          (opt (cat
                (string "[")
                (opt (nt :c11/expression))
                (string "]"))))))

   :c11.variable/declaration
   (cat
    (nt :c11.variable/description)
    (hs ";"))
   :c11.variable/definition
   (cat (nt :c11.variable/description)
        (hs "=")
        (altnt :c11/expression)
        (hs ";"))
   })
