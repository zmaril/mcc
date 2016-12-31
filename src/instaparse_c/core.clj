(ns instaparse-c.core
  (:refer-clojure :exclude [cat comment struct do])
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
   [instaparse-c.util :refer [altnt]]
   [clojure.data :refer [diff]]
   [com.rpl.specter :refer :all]
   [datascript.core :as d]))

;;(insta/set-default-output-format! :enlive)
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



(s/def :mcc.bundle/type
       #{:mcc.macro/if
         :mcc.macro/ifdef
         :mcc.macro/else
         :mcc.macro/endif
         :mcc.bundle/raw-text
         :mcc.macro/include
         :mcc.macro/define})

(s/def :mcc.bundle/text string?)
(s/def :mcc.bundle/parsed vector?) ;;TODO
(s/def :mcc/bundle (s/keys :req [:mcc.bundle/type :mcc.bundle/text]
                           :opt [:mcc.bundle/parsed]))

(defn bundle-of [tags]
     (s/and :mcc/bundle
            (fn [bundle] (-> bundle :mcc.bundle/type tags boolean))))

(s/def ::static-conditional
   (s/cat ::conditional (bundle-of #{:mcc.macro/if :mcc.macro/ifdef})
          ::bundles (s/* :mcc/bundle)
          ::else
          (s/*
             (s/cat :else (bundle-of #{:mcc.macro/else})
                    ::bundles :mcc/bundle))
          ::end (bundle-of #{:mcc.macro/endif})))


(s/def ::not-static-conditional
       (bundle-of
             #{:mcc.bundle/raw-text
               :mcc.macro/include
               :mcc.macro/define}))

(s/def :mcc/chunked
       (s/alt :mcc.bundle/not-static-conditional ::not-static-conditional
             :mcc.bundle/static-conditional     ::static-conditional))

(s/def :mcc/bundles  (s/* :mcc/bundle))
(s/explain :mcc/bundles bundled)
(s/conform :mcc/bundles bundled)

(s/explain (s/* :mcc/chunked) bundled)
(s/conform (s/* :mcc/chunked) bundled)

(defn into-chunks [bundles]
      (let [chunks (s/conform (s/* :mcc/chunked) bundles)]
          (map (fn [[k m]] (assoc m :mcc.chunk/type k)) chunks)))

(def test-chunks (into-chunks bundled))

(defmulti produce-text :mcc.chunk/type)

(defmethod produce-text :mcc.bundle/not-static-conditional [chunk]
      (list (:mcc.bundle/text chunk)))

(defmethod produce-text :mcc.bundle/static-conditional [chunk]
      (list "" (join (map :mcc.bundle/text  (::bundles chunk))))) ;;uhhh

(defn cart [colls]
  (if (empty? colls)
    '(())
    (for [x (first colls)
          more (cart (rest colls))]
      (cons x more))))

(defn produce-strings [chunks]
    (map join (cart (map produce-text chunks))))

(map join (cart (map produce-text test-chunks)))

(insta/set-default-output-format! :enlive)

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
  (let [parsed (apply parse args)
        parsed (insta/add-line-and-column-info-to-metadata (first args) parsed)]
    (if (insta/failure? parsed)
      parsed
      (remove-cruft parsed))))

(defn clean-parses [& args]
  (let [parsed (apply insta/parses (cons parse args))]
    (map remove-cruft parsed)))

(apply diff (map clean-parse (produce-strings test-chunks)))

(def testparse
 (clean-parse (first (produce-strings test-chunks))))


(defn enlive-output->datascript-datums [m]
 (cond
    (map? m)
    (as-> m $
        #_(assoc $ :meta (meta m))
        (assoc $ :db/id (d/tempid :mcc))
        (transform [:content ALL] enlive-output->datascript-datums $))

    (vector? m)
    {:tag (first m)
     :content
     (map-indexed
      (fn [i v] (-> v enlive-output->datascript-datums (assoc :order i)))
      (rest m))}

    :default {:db/id (d/tempid :mcc) :type :value :value m}))
(def metad (enlive-output->datascript-datums testparse))

(def schema
 {:content {:db/cardinality :db.cardinality/many
            :db/valueType   :db.type/ref
            :db/isComponent true}})


(def conn (d/create-conn schema))
(d/transact! conn [metad])

(def printf
 (d/q '[:find  ?prize
        :where [?value :value "printf"]
               [?symbol :content ?value]
               [?fun-call :content ?symbol]
               [?fun-call :content ?args]
               [?args :order 1]
               [?args :content ?temp1]
               [?temp1 :content ?temp2]
               [?temp2 :value ?prize]]
      @conn))
