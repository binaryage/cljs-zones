(ns zones.core
  (:require [goog.object]))

(defn make-zone
  ([] (js-obj))
  ([init] init))

(def ^:dynamic default-zone (make-zone))
