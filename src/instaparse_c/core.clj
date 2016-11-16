(ns instaparse-c.core
  (:refer-clojure :exclude [cat comment])
  (:require 
   [instaparse.core :as insta]
   [instaparse.combinators :refer :all]
   [instaparse-c.util :refer :all]))

(def comment
  {:c11.comment (altnt :c11.comment/mutliple-lines
                       :c11.comment/single-line)
   :c11.comment/mutliple-lines
   (cat (hs "/*")
        (regexp "([\\s\\S])*?(?=\\*/)")
        (hs "*/"))
   :c11.comment/single-line
   (cat (hs "//")
        (regexp ".*")
        (hs "\n"))})

(def macro
  {:c11.macro
   (altnt :c11.macro/ifdef :c11.macro/if :c11.macro/include)
   :c11.macro/symbol (regexp "[A-Z_]+")
   :c11.macro/expr
   (altnt :c11.macro/symbol :c11.macro.expr/and :c11.macro.expr/not :c11.macro.expr/function)
   :c11.macro.expr/and
   (cat (nt :c11.macro/expr) (plus (cat (hs "&&") (nt :c11.macro/expr))))
   :c11.macro.expr/not
   (cat (hs "!") (nt :c11.macro/expr))
   :c11.macro.expr/function
   (cat (regexp "[a-z]+") (hs "(") (nt :c11.macro/expr) (hs ")"))

   :c11.macro/if (cat
                     (hs "#" "if")
                     (nt :c11.macro/expr)
                     (nt :c11)
                     (cat? (hs "#" "else")
                           (nt :c11))
                     (hs "#" "endif"))

   :c11.macro/ifdef (cat
                     (hs "#" "ifdef")
                     (nt :c11.macro/expr)
                     (nt :c11)
                     (cat? (hs "#" "else")
                           (nt :c11))
                     (hs "#" "endif"))
   :c11.macro/include
   (altnt :c11.macro.include/header :c11.macro.include/source)
   :c11.macro.include/header
   (cat (hs "#" "include" "<") (regexp "[a-z0-9/]+\\.h") (hs ">"))
   :c11.macro.include/source
   (cat (hs "#" "include" "\"") (regexp "[a-z0-9/]+\\.h") (hs "\""))
   })

(def pieces '[comment macro])

(def c11-grammar
  (let [keywords (map #(keyword (str "c11." (name %)))
                    pieces
                    )
        alts (apply altnt keywords)
        starter {:c11 (star alts)}]
    (apply merge (cons starter (eval pieces)))))

(def whitespace
  (insta/parser {:whitespace (regexp "[\\s]+") }
                :start :whitespace))

(def parse
  (insta/parser c11-grammar :start :c11 :auto-whitespace whitespace))

