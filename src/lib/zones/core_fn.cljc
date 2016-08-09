(ns zones.core-fn
  #?(:cljs (:require [goog.object])))

#?(:cljs
(defn make-zone
  ([] (js-obj))
  ([init] init)))

#?(:cljs
  (def ^:dynamic default-zone (make-zone)))

#?(:clj
    (def ^:dynamic default-zone nil))
