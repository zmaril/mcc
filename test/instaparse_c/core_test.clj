(ns instaparse-c.core-test
  (:require [clojure.test :refer :all]
            [instaparse-c.core :as c]
            [instaparse.core :as insta]
            [clojure.data :as data]
            [clojure.java.io :refer [file]]
            [me.raynes.fs :refer [glob]]))
(defn walk [dirpath pattern]
  (doall (filter #(re-matches pattern (.getName %))
                 (file-seq (file dirpath)))))

(def corpus (take 1 (walk "dev-resources" #".*\.c")))

(defn parse-file [file-name]
  (c/parse (slurp file-name)))

(deftest can-we-parse-everything
  (doseq [f corpus] 
         (testing (str "Testing: " (.getPath f))
           (is (not (insta/failure? (parse-file f)))))))

;(parse-file "dev-resources/corpus/openssh-portable/scp.c" )

(def t
  "if (d) {k();
	}")

(def s
  "
		while (a == -1 && errno == EINTR);
")

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
    (let [parses  (insta/parses c/parse (up-to i) :unhide :all)]
      (println i (count parses)))))

(defn show
  ([n] (show n (inc n)) )
  ([i j]
   (->> lines
        (take j)
        (drop i)
        (clojure.string/join "\n")
        (println))))

(count  (insta/parses c/parse t :unhide :all))
#_(line-by-line 150 170)
