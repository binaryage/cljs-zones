(ns zones.tests.helpers
  (:require [zones.core :as zones]
            [cljs.pprint :refer [cl-format]]))

; -- helpers ----------------------------------------------------------------------------------------------------------------

(defn leftpad [s len]
  (cl-format nil (str "~" len ",'" " " "d") s))

(defn print-default-zone [prefix]
  (print (leftpad (str prefix ": ") 60) zones/default-zone))
