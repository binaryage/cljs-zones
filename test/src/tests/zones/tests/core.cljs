(ns zones.tests.core
  (:refer-clojure :exclude [binding get set])
  (:require [cljs.test :refer-macros [async deftest testing is use-fixtures]]
            [zones.core :as zones :refer-macros [binding get set bound-fn* bound-fn]]))

; -- helpers ----------------------------------------------------------------------------------------------------------------

(defn print-default-zone [location]
  (print (str location ": ") zones/default-zone))

; -- T0 ---------------------------------------------------------------------------------------------------------------------

(defn test0 []
  (print-default-zone "test0: before binding")
  (binding [v "I'm a dynamically bound value in the default zone"]
    (print-default-zone "test0: inside binding")
    (js/setTimeout (bound-fn* #(print-default-zone "test0: in async call")) 500))
  (print-default-zone "test0: after binding"))

(deftest T0
  (async done
    (test0)
    (js/setTimeout done 1000)))

; -- T1 ---------------------------------------------------------------------------------------------------------------------

(defn test1-async-fn2 []
  (print-default-zone "test1-async-fn2"))

(defn test1-async-fn1 []
  (print-default-zone "test1-async-fn1: before binding")
  (binding [v3 3
            v2 42]
    (print-default-zone "test1-async-fn1: second-level-binding")
    (js/setTimeout (bound-fn* test1-async-fn2) 500))
  (print-default-zone "test1-async-fn1: after binding"))

(defn test1 []
  (print-default-zone "test1: before binding")
  (binding [v1 1
            v2 2]
    (print-default-zone "test1: inside binding")
    (js/setTimeout (bound-fn* test1-async-fn1) 1000))
  (print-default-zone "test1: after binding"))

(deftest T1
  (async done
    (test1)
    (js/setTimeout done 2000)))

; -- T2 ---------------------------------------------------------------------------------------------------------------------

(defn test2-async-fn1 []
  (print-default-zone "test2-async-fn1: before set")
  (set x 100)
  (print-default-zone "test2-async-fn1: after set"))

(defn test2-async-fn2 []
  (print-default-zone "test2-async-fn2"))

(defn test2-async-fn3 []
  (print-default-zone "test2-async-fn3"))

(defn test2-async-fn4 []
  (print-default-zone "test2-async-fn4"))

(defn test2 []
  (binding [x 1]
    (print-default-zone "test2: inside binding1")
    (js/setTimeout (bound-fn* test2-async-fn1) 200)
    (js/setTimeout (bound-fn* test2-async-fn2) 400)
    (binding [x 1000]
      (print-default-zone "test2: inside binding2")
      (js/setTimeout (bound-fn* test2-async-fn3) 300)
      (js/setTimeout (bound-fn* test2-async-fn4) 600))))

(deftest T2
  (async done
    (test2)
    (js/setTimeout done 1000)))
