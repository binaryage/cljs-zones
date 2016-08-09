# cljs-zones 

[![GitHub license](https://img.shields.io/github/license/binaryage/cljs-zones.svg)](license.txt) 
[![Clojars Project](https://img.shields.io/clojars/v/binaryage/zones.svg)](https://clojars.org/binaryage/zones) 
[![Travis](https://img.shields.io/travis/binaryage/cljs-zones.svg)](https://travis-ci.org/binaryage/cljs-zones) 

Magical `binding` macro which survives async calls (with the help of `bound-fn`). 

### Documentation

Example

```clojure
(ns zones.test
  (:require [zones.core :as zones :include-macros true]))
  
(println "before:" (zones/get v))
(zones/binding [v "I'm a dynamically bound value in the default zone"]
  (println "inside:" (zones/get v))
  (js/setTimeout (zones/bound-fn [] (println "in async call:" (zones/get v))) 500))
(println "after:" (zones/get v))

```

Prints:

```
before: nil
inside: I'm a dynamically bound value in the default zone
after: nil
in async call: I'm a dynamically bound value in the default zone
```

You can play with an example in your browser [with klipse][1].

For more info see [full tests](test/src/tests/zones/tests/core.cljs) and [Travis output](https://travis-ci.org/binaryage/cljs-zones).

[1]: http://app.klipse.tech/?cljs_in.gist=darwin/1e31b0c33f1ca0e6e0e475b51f95b424&external-libs=%5Bhttps://raw.githubusercontent.com/binaryage/cljs-zones/v0.1.0/src/lib%5D
