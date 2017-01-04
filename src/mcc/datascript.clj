(ns mcc.datascript
  (:require
   [clojure.data :refer [diff]]
   [datascript.core :as d]
   [com.rpl.specter :refer :all]))


(defn enlive-output->datascript-datums [m]
 (cond
    (map? m)
    (as-> m $
        (let [{:keys [:instaparse.gll/start-index  :instaparse.gll/end-index
                      :instaparse.gll/start-line   :instaparse.gll/end-line
                      :instaparse.gll/start-column :instaparse.gll/end-column]}
              (meta m)]
          (assoc $ :location {:index  [start-index end-index]
                              :line   [start-line  end-line]
                              :column [start-column end-column]}))
        (assoc $ :db/id (d/tempid :mcc))
        (transform [:content ALL] enlive-output->datascript-datums $))

    (vector? m)
    {:tag (first m)
     :content
     (map-indexed
      (fn [i v] (-> v enlive-output->datascript-datums (assoc :order i)))
      (rest m))}

    :default {#_:db/id #_(d/tempid :mcc) :type :value :value m}))

(def schema
 {:content {:db/cardinality :db.cardinality/many
            :db/valueType   :db.type/ref
            :db/isComponent true}})
