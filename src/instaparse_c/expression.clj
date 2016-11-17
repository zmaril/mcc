(ns instaparse-c.expression
  (:refer-clojure :exclude [cat comment function])
  (:require 
   [instaparse.combinators :refer :all]
   [instaparse-c.util :refer :all]))

(def expression 
  {:c11/expression
   ;;precedence tree climbing todo later
   (altnt
    :c11.expression/parens
    :c11.expression/comparison
    :c11.expression/and
    :c11.expression/or
    :c11.expression/not
    :c11.expression/ternary
    :c11.expression/address-of
    :c11.expression/structure-dereference
    :c11.expression/assignment
    :c11.expression/subscript
    :c11.expression/postfix-increment
    :c11.expression/prefix-increment
    :c11.function/call
    :c11/variable
    :c11/literal
    :c11/symbol)
   :c11.expression/parens
   (parens (nt :c11/expression))
   :c11.expression/comparison 
   (cat
    (nt :c11/expression)
    (alts ">" "<" "==" "!=" "<=" ">=")
    (nt :c11/expression))
   :c11.expression/subscript
   (cat
    (nt :c11/expression)
    (hs "[")
    (nt :c11/expression)
    (hs "]"))
   :c11.expression/postfix-increment
   (cat
    (nt :c11/expression)
    (alts "--" "++"))
   :c11.expression/prefix-increment
   (cat
    (alts "--" "++")
    (nt :c11/expression))
   :c11.expression/not
   (cat
    (string "!")
    (nt :c11/expression))
   :c11.expression/ternary 
   (cat
    (nt :c11/expression)
    (hs "?")
    (nt :c11/expression)
    (hs ":")
    (nt :c11/expression))

   :c11.expression/assignment 
   (cat
    (nt :c11/expression)
    (hs "=")
    (nt :c11/expression))
   :c11.expression/and 
   (cat
    (nt :c11/expression)
    (alts "and" "&&")
    (nt :c11/expression))
   :c11.expression/or
   (cat
    (nt :c11/expression)
    (alts "or" "||")
    (nt :c11/expression))
   :c11.expression/address-of
   (cat (hs "&") (nt :c11/expression))
   :c11.expression/structure-dereference
   (cat (nt :c11/symbol) (hs "->") (nt :c11/symbol))
   })
