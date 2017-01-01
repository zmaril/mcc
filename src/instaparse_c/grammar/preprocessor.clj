(ns instaparse-c.preprocessor
  (:refer-clojure :exclude [cat comment string?])
  (:require
   [instaparse.combinators :refer :all]
   [instaparse-c.util :refer :all]
   [instaparse-c.expression :refer [remove-cruft]]))

(def preprocessor
  {
   :mcc/macro
   (altnt :mcc.macro/define :mcc.macro/if :mcc.macro/ifdef :mcc.macro/include
          :mcc.macro/else :mcc.macro/endif)

   :mcc.macro/define
   (cat (hs "#" "define")
        (altnt :mcc.macro.define/value :mcc.macro.define/function))

   :mcc.macro.define/value
   (cat (nt :mcc/symbol) (nt :mcc/expression))

   :mcc.macro.define/function
   (cat (nt :mcc/symbol) (parens (list-of (nt :mcc/symbol)))
        (brackets? (star (nt :mcc/statement))))

   :mcc.macro/if (cat
                  (hs "#" "if")
                  (nt :mcc/expression))

   :mcc.macro/else (hs "#" "else")
   :mcc.macro/endif (hs "#" "endif")

   ;;TODO: ifndef
   :mcc.macro/ifdef (cat
                     (hs "#" "ifdef")
                     (nt :mcc/expression))

   :mcc.macro/include
   (altnt :mcc.macro.include/header :mcc.macro.include/source)
   :mcc.macro.include/header
   (cat (hs "#" "include" "<") (regexp "[a-z0-9/]+\\.h") (hs ">"))
   :mcc.macro.include/source
   (cat (hs "#" "include" "\"") (regexp "[a-z0-9/]+\\.h") (hs "\""))})
