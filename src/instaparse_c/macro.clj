(ns instaparse-c.macro
  (:refer-clojure :exclude [cat comment string?])
  (:require 
   [instaparse.combinators :refer :all]
   [instaparse-c.util :refer :all]))

(def macro
  {:c11/macro
   (altnt :c11.macro/define :c11.macro/if :c11.macro/ifdef :c11.macro/include)

   :c11.macro/define 
   (cat (hs "#" "define")
        (altnt :c11.macro.define/value :c11.macro.define/function) )

   :c11.macro.define/value
   (cat (nt :c11/symbol) (nt :c11/expression))

   :c11.macro.define/function 
   (cat (nt :c11/symbol) (parens (list-of (nt :c11/symbol)))
        (brackets? (star (nt :c11/statement))))

   :c11.macro/if (cat
                  (hs "#" "if")
                  (nt :c11.macro/expr)
                  (nt :c11/statements)
                  (cat? (hs "#" "else")
                        (nt :c11/statements))
                  (hs "#" "endif"))

   :c11.macro/ifdef (cat
                     (hs "#" "ifdef")
                     (nt :c11.macro/expr)
                     (nt :c11/statements)
                     (cat? (hs "#" "else")
                           (nt :c11/statements))
                     (hs "#" "endif"))

   :c11.macro/include
   (altnt :c11.macro.include/header :c11.macro.include/source)
   :c11.macro.include/header
   (cat (hs "#" "include" "<") (regexp "[a-z0-9/]+\\.h") (hs ">"))
   :c11.macro.include/source
   (cat (hs "#" "include" "\"") (regexp "[a-z0-9/]+\\.h") (hs "\""))


   ;; The following uses https://en.wikipedia.org/wiki/Operator-precedence_parser
   ;; Removes ambiguity from the parses

   ;;I couldn't find an operator precedence chart for the c preproccesr
   ;;language, so I'm acting like the macro language is just a subset of the C language
   ;;  https://en.wikipedia.org/wiki/Operators_in_C_and_C%2B%2B

   ;;TODO: rework this to use the left-to-right operators
   :c11.macro/expr (nt :c11.macro.expr/or)

   :c11.macro.expr/or
   (cat (nt :c11.macro.expr.logical/or)
        (star (cat (hs "||") (nt :c11.macro.expr.logical/or))))

   :c11.macro.expr.logical/or
   (cat (nt :c11.macro.expr/and) (star (cat (hs "|") (nt :c11.macro.expr/and))))

   :c11.macro.expr/and
   (cat (nt :c11.macro.expr/not) (star (cat (hs "&&") (nt :c11.macro.expr/not))))

   :c11.macro.expr/not
   (cat (opt (hs "!")) (nt :c11.macro.expr/bottom))

   :c11.macro.expr/bottom
   (altnt :c11.macro/value :c11/symbol :c11.macro.expr/function :c11.macro.expr/parens)

   :c11.macro.expr/function
   (cat (regexp "[a-z]+") (hs "(") (nt :c11.macro/expr) (hs ")"))

   :c11.macro.expr/parens
   (parens (nt :c11.macro/expr))

   :c11.macro/value
   (regexp "[0-9]+")})

;;TODO remove cruft from precendece stuff
