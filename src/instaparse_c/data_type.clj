(ns instaparse-c.data-type 
  (:refer-clojure :exclude [cat comment string?])
  (:require 
   [instaparse.combinators :refer :all]
   [instaparse-c.util :refer :all]))

(def data-type
  {:c11/data-type
   (cat 
    (star (nt :c11.data-type/storage))
    (star (nt :c11.data-type/qualifier))
    (nt :c11.data-type/specifier))

   :c11.data-type/storage (alts "typedef"
                                "extern"
                                "static"
                                "auto"
                                "register")

   :c11.data-type/qualifier
   (alts "restrict" "volatile" "const")

   ;;TODO: this breaks validation, it produces combinations of specifiers that
   ;;aren't valid according to the c11 specification. 
   :c11.data-type/specifier
    (alt
     (plus (nt :c11.data-type/specifier-keywords))
     (altnt :c11/symbol :c11.data-type/struct :c11.data-type/enum))

   :c11.data-type/specifier-keywords
   (alts "void" "char" "short" "int" "long" "float" "double" "signed" "unsigned"
         "_Bool" "_Complex")

   :c11.data-type/struct (cat (string "struct") (nt :c11/symbol))
   :c11.data-type/enum (cat (string "enum") (nt :c11/symbol))
   })
