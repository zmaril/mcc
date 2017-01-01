(ns mcc.bundle
  (:require
   [mcc.preprocessor :refer [clean-preprocess]]
   [clojure.string :refer [split-lines trim join]]
   [clojure.core.match :refer [match]]))

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
