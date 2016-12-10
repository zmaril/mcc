(ns instaparse-c.function
  (:refer-clojure :exclude [cat comment function string?])
  (:require 
   [instaparse.combinators :refer :all]
   [instaparse-c.util :refer :all]))

(def function 
  {:mcc.statement/function
   (altnt :mcc.function/declaration
          :mcc.function/definition)
   :mcc.function/argument
   (cat (nt :mcc/data-type)
        ;;What a mess ick
        (star (string "*"))
        (string? "[]")
        ;;Declarations don't need names for arguments apparently!
        ;;BUF *allocbuf(BUF *, int, int);
        (nt? :mcc/symbol))
   :mcc.function/declaration-header
   (cat
    (nt :mcc/data-type)
    (string? "*")
    (nt :mcc/symbol)
    (parens (list-of? (nt :mcc.function/argument))
            (cat? (hs ",") (string "...") )))
   :mcc.function/declaration
   (cat (nt :mcc.function/declaration-header) (hs ";"))

   :mcc.function/definition
   (cat (nt :mcc.function/declaration-header)
        (hs "{")
        (star (nt :mcc/statement))
        (hs "}"))})
