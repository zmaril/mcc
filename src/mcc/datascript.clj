(ns mcc.datascript
  (:require
   [clojure.data :refer [diff]]
   [datascript.core :as d]
   [com.rpl.specter :refer :all]))


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

(def schema
 {:content {:db/cardinality :db.cardinality/many
            :db/valueType   :db.type/ref
            :db/isComponent true}})
