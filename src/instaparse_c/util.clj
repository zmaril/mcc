(ns instaparse-c.util
  (:refer-clojure :exclude [cat])
  (:require [instaparse.combinators :refer :all]))

(defn altnt [& nts]
  (apply alt (map nt nts)))

(defn hs [& ss]
  (cat (apply cat (map string ss))))

(defn cat? [& cs]
  (opt (apply cat cs)))
