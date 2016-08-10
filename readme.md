# cljs-zones 

[![GitHub license](https://img.shields.io/github/license/binaryage/cljs-zones.svg)](license.txt) 
[![Clojars Project](https://img.shields.io/clojars/v/binaryage/zones.svg)](https://clojars.org/binaryage/zones) 
[![Travis](https://img.shields.io/travis/binaryage/cljs-zones.svg)](https://travis-ci.org/binaryage/cljs-zones) 
[![Sample Project](https://img.shields.io/badge/project-example-ff69b4.svg)](https://github.com/binaryage/cljs-zones-sample)

Magical `binding` macro which survives async calls (with the help of `bound-fn`).

### Teaser

This example:

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

And generates code similar to this under `:advanced` optimizations:

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

You can play with the example in your browser [with klipse][1].

For more info see [full tests](test/src/tests/zones/tests/core.cljs) and [Travis output](https://travis-ci.org/binaryage/cljs-zones).

### FAQ

> What is a zone?

In general. A Zone is an execution context that persists across async tasks. You can think of it as thread-local storage for JavaScript VMs.

The name cljs-zones was inspired by [Angular's zone.js][3]. See [their presentations][4]. 

> Why do we need this in ClojureScript?
 
Clojure has [`binding`][5] and [`bound-fn`][6], but ClojureScript has just [`binding`][7]. 
Why? The simple reasoning is: Clojure has threads, but ClojureScript does not. 

But wait! Standard `binding` macro cannot be safely used across async call boundaries. 
ClojureScript is Javascript and it has asynchronous callbacks all over the place. 
Without something like `bound-fn` we are left in cold.
 
Well, that's a good point! This [issue](http://dev.clojure.org/jira/browse/CLJS-1634) was raised multiple times before. 
But we did not know how to implement it in a nice and performant way. Until now :-)

> Isn't this slow?

No, I believe (benchmarks needed).

> What is the trick?

First please think about `bound-fn`. It has to wrap given function so that:

  1. with each future (async) invocation, it
      1. stores currently active bindings
      2. sets currently active bindings to match bound-fn's call site
      3. executes wrapped function
      4. restores original bindings as currently active
 
A straight-forward attempt would be to go through all bindings and `set!` them one by one. But this could be costly when you
 imagine a lot of bindings and frequent calls to wrapped async function. Yeah, we could be smarter and [track only currently active
  bindings][2] to do as little work as possible but still this can lead to performance hits in specific scenarios.

The trick of this implementation is to (ab)use Javascript's prototypal inheritance. We group all dynamic "vars" under one
"bag" object (it is a plain javascript object). With each new `binding` macro we create a new bag which inherits 
parent bag via prototype. So we only define newly re-bound "vars" in our new bag, all previous "vars" will be visible 
via prototypal inheritance chain (except for vars shadowed by our new bag). 

We keep track of currently active bag and call it a zone. It means that at any execution point the zone holds a reference 
to currently active binding frame. Whenever code wants to read some dynamic "var" it needs to look for it in the zone 
(to effectively read it from current binding frame).
 
With this in place, we can now implement `bound-fn`.
  
  Given function `f`:
  
  1. store currently active zone as `call-site-zone`
  2. return a new function `g` wrapping `f` in the following way:
     1. store currently active zone as `last-active-zone`
     2. set currently active zone to be `call-site-zone`
     3. call `f` with applied arguments from `g`
     4. set currently active zone to be `last-active-zone`
     
As you can see, this implementation of wrapping is cheap. We are just juggling around pointers to bags which should be fast, 
because we are not creating new javascript objects on each invocation. Additionally during a new binding frame creation 
we pay only for newly re-bound "vars", not all existing dynamic "vars". Dynamics "var" lookup is cheap as well because 
it boils down to normal object property access and that's Javascript job. Javascript engines are good at walking protype chains.

> Nice, so we can track multiple zones if needed?

Good catch! Yes, cljs-zones provides a simplified API which implicitly works with `default-zone` for your convenience. 
But you can create your own zones and use them for different purposes. E.g. I could imagine you could gain some performance 
by splitting your `default-zone` if it got too big or deep.
  
> Is it compatible with ancient ECMAScript 3 Javascript engines?

Yes.

> Can this be ported to ClojureScript as part of standard binding macro in a backward compatible-way?

I believe, yes.

ClojureScript compiler could introduce a new meta to mark vars as being in the `:zone`. You could set it to `true` 
for internal default zone, or you could set it to some other :dynamic var acting as a custom zone.

Analyzer would be aware of `:zone` vars. It would mark zone var sites to:

  1. emit `zones/get` for each read requests. 
  2. emit `zones/set` for each write request.
  3. `binding` macro would merge functionality of regular `binding` and `zones/binding` (you could mix plain `:dynamic` and `:zone` vars there)

> What about code accessing :zone vars directly via js-interop?

Access via namespace would not be supported for `:zone` vars (they are not sitting there). 
People must be aware that they must go through zone for js-interop. 

For backward compatibility with legacy code we could implement a macro which would
generate ES2015 getters and setters to polyfil it. But I think it would be better not to encourage its usage.

> Does it work with core.async?

Yes and no. 

Please note that the code you wrap in `go` macro gets chopped into smaller chunks cut on async-call boundaries. 
Core.async then runs a small state machine executing those chunks in right order and storing/restoring machine state between async calls.

Ideally we would like to wrap those code chunks in our `bound-fn` but that is not conveniently possible AFAIK (help needed!).
What you can do today is to capture the "call-site-zone" immediately before entering go block. And then extract your 
go-block code into functions which receive call-site-zone as a parameter. Inside you can store/restore call-site-zone similar
to our bound-fn implementation. Please note that you cannot do this inside `go` block body itself - your code there will be
reordered and rewritten. And naturally you can extract only linear parts of the code without async calls in them.
  
This is an area of my future research. Ideas welcome!

[1]: http://app.klipse.tech/?cljs_in.gist=darwin/1e31b0c33f1ca0e6e0e475b51f95b424&external-libs=%5Bhttps://raw.githubusercontent.com/binaryage/cljs-zones/v0.1.0/src/lib%5D
[2]: https://gist.github.com/whilo/a8ef2cd3f0e033d3973880a2001be32a
[3]: https://github.com/angular/zone.js
[4]: https://www.youtube.com/watch?v=3IqtmUscE_U
[5]: https://clojuredocs.org/clojure.core/binding
[6]: https://clojuredocs.org/clojure.core/bound-fn
[7]: http://cljs.github.io/api/cljs.core/#binding
