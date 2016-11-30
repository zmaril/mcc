(ns instaparse-c.util
  (:refer-clojure :exclude [cat string?])
  (:require [instaparse.combinators :refer :all]))

(defn altnt [& nts]
  (apply alt (map nt nts)))

(defn alts [& ss]
  (apply alt (map string ss)))

(defn hs [& ss]
  (hide (cat (apply cat (map string ss)))))

(def string? (comp opt string))

(defn cat? [& cs]
  (opt (apply cat cs)))

(defn alt? [& cs]
  (opt (apply alt cs)))

(defn parens [& cs]
  (cat (hs "(") (apply cat cs) (hs ")")))

(defn list-of [c]
  (opt (cat c (star (cat (hs ",") c))))
  )
