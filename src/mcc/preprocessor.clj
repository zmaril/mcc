(ns mcc.preprocessor
  (:refer-clojure :exclude [cat comment struct do])
  (:require
   [instaparse.core :as insta]
   [instaparse.combinators :refer :all]
   [mcc.grammar.core :refer [grammar]]
   [mcc.grammar.expression :refer [remove-cruft]]))

(def preprocessor-whitespace
  {:mcc.macro/whitespace (plus (regexp "([ \t]|(\\\\\n))+"))})

(def preprocess
  (insta/parser grammar
                :start :mcc/macro
                :auto-whitespace
                (insta/parser preprocessor-whitespace
                              :start :mcc.macro/whitespace)))

(def clean-preprocess
   (comp remove-cruft preprocess))
