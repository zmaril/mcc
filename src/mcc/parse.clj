(ns mcc.parse
  (:refer-clojure :exclude [cat comment struct do string?])
  (:require
   [instaparse.combinators :refer :all]
   [instaparse.core :as insta]
   [mcc.grammar.core :refer [grammar]]
   [mcc.util :refer :all]
   [mcc.grammar.expression :refer [remove-cruft]]
   [mcc.grammar.comment :refer [comment]]))

(def whitespace
  (merge comment
         {:mcc/whitespace
          (plus (altnt :mcc.whitespace/characters :mcc/comment))
          :mcc.whitespace/characters
          (regexp "[\\s]+")}))

(def parse
  (insta/parser grammar
                :start :mcc/start
                :output-format :enlive
                :auto-whitespace
                (insta/parser whitespace
                              :start :mcc/whitespace)))

(defn clean-parse [& args]
  (let [parsed (apply parse args)
        parsed (insta/add-line-and-column-info-to-metadata (first args) parsed)]
    (if (insta/failure? parsed)
      parsed
      (remove-cruft parsed))))

(defn clean-parses [& args]
  (let [parsed (apply insta/parses (cons parse args))]
    (map remove-cruft parsed)))
