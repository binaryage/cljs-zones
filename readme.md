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
  
(.log js/console "before:" (zones/get v))
(zones/binding [v "I'm a dynamically bound value in the default zone"]
  (.log js/console "inside:" (zones/get v))
  (js/setTimeout (zones/bound-fn [] (.log js/console "in async call:" (zones/get v))) 500))
(.log js/console "after:" (zones/get v))

```

Prints:

```
before: nil
inside: I'm a dynamically bound value in the default zone
after: nil
in async call: I'm a dynamically bound value in the default zone
```

And generated code similar to this under `:advanced` optimizations:

```javascript
  console.log("before:", $goog$object$get$$($zones$core$default_zone$$, "v"));
  var $outer_zone_17341_17351$$ = $zones$core$default_zone$$
    , $newborn_zone_17342$$inline_1313$$ = {
    v: "I'm a dynamically bound value in the default zone"
  };
  $newborn_zone_17342$$inline_1313$$.__proto__ = $zones$core$default_zone$$;
  $zones$core$default_zone$$ = $newborn_zone_17342$$inline_1313$$;
  try {
    console.log("inside:", $goog$object$get$$($zones$core$default_zone$$, "v")),
    setTimeout(function() {
      return function($call_site_zone_17349$$1$$, $outer_zone_17341_17351$$1$$) {
        return function() {
          var $active_zone_17350$$ = $zones$core$default_zone$$;
          $zones$core$default_zone$$ = $call_site_zone_17349$$1$$;
          try {
            return function() {
              return function() {
                return console.log("in async call:", $goog$object$get$$($zones$core$default_zone$$, "v"))
              }
            }($active_zone_17350$$, $call_site_zone_17349$$1$$, $outer_zone_17341_17351$$1$$).apply(null , arguments)
          } finally {
            $zones$core$default_zone$$ = $active_zone_17350$$
          }
        }
      }($zones$core$default_zone$$, $outer_zone_17341_17351$$)
    }(), 500)
  } finally {
    $zones$core$default_zone$$ = $outer_zone_17341_17351$$
  }
  console.log("after:", $goog$object$get$$($zones$core$default_zone$$, "v"));
```



You can play with an example in your browser [with klipse][1].

For more info see [full tests](test/src/tests/zones/tests/core.cljs) and [Travis output](https://travis-ci.org/binaryage/cljs-zones).

[1]: http://app.klipse.tech/?cljs_in.gist=darwin/1e31b0c33f1ca0e6e0e475b51f95b424&external-libs=%5Bhttps://raw.githubusercontent.com/binaryage/cljs-zones/v0.1.0/src/lib%5D
