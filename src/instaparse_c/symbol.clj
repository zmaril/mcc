(ns instaparse-c.symbol 
  (:refer-clojure :exclude [cat comment string?])
  (:require 
   [instaparse.combinators :refer :all]
   [instaparse-c.util :refer :all]))

;;Symbols serve both macros and the c language proper
(def symbol
  {:c11/symbol (cat (neg (nt :c11/reserved))
                    (regexp "[a-zA-Z_][a-zA-Z_0-9]*"))
   :c11/reserved
   (alt (alts "extern" "void" "if" "NULL" "return")
        (nt :c11.data-type/storage)
        (nt :c11.data-type/qualifier)
        (nt :c11.data-type/specifier-keywords)
        )})
