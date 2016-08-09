# cljs-zones 

[![GitHub license](https://img.shields.io/github/license/binaryage/cljs-zones.svg)](license.txt) 
[![Clojars Project](https://img.shields.io/clojars/v/binaryage/zones.svg)](https://clojars.org/binaryage/zones) 
[![Travis](https://img.shields.io/travis/binaryage/cljs-zones.svg)](https://travis-ci.org/binaryage/cljs-zones) 
[![Sample Project](https://img.shields.io/badge/project-example-ff69b4.svg)](https://github.com/binaryage/cljs-zones-sample)

Magical `binding` which survives async calls (with the help of `bound-fn`). 

### Documentation


You can test it in your browser [with klipse](http://app.klipse.tech/?cljs_in.gist=viebel/ee6f2e662c82edb7070068f1186c9e4d&external-libs=[https://raw.githubusercontent.com/viebel/cljs-zones/master/src/lib]).

If you want to test it locally, just do the following:

```clojure
(ns zones.tests.core
  (:refer-clojure :exclude [binding get set])
  (:require [cljs.test :refer-macros [async deftest testing is use-fixtures]]
            [zones.core :refer-macros [binding get set bound-fn* bound-fn] :refer [make-zone default-zone]]))

; -- helpers ----------------------------------------------------------------------------------------------------------------

(defn default-zone-str []
  (pr-str default-zone))

(defn print-default-zone [& args]
  (apply print (concat args [": " (default-zone-str)])))

; -- T0 ---------------------------------------------------------------------------------------------------------------------

(defn test0 []
  (print-default-zone "before")
  (binding [v "I'm a dynamically bound value in default zone"]
    (print-default-zone "inside")
    (js/setTimeout (bound-fn* #(print-default-zone "in async call")) 500))
  (print-default-zone "after"))

(deftest T0
  (async done
    (test0)
    (js/setTimeout done 1000)))
```

Prints:

```
Testing zones.tests.core
before :  #js {}
inside :  #js {:v "I'm a dynamically bound value in default zone"}
after :  #js {}
in async call :  #js {:v "I'm a dynamically bound value in default zone"}
```

For more info see [full tests](test/src/tests/zones/tests/core.cljs) and [Travis output](https://travis-ci.org/binaryage/cljs-zones).
