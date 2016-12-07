(ns instaparse-c.function
  (:refer-clojure :exclude [cat comment function string?])
  (:require 
   [instaparse.combinators :refer :all]
   [instaparse-c.util :refer :all]))

(def function 
  {:c11.statement/function
   (altnt :c11.function/declaration
          :c11.function/definition)
   :c11.function/argument
   (cat (nt :c11/data-type)
        ;;What a mess ick
        (star (string "*"))
        (string? "[]")
        ;;Declarations don't need names for arguments apparently!
        ;;BUF *allocbuf(BUF *, int, int);
        (nt? :c11/symbol))
   :c11.function/declaration-header
   (cat
    (nt :c11/data-type)
    (string? "*")
    (nt :c11/symbol)
    (parens (list-of? (nt :c11.function/argument))
            (cat? (hs ",") (string "...") )))
   :c11.function/declaration
   (cat (nt :c11.function/declaration-header) (hs ";"))

   :c11.function/definition
   (cat (nt :c11.function/declaration-header)
        (hs "{")
        (star (altnt :c11/statement :c11/macro))
        (hs "}"))})
