(ns instaparse-c.core
  (:refer-clojure :exclude [cat comment symbol if while for string? struct do])
  (:require
   [clojure.string :refer [split-lines trim]]
   [clojure.core.match :refer [match]]
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



(def testtest (slurp  "dev-resources/corpus/openssh-portable/scp.c"))

(def preprocessor-whitespace
  {:mcc.raw/whitespace (plus (regexp "[ \t]+"))})

(def preprocess
  (insta/parser grammar
                :start :mcc/raw
                :auto-whitespace
                (insta/parser preprocessor-whitespace
                              :start :mcc.raw/whitespace)))

(defn clean-preprocess [& args]
  (let [parsed (apply preprocess args)]
    (if (insta/failure? parsed)
      parsed
      (remove-cruft parsed))))

;;TODO: weird function names
(defn clean-preprocess* [& args]
  (let [parsed (apply insta/parses (cons preprocess args))]
    (map remove-cruft parsed)))

(defn tag-lines-step [{:keys [bundles current-bundle] :as state} line]
  (let [line (trim line)]
    (match [(first line) (last line) (:type current-bundle)]

           [\# \\ _]
           {:bundles (conj bundles current-bundle)
            :current-bundle {:type :macro
                             :lines [line]}
            :in-macro true}

           [_  \\ :macro]
           (update-in state [:current-bundle :lines] conj line)

           [_  _ :macro]
           {:bundles
            (conj bundles
                  (update current-bundle :lines conj line))
            :current-bundle nil
            :in-macro false}

           [\# _  _]
           {:bundles
            (conj bundles
                  current-bundle
                  {:type :macro :lines [line]})
            :current-bundle nil
            :in-macro false}


           [_ _ :not-macro]
           (update-in state [:current-bundle :lines] conj line)

           [_  _  _]
           {:bundles (conj bundles current-bundle)
            :current-bundle {:type :not-macro
                             :lines [line]}
            :in-macro false})))

(defn tag-lines [s]
  (let [tagged
        (reduce tag-lines-step
                {:bundles [] :current-bundle nil :in-macro false}
                (vec (take 1000 (split-lines s))))
        tagged
        (update tagged :bundles (partial filter identity))]
    (conj (vec (:bundles tagged)) (:current-bundle tagged))))

(filter #(= :macro (:type %)) (tag-lines testtest))

(defn collect-bundles-step [{:keys [bundles current-bundle] :as state} line]
  (let [line (trim line)]
    (match [(first line) (last line) (:type current-bundle)]

           [\# \\ _]
           {:bundles (conj bundles current-bundle)
            :current-bundle {:type :macro
                             :lines [line]}
            :in-macro true}

           [_  \\ :macro]
           (update-in state [:current-bundle :lines] conj line)

           [_  _ :macro]
           {:bundles
            (conj bundles
                  (update current-bundle :lines conj line))
            :current-bundle nil
            :in-macro false}

           [\# _  _]
           {:bundles
            (conj bundles
                  current-bundle
                  {:type :macro :lines [line]})
            :current-bundle nil
            :in-macro false}


           [_ _ :not-macro]
           (update-in state [:current-bundle :lines] conj line)

           [_  _  _]
           {:bundles (conj bundles current-bundle)
            :current-bundle {:type :not-macro
                             :lines [line]}
            :in-macro false})))

(defn collect-bundles [tagged]
  (let [bundled
        (reduce collect-bundles-step
                {:bundles [] :current-bundle nil :in-macro false} tagged)

        bundled
        (update bundled :bundles (partial filter identity))]
    (conj (vec (:bundles bundled)) (:current-bundle bundled))))
