(ns mcc.bundle
  (:require
   [mcc.preprocessor :refer [clean-preprocess]]
   [clojure.string :refer [split-lines trim join starts-with? ends-with?]]
   [clojure.spec :as s]
   [com.rpl.specter :refer :all]
   [clojure.core.match :refer [match]]))

(defmulti convert-bundle :type)

(defmethod convert-bundle :macro [{:keys [lines]}]
  (let [text (join "\n" (map second lines))
        parsed (clean-preprocess text)]
       {:mcc.bundle/type (first (second parsed))
        :mcc.bundle/lines lines}))


(defmethod convert-bundle :not-macro [{:keys [lines]}]
 {:mcc.bundle/type :mcc.bundle/raw-text
  :mcc.bundle/lines lines})

(defmethod convert-bundle nil [_] nil)

(defn into-bundles-step [{:keys [bundles current-bundle] :as state}
                         [line-number text :as line]]
  (let [text (trim text)]
    (match [(first text) (last text) (:type current-bundle)]
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
                (vec (map-indexed vector (split-lines s))))
        tagged
        (update tagged :bundles (partial filter identity))]
    (conj (vec (:bundles tagged)) (convert-bundle (:current-bundle tagged)))))

(s/def ::type
       #{:mcc.macro/if
         :mcc.macro/ifdef
         :mcc.macro/ifndef
         :mcc.macro/elif
         :mcc.macro/else
         :mcc.macro/endif
         :mcc.bundle/raw-text
         :mcc.macro/include
         :mcc.macro/define})

(s/def ::line (s/tuple int? string?))
(s/def ::lines (s/* ::line))
(s/def ::parsed vector?) ;;TODO
(s/def ::bundle (s/keys :req [::type ::lines]
                        :opt [::parsed]))


(defn starts-with-hash? [s] (starts-with? (second s) "#"))
(defn ends-with-slash? [s] (ends-with? (second s) "\\"))

(s/def ::not-macro
 (s/and
       ::line
       (complement starts-with-hash?)
       (complement ends-with-slash?)))

(s/def ::single-line-macro
 (s/and ::line
        starts-with-hash?
        (complement ends-with-slash?)))

(s/def ::multi-line-macro
 (s/cat :start (s/and ::line
                starts-with-hash?
                ends-with-slash?)
        :cont
         (s/* (s/and
                 ::line
                 (complement starts-with-hash?)
                 ends-with-slash?))
        :finish ::not-macro))

(s/def ::bundled (s/* (s/alt ::single-line-macro ::single-line-macro
                             ::multi-line-macro  ::multi-line-macro
                             ::raw-text          (s/+ ::not-macro))))


(defn into-bundles2 [s]
 (let [split-up (vec (map-indexed vector (split-lines s)))]
   (s/conform ::bundled split-up)))
;;TODO: someday when I understand spec better this can be used, until then, nope
