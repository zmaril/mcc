(ns mcc.grammar.macro
  (:refer-clojure :exclude [cat comment string?])
  (:require 
   [instaparse.combinators :refer :all]
   [mcc.util :refer :all]))

(def macro
  {:mcc/macro
   (altnt :mcc.macro/define :mcc.macro/if :mcc.macro/ifdef :mcc.macro/include)

   :mcc.macro/define 
   (cat (hs "#" "define")
        (altnt :mcc.macro.define/value :mcc.macro.define/function) )

   :mcc.macro.define/value
   (cat (nt :mcc/symbol) (nt :mcc/expression))

   :mcc.macro.define/function 
   (cat (nt :mcc/symbol) (parens (list-of (nt :mcc/symbol)))
        (brackets? (star (nt :mcc/statement))))

   :mcc.macro/if (cat
                  (hs "#" "if")
                  (nt :mcc.macro/expr)
                  (nt :mcc/statements)
                  (cat? (hs "#" "else")
                        (nt :mcc/statements))
                  (hs "#" "endif"))

   :mcc.macro/ifdef (cat
                     (hs "#" "ifdef")
                     (nt :mcc.macro/expr)
                     (nt :mcc/statements)
                     (cat? (hs "#" "else")
                           (nt :mcc/statements))
                     (hs "#" "endif"))

   :mcc.macro/include
   (altnt :mcc.macro.include/header :mcc.macro.include/source)
   :mcc.macro.include/header
   (cat (hs "#" "include" "<") (regexp "[a-z0-9/]+\\.h") (hs ">"))
   :mcc.macro.include/source
   (cat (hs "#" "include" "\"") (regexp "[a-z0-9/]+\\.h") (hs "\""))


   ;; The following uses https://en.wikipedia.org/wiki/Operator-precedence_parser
   ;; Removes ambiguity from the parses

   ;;I couldn't find an operator precedence chart for the c preproccesr
   ;;language, so I'm acting like the macro language is just a subset of the C language
   ;;  https://en.wikipedia.org/wiki/Operators_in_C_and_C%2B%2B

   ;;TODO: rework this to use the left-to-right operators
   :mcc.macro/expr (nt :mcc.macro.expr/or)

   :mcc.macro.expr/or
   (cat (nt :mcc.macro.expr.logical/or)
        (star (cat (hs "||") (nt :mcc.macro.expr.logical/or))))

   :mcc.macro.expr.logical/or
   (cat (nt :mcc.macro.expr/and) (star (cat (hs "|") (nt :mcc.macro.expr/and))))

   :mcc.macro.expr/and
   (cat (nt :mcc.macro.expr/not) (star (cat (hs "&&") (nt :mcc.macro.expr/not))))

   :mcc.macro.expr/not
   (cat (opt (hs "!")) (nt :mcc.macro.expr/bottom))

   :mcc.macro.expr/bottom
   (altnt :mcc.macro/value :mcc/symbol :mcc.macro.expr/function :mcc.macro.expr/parens)

   :mcc.macro.expr/function
   (cat (regexp "[a-z]+") (hs "(") (nt :mcc.macro/expr) (hs ")"))

   :mcc.macro.expr/parens
   (parens (nt :mcc.macro/expr))

   :mcc.macro/value
   (regexp "[0-9]+")})

;;TODO remove cruft from precendece stuff
