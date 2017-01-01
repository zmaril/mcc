
(ns mcc.grammar.symbol
  (:refer-clojure :exclude [cat comment string? symbol])
  (:require
   [instaparse.combinators :refer :all]
   [mcc.util :refer :all]))

;;Symbols serve both macros and the c language proper
(def symbol
  {:mcc/symbol (cat (neg (nt :mcc/reserved))
                    (regexp "[a-zA-Z_][a-zA-Z_0-9]*"))
   :mcc/reserved
   (alt (alts "extern" "void" "if" "NULL" "return")
        (nt :mcc.data-type/storage)
        (nt :mcc.data-type/qualifier)
        (nt :mcc.data-type/specifier-keywords))})
