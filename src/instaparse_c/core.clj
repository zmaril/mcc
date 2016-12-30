(ns instaparse-c.core
  (:refer-clojure :exclude [cat comment symbol if while for string? struct do])
  (:require
   [clojure.string :refer [split-lines trim join]]
   [clojure.core.match :refer [match]]
   [clojure.pprint :refer [pprint]]
   [clojure.spec :as s]
   [instaparse.combinators :refer :all]
   [instaparse.core :as insta]
   [instaparse-c.grammar :refer [grammar]]
   [instaparse-c.comment :refer [comment]]
   [instaparse-c.expression :refer [remove-cruft]]
   [instaparse-c.util :refer :all]))


(def whitespace
  (merge comment
         {:mcc/whitespace
          (plus (altnt :mcc.whitespace/characters :mcc/comment))
          :mcc.whitespace/characters
          (regexp "[\\s]+")}))

(def parse
  (insta/parser grammar
                :start :mcc/start
                :auto-whitespace
                (insta/parser whitespace
                              :start :mcc/whitespace)))

(defn clean-parse [& args]
  (let [parsed (apply parse args)]
    (if (insta/failure? parsed)
      parsed
      (remove-cruft parsed))))

(defn clean-parses [& args]
  (let [parsed (apply insta/parses (cons parse args))]
    (map remove-cruft parsed)))

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

(def testtest (slurp  "dev-resources/corpus/openssh-portable/testtest.c"))

(defmulti convert-bundle :type)

(defmethod convert-bundle :macro [{:keys [lines]}]
  (let [text (join "\n" lines)
        parsed (clean-preprocess text)]
       {:mcc.bundle/type (first (second parsed))
        :mcc.bundle/text text
        :mcc.bundle/parsed parsed}))


(defmethod convert-bundle :not-macro [{:keys [lines]}]
 {:mcc.bundle/type :mcc.bundle/raw-text
  :mcc.bundle/text (join "\n" lines)})

(defmethod convert-bundle nil [_] nil)

(defn into-bundles-step [{:keys [bundles current-bundle] :as state} line]
  (let [line (trim line)]
    (match [(first line) (last line) (:type current-bundle)]
           ;;Start of multiline macro
           [\# \\ _]
           {:bundles (conj bundles (convert-bundle current-bundle))
            :current-bundle {:type :macro
                             :lines [line]}
            :in-macro true}

           ;;Continue multiline macro
           [_  \\ :macro]
           (update-in state [:current-bundle :lines] conj line)

           ;;End of multiline macro
           [_  _ :macro]
           (let [new-bundle
                 (convert-bundle
                  (update current-bundle :lines conj line))]
                {:bundles (conj bundles new-bundle)
                 :current-bundle nil
                 :in-macro false})

           ;;Single line macro
           [\# _  _]
           {:bundles
            (conj bundles
                  (convert-bundle current-bundle)
                  (convert-bundle {:type :macro :lines [line]}))
            :current-bundle nil
            :in-macro false}

           ;;Add non macro lines to bundle
           [_ _ :not-macro]
           (update-in state [:current-bundle :lines] conj line)

           ;;Begin collecting non macro lines
           [_  _  _]
           {:bundles (conj bundles (convert-bundle current-bundle))
            :current-bundle {:type :not-macro :lines [line]}
            :in-macro false})))

(defn into-bundles [s]
  (let [tagged
        (reduce into-bundles-step
                {:bundles [] :current-bundle nil :in-macro false}
                (vec (take 1000 (split-lines s))))
        tagged
        (update tagged :bundles (partial filter identity))]
    (conj (vec (:bundles tagged)) (convert-bundle (:current-bundle tagged)))))

#_(map
   (comp clean-preprocess-line #(apply str %) :lines)
   (filter #(= :macro (:type %)) (tag-lines testtest)))

(def bundled (into-bundles testtest))
(def tags (map :type bundled))



(s/def :mcc.bundle/type keyword?) ;TOOD add all these in
                                 ;#{:mcc.bundle/raw-text :mcc.macro/if})
(s/def :mcc.bundle/text string?)
(s/def :mcc.bundle/parsed (constantly true)) ;;TODO
(s/def :mcc/bundle (s/keys :req [:mcc.bundle/type :mcc.bundle/text]
                           :opt [:mcc.bundle/parsed]))


(s/def ::static-conditional
   (s/cat ::conditional #{:mcc.macro/if :mcc.macro/ifdef}
          ::bundles (s/* ::bundle)
          ::else
          (s/* (s/cat :else #{:mcc.macro/else} ::bundles ::bundle))
          ::end #{:mcc.macro/endif}))

(s/def ::not-static-conditional
       (s/+ #{:mcc/raw-text :mcc.macro/include :mcc.macro/define}))


(s/def :mcc/chunked
       (s/alt :mcc.chunked/basic
              ::not-static-conditional
              :mcc.chunked/static-conditional
              ::static-conditional))

(s/def :mcc/bundles  (s/* :mcc/bundle))
(s/explain :mcc/bundles bundled)
(s/conform :mcc/bundles bundled)
