(ns instaparse-c.macro
  (:refer-clojure :exclude [cat comment])
  (:require 
   [instaparse.combinators :refer :all]
   [instaparse-c.util :refer :all]))

(def macro
  {:c11/macro
   (altnt :c11.macro/ifdef :c11.macro/if :c11.macro/include :c11.macro/define)
   :c11.macro/symbol (regexp "[A-Z_]+")
   :c11.macro/expr
   (altnt :c11.macro/symbol :c11.macro.expr/and :c11.macro.expr/not :c11.macro.expr/function :c11.macro/value)
   :c11.macro/value
   (regexp "[0-9]+")
   :c11.macro/define 
   (cat (hs "#" "define") (nt :c11.macro/symbol) (nt :c11.macro/expr))
   :c11.macro.expr/and
   (cat (nt :c11.macro/expr) (plus (cat (hs "&&") (nt :c11.macro/expr))))
   :c11.macro.expr/not
   (cat (hs "!") (nt :c11.macro/expr))
   :c11.macro.expr/function
   (cat (regexp "[a-z]+") (hs "(") (nt :c11.macro/expr) (hs ")"))

   :c11.macro/if (cat
                     (hs "#" "if")
                     (nt :c11.macro/expr)
                     (nt :c11)
                     (cat? (hs "#" "else")
                           (nt :c11))
                     (hs "#" "endif"))

   :c11.macro/ifdef (cat
                     (hs "#" "ifdef")
                     (nt :c11.macro/expr)
                     (nt :c11)
                     (cat? (hs "#" "else")
                           (nt :c11))
                     (hs "#" "endif"))
   :c11.macro/include
   (altnt :c11.macro.include/header :c11.macro.include/source)
   :c11.macro.include/header
   (cat (hs "#" "include" "<") (regexp "[a-z0-9/]+\\.h") (hs ">"))
   :c11.macro.include/source
   (cat (hs "#" "include" "\"") (regexp "[a-z0-9/]+\\.h") (hs "\""))
   })
