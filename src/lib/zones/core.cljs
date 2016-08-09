(ns zones.core
  (:require-macros [zones.core :refer [get-prototype make-zone get-current-version]])
  (:require [goog.object]))

(def ^:dynamic default-zone (make-zone))

(def version (get-current-version))

; -- macro apis -------------------------------------------------------------------------------------------------------------

(defn prototype-with-own-property [o name]
  (if (some? o)
    (if (.hasOwnProperty o name)
      o
      (recur (get-prototype o) name))))

(defn prototype-aware-set! [o name val]
  (let [selected-prototype (prototype-with-own-property o name)
        effective-target (or selected-prototype o)]
    (goog.object/set effective-target name val)))
