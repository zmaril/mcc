(ns mcc.chunk
  (:require
   [clojure.string :refer [split-lines trim join]]
   [clojure.pprint :refer [pprint]]
   [mcc.util :refer [cart]]
   [clojure.spec :as s]))

(defn bundle-of [tags]
     (s/and :mcc.bundle/bundle
            (fn [bundle] (-> bundle :mcc.bundle/type tags boolean))))

(s/def ::static-conditional
   (s/cat ::if
          (s/cat ::conditional
                 (bundle-of #{:mcc.macro/if :mcc.macro/ifdef
                              :mcc.macro/ifndef})
                 ::chunks (s/* :mcc/chunked))
          ::elif
          (s/*
           (s/cat :conditional (bundle-of #{:mcc.macro/elif})
                  ::chunks (s/* :mcc/chunked)))
          ::else
          (s/?
             (s/cat :conditional (bundle-of #{:mcc.macro/else})
                    ::chunks (s/* :mcc/chunked)))
          ::endif (bundle-of #{:mcc.macro/endif})))


(s/def ::not-static-conditional
       (bundle-of
             #{:mcc.bundle/raw-text
               :mcc.macro/include
               :mcc.macro/define}))

(s/def :mcc/chunked
       (s/alt :mcc.bundle/not-static-conditional ::not-static-conditional
              :mcc.bundle/static-conditional     ::static-conditional))

(s/def :mcc/bundles  (s/* :mcc/bundle))

(defn conform-chunks [bundles]
 (s/conform (s/* :mcc/chunked) bundles))

(defn add-type-to-chunks [chunks]
 (map (fn [[k m]] (assoc m :mcc.chunk/type k)) chunks))

(defn into-chunks [bundles]
      (-> bundles conform-chunks add-type-to-chunks))


(defmulti produce-text :mcc.chunk/type)

(defmethod produce-text :mcc.bundle/not-static-conditional [chunk]
      (list (:mcc.bundle/text chunk)))

(declare produce-strings)

(defmethod produce-text :mcc.bundle/static-conditional
      [{:keys [mcc.chunk/conditional mcc.chunk/elif
               mcc.chunk/else mcc.chunk/endif] :as chunk}]
      (let [conditional-data
             {:directive (-> conditional :mcc.chunk/conditional :mcc.bundle/number-of-lines)
              :code      (-> conditional :mcc.chunk/chunks
                          add-type-to-chunks produce-strings)}
            else-data
             {:directive (-> else :else :mcc.bundle/number-of-lines)
              :code      (-> else :mcc.chunk/chunks add-type-to-chunks produce-strings)}
            endif-data
            {:directive (-> endif :mcc.bundle/number-of-lines dec)
             :code      '("")} ;;#endifs don't ever have any extra space beyond the number of lines they use
            elif-data
            (map
             (fn [m]
              {:directive (-> m :elif :mcc.bundle/number-of-lines)
               :code      (-> m :mcc.chunk/chunks
                           add-type-to-chunks produce-strings)})
             elif)
            data (concat [conditional-data] elif-data [else-data] [endif-data])]
           #_(map-indexed)
           #_(for [i (range (count data))]))



      (list "" (join (map :mcc.bundle/text  (::bundles chunk)))))

(defn produce-strings [chunks]
    (map join (cart (map produce-text chunks))))
