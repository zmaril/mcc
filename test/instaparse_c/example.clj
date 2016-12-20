(def test
  "#ifdef A
				if (a) {
  #else
				if (b) {
  #endif
				  a = a;
				}")



[:c11/start
 [:c11/statements
  [:c11/statement
   [:c11.statement/if
    [:c11/expression [:c11/symbol "a"]]
    [:c11.if/body]]]]]

["#ifdef" "a"
 "if" "(" "a" ")" "{"
 "#else"
 "if" "(" "b" ")" "{"
 "#endif"

 "a" "=" "a"

 "}"]


[:c11/start
 [:c11/statements
  [:c11/statement
   [:c11.statement/if
    [:c11/expression [:c11/symbol "a"]]
    [:c11.if/body
     [:c11/statement
      [:c11.statement/expression
       [:c11/expression
        [:c11.expression/assignment
         [:c11/symbol "a"]
         "="
         [:c11/symbol "a"]]]
       ";"]]]]]]]


[:c11/start
 [:c11/statements
  [:c11/statement
   [:c11.statement/if
    [:c11/expression [:c11/symbol "b"]]
    [:c11.if/body
     [:c11/statement
      [:c11.statement/expression
       [:c11/expression
        [:c11.expression/assignment
         [:c11/symbol "a"]
         "="
         [:c11/symbol "a"]]]
       ";"]]]]]]]
