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
  {:c11/expression (nt :c11/comma) 
   :c11.statement/expression (cat (nt :c11/expression) (string ";") )
   :c11/comma
   (left-to-right* :c11.expression/assignment (hs ","))

   :c11.expression/assignment 
   (left-to-right*
    :c11.expression/ternary
    (alts "=" "+=" "-=" "*=" "/=" "%=" "<<=" ">>=" "&=" "^=" "|="))
   
   :c11.expression/ternary 
   (cat
    (nt :c11.expression.logical/or)
    (cat? 
     (hs "?")
     (nt :c11.expression.logical/or)
     (hs ":")
     (nt :c11.expression.logical/or)))

   :c11.expression.logical/or
   (left-to-right* :c11.expression.logical/and (alts "or" "||"))

   :c11.expression.logical/and 
   (left-to-right* :c11.expression.bitwise/or (alts "and" "&&"))

   :c11.expression.bitwise/or 
   (left-to-right* :c11.expression.bitwise/xor (string "|"))

   :c11.expression.bitwise/xor 
   (left-to-right* :c11.expression.bitwise/and (string "^"))

   :c11.expression.bitwise/and 
   (cat
    (cat? (nt :c11.expression/equality) (string "&") (neg (string "&")))
    (nt :c11.expression/equality))

   :c11.expression/equality
   (left-to-right* :c11.expression/comparsion (alts "==" "!="))

   :c11.expression/comparsion
   (left-to-right* :c11.expression.bitwise/shift (alts ">" ">=" "<="  "<"))

   :c11.expression.bitwise/shift 
   (left-to-right* :c11.expression.arthimetic/addition (alts "<<" ">>"))

   :c11.expression.arthimetic/addition 
   (left-to-right* :c11.expression.arthimetic/subtraction (string "+"))

   :c11.expression.arthimetic/subtraction
   (left-to-right* :c11.expression.arthimetic/multiplication (string "-"))

   :c11.expression.arthimetic/multiplication
   (left-to-right* :c11.expression.arthimetic/division (string "*"))

   :c11.expression.arthimetic/division
   (left-to-right* :c11.expression.arthimetic/modulo (string "/"))

   :c11.expression.arthimetic/modulo
   (left-to-right* :c11.expression/prefix-operation (string "%"))

   ;;To allow unary operations to commute while still using the parsing tree
   ;;method, and while also avoiding an infinite loop with :c11/expression at
   ;;the top and bottom of the tree, we consider all unary operators to be the
   ;;same non terminal while parsing.
   ;;TODO: Have a transformation that cleans this up and introduces better
   ;;labels, as one would expect.
   :c11.expression/prefix-operation
   (cat
    (star
     (alt 
      (alts "++" "--" "!" "~" "*" "&")
      (cat (string "+") (neg (string "+")))
      (cat (string "-") (neg (string "-")))

      ;;TODO: how does the * character interact with data-type declarations? 
      ;;Cast
      (parens (cat (nt :c11/data-type) (string? "*")))
      ))
    (nt :c11.expression/size-of))
   

   :c11.expression/size-of
   (cat (string? "sizeof") (nt :c11.expression/array-subscript))


   ;;TODO:Cruft does weird things here
   :c11.expression/array-subscript
   (cat (nt :c11.expression.member/select-through-pointer)
        (cat?
         (string "[")
         (nt? :c11/expression)
         (string "]")))

   :c11.expression.member/select-through-pointer
   (left-to-right :c11.expression.member/select-by-reference (string "->"))

   :c11.expression.member/select-by-reference
   (left-to-right :c11.expression/postfix-increment (string "."))

   :c11.expression/postfix-increment
   (postfix-unary :c11.expression/postfix-decrement "++")

   :c11.expression/postfix-decrement
   (postfix-unary :c11.expression/bottom "--")

   :c11.expression/bottom
   (altnt :c11/literal :c11/symbol :c11.expression/function-call
          :c11.expression/parens)


   ;;TODO: Inside of a function call, comma operators don't exist. By skipping
   ;;past comma as the first tip top part of the expression precedence tree, we
   ;;lose [:c11/expression] for each parse. One option is to remove the comma
   ;;operator from the parse tree and have it be it's own thing. Another option
   ;;is to have transform at the end that wraps expressions inside of function
   ;;calls.

   :c11.expression/function-call
   (alt 
    (cat (nt :c11/symbol)
         (parens (list-of? (nt :c11.expression/assignment)))))

   :c11.expression/parens
   (parens (nt :c11/expression))
   
   :c11.expression/subscript
   (cat
    (nt :c11/expression)
    (hs "[")
    (nt :c11/expression)
    (hs "]"))})

;;Right an assert that checks all the keys are present
(def cruft-pieces
  [:c11.expression.arthimetic/addition
   :c11.expression.arthimetic/division
   :c11.expression.arthimetic/modulo
   :c11.expression.arthimetic/multiplication
   :c11.expression.arthimetic/subtraction
   :c11.expression.bitwise/and
   :c11.expression.bitwise/not
   :c11.expression.bitwise/or
   :c11.expression.bitwise/shift
   :c11.expression.bitwise/xor
   :c11.expression.logical/and
   :c11.expression.logical/not
   :c11.expression.logical/or
   :c11.expression/address-of
   :c11.expression/assignment
   :c11.expression/bottom
   :c11.expression/comparsion
   :c11.expression/equality
   :c11.expression/function-call
   :c11.expression/indirection
   :c11.expression/parens
   :c11.expression/postfix-decrement
   :c11.expression/postfix-increment
   :c11.expression/prefix-decrement
   :c11.expression/prefix-increment
   :c11.expression/size-of
   :c11.expression/subscript
   :c11.expression/type
   :c11.expression/unary-minus
   :c11.expression/unary-plus
   :c11.expression/ternary
   :c11.expression/array-subscript
   :c11.expression.member/select-through-pointer
   :c11.expression.member/select-by-reference
   :c11.expression/prefix-operation
   :c11/comma])

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
