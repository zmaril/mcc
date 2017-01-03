# mcc

mcc is a C parser (and macro preprocessor) written in Clojure and Instaparse. It's meant to allow different types of analysis than is typically possible with the parsers built into compilers. mcc delays the addition of semantic information to the AST's because that makes it easier to analyze both the code in the c language proper as well as the macros involved. mcc can do interesting things but it is still very raw and in progress.

## Usage

``` c
/* Hello World program */

/*#include<stdio.h>*/

int main()
{

#if defined \
  (HAVE_STRNVIS)
  printf("Hello World");
#endif
  printf("Hello World");
  printf("HELLO PAUL");

}
```

```clojure
(def sample (slurp  "dev-resources/sample.c"))
(def db (mcc.core/string->db sample))

(d/q '[:find  ?prize
       :where [?value :value "printf"]
              [?symbol :content ?value]
              [?fun-call :content ?symbol]
              [?fun-call :content ?args]
              [?args :order 1]
              [?args :content ?temp1]
              [?temp1 :content ?temp2]
              [?temp2 :value ?prize]]
     @db)
;;#{["\"HELLO PAUL\""] ["\"Hello World\""]} 
```
## Development 

`lein test` runs the tests. It will download `openssh-portable` to the
`dev-resources/corpus` directory automatically.

Recommend that you run `lein checkout instaparse` and change the following line. 

https://github.com/timvisher/lein-checkout for installation instructions


## License

Copyright © 2016 Zack Maril

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
