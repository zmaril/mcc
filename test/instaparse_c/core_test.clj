(ns instaparse-c.core-test
  (:require [clojure.test :refer :all]
            [instaparse-c.core :as c]
            [instaparse.core :as insta]
            [clojure.data :as data]
            [clojure.java.io :refer [file]]
            [clojure.pprint :refer [pprint]]
            [me.raynes.fs :refer [glob]]))
(defn walk [dirpath pattern]
  (doall (filter #(re-matches pattern (.getName %))
                 (file-seq (file dirpath)))))

(def corpus (take 1 (walk "dev-resources" #".*\.c")))

(defn parse-file [file-name]
  (c/clean-parse (slurp file-name)))


(defn preprocess-file [file-name]
  (c/clean-preprocess* (slurp file-name)))

(deftest can-we-parse-everything
  (doseq [f corpus] 
         (testing (str "Testing: " (.getPath f))
           (is (not (insta/failure? (parse-file f)))))))


(def lines
  (->> "dev-resources/corpus/openssh-portable/scp.c"
       slurp
       clojure.string/split-lines))

(defn up-to [n]
  (->> lines 
       (take n) 
       (clojure.string/join "\n")))


(defn line-by-line [start stop]
  (doseq [i (range start stop)]
    (let [parses (atom nil)
          timed
          (with-out-str
            (time
             (reset! parses (insta/parses c/parse (up-to i) :unhide :all))))
          parses @parses]
      (when (not= 0 (count parses))
        (println i (count parses) timed)))))

(defn show
  ([n] (show n (inc n)) )
  ([i j]
   (->> lines
        (take j)
        (drop i)
        (clojure.string/join "\n")
        (println))))

(def t
  "
			if (a)
#ifdef A
				if (a) {
#else
				if (a) {
#endif 
				  a = a;	
				}
  ")

(def t
  "
		if (a) {
			if (a)
  #ifdef A
				if (a) {
  #else
				if (a) {
  #endif 
				  a = a;	
				}
		}
  ")

(def a "if (a) {a =a;} if (c){a = 1;}")
(def b "if (b) {a =a;} if (c){a=2;}")
(def parsed-a (c/clean-parse a))
(def parsed-b (c/clean-parse b))

#_(c/parse t)
#_(count  (c/clean-parses t :unhide :all))
#_(line-by-line 150 170)

#_(parse-file "dev-resources/corpus/openssh-portable/scp.c" )

(def scp (slurp  "dev-resources/corpus/openssh-portable/scp.c"))

;(line-by-line 0 200)


