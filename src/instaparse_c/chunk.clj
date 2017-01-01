(ns instaparse-c.chunk
  (:require
   [clojure.string :refer [split-lines trim join]]
   [instaparse-c.util :refer [cart]]
   [clojure.spec :as s]))

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

(defn into-chunks [bundles]
      (let [chunks (s/conform (s/* :mcc/chunked) bundles)]
          (map (fn [[k m]] (assoc m :mcc.chunk/type k)) chunks)))

(defmulti produce-text :mcc.chunk/type)

(defmethod produce-text :mcc.bundle/not-static-conditional [chunk]
      (list (:mcc.bundle/text chunk)))

(defmethod produce-text :mcc.bundle/static-conditional [chunk]
      (list "" (join (map :mcc.bundle/text  (::bundles chunk))))) ;;uhhh

(defn produce-strings [chunks]
    (map join (cart (map produce-text chunks))))
