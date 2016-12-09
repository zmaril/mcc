(ns instaparse-c.expression
  (:refer-clojure :exclude [cat comment function string?])
  (:require 
   [instaparse.combinators :refer :all]
   [instaparse-c.util :refer :all]
   [instaparse.core :as insta]))

(defn right-to-left [next-tag c]
  (cat
   (nt next-tag)
   (cat? c (nt next-tag))))


(defn right-to-left* [next-tag c]
  (cat
   (nt next-tag)
   (star (cat c (nt next-tag)))))

(defn left-to-right [next-tag c]
  (cat
   (cat? (nt next-tag) c)
   (nt next-tag)))

(defn left-to-right* [next-tag c]
  (cat
   (star (cat (nt next-tag) c))
   (nt next-tag)))

(defn prefix-unary [tag s]
  (cat (string? s) (nt tag)))

(defn postfix-unary [tag s]
  (cat (nt tag) (string? s)))

;; The following uses https://en.wikipedia.org/wiki/Operator-precedence_parser
;; Removes ambiguity from the parses
(def expression 
  {:mcc/expression (nt :mcc/comma) 
   :mcc.statement/expression (cat (nt :mcc/expression) (string ";") )
   :mcc/comma
   (left-to-right* :mcc.expression/assignment (hs ","))

   :mcc.expression/assignment 
   (left-to-right*
    :mcc.expression/ternary
    (alts "=" "+=" "-=" "*=" "/=" "%=" "<<=" ">>=" "&=" "^=" "|="))
   
   :mcc.expression/ternary 
   (cat
    (nt :mcc.expression.logical/or)
    (cat? 
     (hs "?")
     (nt :mcc.expression.logical/or)
     (hs ":")
     (nt :mcc.expression.logical/or)))

   :mcc.expression.logical/or
   (left-to-right* :mcc.expression.logical/and (alts "or" "||"))

   :mcc.expression.logical/and 
   (left-to-right* :mcc.expression.bitwise/or (alts "and" "&&"))

   :mcc.expression.bitwise/or 
   (left-to-right* :mcc.expression.bitwise/xor (string "|"))

   :mcc.expression.bitwise/xor 
   (left-to-right* :mcc.expression.bitwise/and (string "^"))

   :mcc.expression.bitwise/and 
   (cat
    (cat? (nt :mcc.expression/equality) (string "&") (neg (string "&")))
    (nt :mcc.expression/equality))

   :mcc.expression/equality
   (left-to-right* :mcc.expression/comparsion (alts "==" "!="))

   :mcc.expression/comparsion
   (left-to-right* :mcc.expression.bitwise/shift (alts ">" ">=" "<="  "<"))

   :mcc.expression.bitwise/shift 
   (left-to-right* :mcc.expression.arthimetic/addition (alts "<<" ">>"))

   :mcc.expression.arthimetic/addition 
   (left-to-right* :mcc.expression.arthimetic/subtraction (string "+"))

   :mcc.expression.arthimetic/subtraction
   (left-to-right* :mcc.expression.arthimetic/multiplication (string "-"))

   :mcc.expression.arthimetic/multiplication
   (left-to-right* :mcc.expression.arthimetic/division (string "*"))

   :mcc.expression.arthimetic/division
   (left-to-right* :mcc.expression.arthimetic/modulo (string "/"))

   :mcc.expression.arthimetic/modulo
   (left-to-right* :mcc.expression/prefix-operation (string "%"))

   ;;To allow unary operations to commute while still using the parsing tree
   ;;method, and while also avoiding an infinite loop with :mcc/expression at
   ;;the top and bottom of the tree, we consider all unary operators to be the
   ;;same non terminal while parsing.
   ;;TODO: Have a transformation that cleans this up and introduces better
   ;;labels, as one would expect.
   :mcc.expression/prefix-operation
   (cat
    (star
     (alt 
      (alts "++" "--" "!" "~" "*" "&")
      (cat (string "+") (neg (string "+")))
      (cat (string "-") (neg (string "-")))

      ;;TODO: how does the * character interact with data-type declarations? 
      ;;Cast
      (parens (cat (nt :mcc/data-type) (string? "*")))
      ))
    (nt :mcc.expression/size-of))
   

   :mcc.expression/size-of
   (cat (string? "sizeof") (nt :mcc.expression/array-subscript))


   ;;TODO:Cruft does weird things here
   :mcc.expression/array-subscript
   (cat (nt :mcc.expression.member/select-through-pointer)
        (cat?
         (string "[")
         (nt? :mcc/expression)
         (string "]")))

   :mcc.expression.member/select-through-pointer
   (left-to-right :mcc.expression.member/select-by-reference (string "->"))

   :mcc.expression.member/select-by-reference
   (left-to-right :mcc.expression/postfix-increment (string "."))

   :mcc.expression/postfix-increment
   (postfix-unary :mcc.expression/postfix-decrement "++")

   :mcc.expression/postfix-decrement
   (postfix-unary :mcc.expression/bottom "--")

   :mcc.expression/bottom
   (altnt :mcc/literal :mcc/symbol :mcc.expression/function-call
          :mcc.expression/parens)


   ;;TODO: Inside of a function call, comma operators don't exist. By skipping
   ;;past comma as the first tip top part of the expression precedence tree, we
   ;;lose [:mcc/expression] for each parse. One option is to remove the comma
   ;;operator from the parse tree and have it be it's own thing. Another option
   ;;is to have transform at the end that wraps expressions inside of function
   ;;calls.

   :mcc.expression/function-call
   (alt 
    (cat (nt :mcc/symbol)
         (parens (list-of? (nt :mcc.expression/assignment)))))

   :mcc.expression/parens
   (parens (nt :mcc/expression))
   
   :mcc.expression/subscript
   (cat
    (nt :mcc/expression)
    (hs "[")
    (nt :mcc/expression)
    (hs "]"))})

;;Right an assert that checks all the keys are present
(def cruft-pieces
  [:mcc.expression.arthimetic/addition
   :mcc.expression.arthimetic/division
   :mcc.expression.arthimetic/modulo
   :mcc.expression.arthimetic/multiplication
   :mcc.expression.arthimetic/subtraction
   :mcc.expression.bitwise/and
   :mcc.expression.bitwise/not
   :mcc.expression.bitwise/or
   :mcc.expression.bitwise/shift
   :mcc.expression.bitwise/xor
   :mcc.expression.logical/and
   :mcc.expression.logical/not
   :mcc.expression.logical/or
   :mcc.expression/address-of
   :mcc.expression/assignment
   :mcc.expression/bottom
   :mcc.expression/comparsion
   :mcc.expression/equality
   :mcc.expression/function-call
   :mcc.expression/indirection
   :mcc.expression/parens
   :mcc.expression/postfix-decrement
   :mcc.expression/postfix-increment
   :mcc.expression/prefix-decrement
   :mcc.expression/prefix-increment
   :mcc.expression/size-of
   :mcc.expression/subscript
   :mcc.expression/type
   :mcc.expression/unary-minus
   :mcc.expression/unary-plus
   :mcc.expression/ternary
   :mcc.expression/array-subscript
   :mcc.expression.member/select-through-pointer
   :mcc.expression.member/select-by-reference
   :mcc.expression/prefix-operation
   :mcc/comma])

(def cruft
  (->>
   (for [k cruft-pieces]
     [k (fn [& args]
          (let [args (vec args)]
            (if (= 1 (count args))
              (first args)
              ;;I hate seqs so much
              (vec (cons k args)))))])
   (into {})))

(defn remove-cruft [vs]
  (insta/transform cruft vs))
