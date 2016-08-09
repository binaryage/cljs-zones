(ns zones.tests.core
  (:refer-clojure :exclude [binding get set])
  (:require [cljs.test :refer-macros [async deftest testing is use-fixtures]]
            [zones.core-fn :refer [make-zone default-zone]])
  (:require-macros
            [zones.core :refer [binding get set bound-fn* bound-fn]]))

; -- helpers ----------------------------------------------------------------------------------------------------------------

(defn default-zone-str []
  (pr-str default-zone))

(defn print-default-zone [& args]
  (apply print (concat args [": " (default-zone-str)])))

; -- T0 ---------------------------------------------------------------------------------------------------------------------

(defn test0 []
  (print-default-zone "before")
  (binding [v "I'm a dynamically bound value in default zone"]
    (print-default-zone "inside")
    (js/setTimeout (bound-fn* #(print-default-zone "in async call")) 500))
  (print-default-zone "after"))

(deftest T0
  (async done
    (test0)
    (js/setTimeout done 1000)))

; -- T1 ---------------------------------------------------------------------------------------------------------------------

(defn async-t1-fn2 []
  (print-default-zone "T1 timeout in async-t1-fn2"))

(defn async-t1-fn1 []
  (print-default-zone "T1 async-t1-fn1 before")
  (binding [v3 3
            v2 42]
    (print-default-zone "T1 async-fn1 second-level-binding")
    (js/setTimeout (bound-fn* async-t1-fn2) 500))
  (print-default-zone "T1 async-fn1 after"))

(defn test1 []
  (print-default-zone "T1 before")
  (binding [v1 1
            v2 2]
    (print-default-zone "T1 inside")
    (js/setTimeout (bound-fn* async-t1-fn1) 1000))
  (print-default-zone "T1 after"))

(deftest T1
  (async done
    (test1)
    (js/setTimeout done 2000)))

; -- T2 ---------------------------------------------------------------------------------------------------------------------

(defn async-t2-fn1 []
  (print-default-zone "T2 async-t2-fn1 before set")
  (set x 100)
  (print-default-zone "T2 async-t2-fn1 after set"))

(defn async-t2-fn2 []
  (print-default-zone "T2 async-t2-fn2"))

(defn async-t2-fn3 []
  (print-default-zone "T2 async-t2-fn3"))

(defn async-t2-fn4 []
  (print-default-zone "T2 async-t2-fn4"))

(defn test2 []
  (binding [x 1]
    (print-default-zone "T2 inside")
    (js/setTimeout (bound-fn* async-t2-fn1) 200)
    (js/setTimeout (bound-fn* async-t2-fn2) 400)
    (binding [x 1000]
      (js/setTimeout (bound-fn* async-t2-fn3) 300)
      (js/setTimeout (bound-fn* async-t2-fn4) 600))))

(deftest T2
  (async done
    (test2)
    (js/setTimeout done 1000)))
