(ns instaparse-c.util
  (:refer-clojure :exclude [cat string?])
  (:require [instaparse.combinators :refer :all]))

(defn altnt [& nts]
  (apply alt (map nt nts)))

(defn alts [& ss]
  (apply alt (map string ss)))

(def alts?
  (comp opt alts))

(defn hs [& ss]
  (hide (cat (apply cat (map string ss)))))

(def hs? (comp opt hs))

(def string? (comp opt string))
(def nt? (comp opt nt))

(defn cat? [& cs]
  (opt (apply cat cs)))

(defn alt? [& cs]
  (opt (apply alt cs)))

(defn parens [& cs]
  (cat (hs "(") (apply cat cs) (hs ")")))

(defn brackets [& cs]
  (cat (hs "{") (apply cat cs) (hs "}")))

(defn brackets? [& cs]
  (opt (apply brackets cs)))

(defn list-of [c]
  (cat c (star (cat (hs ",") c))))

(defn list-of? [c]
  (opt (list-of c)))
