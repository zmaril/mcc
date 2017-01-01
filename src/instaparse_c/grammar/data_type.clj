(ns instaparse-c.grammar.data-type 
  (:refer-clojure :exclude [cat comment string?])
  (:require 
   [instaparse.combinators :refer :all]
   [instaparse-c.util :refer :all]))

(def data-type
  {:mcc/data-type
   (cat 
    (star (nt :mcc.data-type/storage))
    (star (nt :mcc.data-type/qualifier))
    (nt :mcc.data-type/specifier))

   :mcc.data-type/storage (alts "typedef"
                                "extern"
                                "static"
                                "auto"
                                "register")

   :mcc.data-type/qualifier
   (alts "restrict" "volatile" "const")

   ;;TODO: this breaks validation, it produces combinations of specifiers that
   ;;aren't valid according to the mcc specification. 
   :mcc.data-type/specifier
    (alt
     (plus (nt :mcc.data-type/specifier-keywords))
     (altnt :mcc/symbol :mcc.data-type/struct :mcc.data-type/enum))

   :mcc.data-type/specifier-keywords
   (alts "void" "char" "short" "int" "long" "float" "double" "signed" "unsigned"
         "_Bool" "_Complex")

   :mcc.data-type/struct (cat (string "struct") (nt :mcc/symbol))
   :mcc.data-type/enum (cat (string "enum") (nt :mcc/symbol))
   })
