(ns zones.runner
  (:require [cljs.test :as test :refer-macros [run-tests] :refer [report]]
            [zones.core :refer-macros [get-compilation-mode]]
            [zones.tests.core]))

(enable-console-print!)

; -- entry point ------------------------------------------------------------------------------------------------------------

(defmethod report [:cljs.test/default :end-run-tests] [m]
  (aset js/window "tests-done" true))

(defmethod report [::test/default :summary] [m]
  (println "\nRan" (:test m) "tests containing"
           (+ (:pass m) (:fail m) (:error m)) "assertions.")
  (println (:fail m) "failures," (:error m) "errors.")
  (aset js/window "test-failures" (+ (:fail m) (:error m))))

(defn run-normal-tests []
  (test/run-tests
    (cljs.test/empty-env ::test/default)
    'zones.tests.core))

(println (str "Code compiled in " (get-compilation-mode) " mode"))
(case (.-selectedTestSuite js/window)
  (run-normal-tests))
