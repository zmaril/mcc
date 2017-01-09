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
