(ns instaparse-c.core-test
  (:require [clojure.test :refer :all]
            [instaparse-c.core :as c]
            [instaparse.core :as insta]
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

(parse-file "dev-resources/corpus/openssh-portable/scp.c" )
