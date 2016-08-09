(ns zones.core
  (:require-macros [zones.core :refer [get-prototype]])
  (:require [goog.object]))

(defn make-zone
  ([] (js-obj))
  ([init] init))

(defn prototype-with-own-property [o name]
  (if (some? o)
    (if (.hasOwnProperty o name)
      o
      (recur (get-prototype o) name))))

(defn prototype-aware-set! [o name val]
  (let [selected-prototype (prototype-with-own-property o name)
        effective-target (or selected-prototype o)]
    (goog.object/set effective-target name val)))

(def ^:dynamic default-zone (make-zone))
