(ns mcc.produce
  (:require
   [clojure.string :refer [split-lines trim join]]
   [clojure.pprint :refer [pprint]]
   [mcc.chunk :refer [add-type-to-chunks]]
   [mcc.util :refer [cart]]
   [clojure.spec :as s]))

(defmulti produce-text :mcc.chunk/type)

(defmethod produce-text nil [chunk]
      (pprint chunk)
      (list ""))

(defmethod produce-text :mcc.bundle/not-static-conditional [chunk]
      (list (:mcc.bundle/lines chunk)))

(defn chunks->strings [chunks]
 (->> chunks :mcc.chunk/chunks add-type-to-chunks (map produce-text)))

(defmethod produce-text :mcc.bundle/static-conditional
      [{:keys [mcc.chunk/if mcc.chunk/elif
               mcc.chunk/else mcc.chunk/endif] :as chunk}]
  (vector [(chunks->strings if)]
          (map chunks->strings elif)
          [(chunks->strings else)]
          [(chunks->strings endif)]))

(defn produce-strings [chunks]
    (map join (cart (map produce-text chunks))))
