(ns mcc.grammar.variable
  (:refer-clojure :exclude [cat comment string?])
  (:require 
   [instaparse.combinators :refer :all]
   [mcc.util :refer :all]))

(def variable 
  {:mcc.statement/variable
   (altnt :mcc.variable/declaration :mcc.variable/definition)
   ;;For lack of a better term, using `description` here to mean a declaration
   ;; but not a statement. Makes code reuse easier and removes ambiguity.
   :mcc.variable/description
   (cat (nt :mcc/data-type)
        (list-of 
         (cat 
          (star (string "*"))
          (nt :mcc/symbol)
          (opt (cat
                (string "[")
                (opt (nt :mcc/expression))
                (string "]"))))))

   :mcc.variable/declaration
   (cat
    (nt :mcc.variable/description)
    (hs ";"))
   :mcc.variable/definition
   (cat (nt :mcc.variable/description)
        (hs "=")
        (altnt :mcc/expression)
        (hs ";"))
   })
