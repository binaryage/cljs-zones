# cljs-zones 

[![GitHub license](https://img.shields.io/github/license/binaryage/cljs-zones.svg)](license.txt) 
[![Clojars Project](https://img.shields.io/clojars/v/binaryage/zones.svg)](https://clojars.org/binaryage/zones) 
[![Travis](https://img.shields.io/travis/binaryage/cljs-zones.svg)](https://travis-ci.org/binaryage/cljs-zones) 
[![Sample Project](https://img.shields.io/badge/project-example-ff69b4.svg)](https://github.com/binaryage/cljs-zones-sample)

Magical `binding` macro which survives async calls (with the help of `bound-fn`). 

### Documentation

Example

```clojure
(ns zones.test
  (:refer-clojure :exclude [binding])
  (:require [zones.core :refer-macros [binding bound-fn] :refer [default-zone]]))
  
(defn print-default-zone [prefix]
  (println (str prefix ": " (pr-str default-zone))))
            
(print-default-zone "before")
(binding [v "I'm a dynamically bound value in default zone"]
  (print-default-zone "inside")
  (js/setTimeout (bound-fn [] (print-default-zone "in async call")) 500))
(print-default-zone "after")
(println "main done")
```

Prints:

```
before:  #js {}
inside:  #js {:v "I'm a dynamically bound value in default zone"}
after:  #js {}
in async call:  #js {:v "I'm a dynamically bound value in default zone"}
```

You can play with an example in your browser [with klipse][1].

For more info see [full tests](test/src/tests/zones/tests/core.cljs) and [Travis output](https://travis-ci.org/binaryage/cljs-zones).

[1]: http://app.klipse.tech/?cljs_in.gist=darwin/1e31b0c33f1ca0e6e0e475b51f95b424&external-libs=%5Bhttps://raw.githubusercontent.com/binaryage/cljs-zones/v0.1.0/src/lib%5D
