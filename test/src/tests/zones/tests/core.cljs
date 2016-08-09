(ns zones.tests.core
  (:refer-clojure :exclude [binding get])
  (:require [cljs.test :refer-macros [async deftest testing is use-fixtures]]
            [zones.core :as zones :refer-macros [binding get set! bound-fn* bound-fn]]
            [zones.tests.helpers :refer [print-default-zone]]))

; -- T0 ---------------------------------------------------------------------------------------------------------------------

(defn test0 []
  (let [v-val "I'm a dynamically bound value in the default zone"]
    (print-default-zone "test0: before binding")
    (is (= (get v) nil))
    (binding [v v-val]
      (print-default-zone "test0: inside binding")
      (is (= (get v) v-val))
      (js/setTimeout (bound-fn []
                       (print-default-zone "test0: in async call")
                       (is (= (get v) v-val))) 500))
    (print-default-zone "test0: after binding")
    (is (= (get v) nil))))

(deftest T0
  (testing "simple async call inside zone binding"
    (async done
      (test0)
      (js/setTimeout done 1000))))

; -- T1 ---------------------------------------------------------------------------------------------------------------------

(defn test1-async-fn2 []
  (is (= (get v1) 1))
  (is (= (get v2) 42))
  (is (= (get v3) 3))
  (print-default-zone "test1-async-fn2"))

(defn test1-async-fn1 []
  (print-default-zone "test1-async-fn1: before binding")
  (is (= (get v1) 1))
  (is (= (get v2) 2))
  (is (= (get v3) nil))
  (binding [v3 3
            v2 42]
    (print-default-zone "test1-async-fn1: second-level-binding")
    (is (= (get v1) 1))
    (is (= (get v2) 42))
    (is (= (get v3) 3))
    (js/setTimeout (bound-fn* test1-async-fn2) 500))
  (print-default-zone "test1-async-fn1: after binding"))

(defn test1 []
  (print-default-zone "test1: before binding")
  (is (= (get v1) nil))
  (is (= (get v2) nil))
  (is (= (get v3) nil))
  (binding [v1 1
            v2 2]
    (print-default-zone "test1: inside binding")
    (is (= (get v1) 1))
    (is (= (get v2) 2))
    (is (= (get v3) nil))
    (js/setTimeout (bound-fn* test1-async-fn1) 1000))
  (print-default-zone "test1: after binding")
  (is (= (get v1) nil))
  (is (= (get v2) nil))
  (is (= (get v3) nil)))

(deftest T1
  (testing "two nested async calls each with own zone bindings"
    (async done
      (test1)
      (js/setTimeout done 2000))))

; -- T2 ---------------------------------------------------------------------------------------------------------------------

(defn test2-async-fn1 []
  (print-default-zone "test2-async-fn1: before set!")
  (is (= (get x) 1))
  (zones/set! x 100)
  (print-default-zone "test2-async-fn1: after set!")
  (is (= (get x) 100)))

(defn test2-async-fn2 []
  (print-default-zone "test2-async-fn2")
  (is (= (get x) 100)))

(defn test2-async-fn3 []
  (print-default-zone "test2-async-fn3: before set!")
  (is (= (get x) 1000))
  (zones/set! x 1001)
  (print-default-zone "test2-async-fn3: after set!")
  (is (= (get x) 1001)))

(defn test2-async-fn4 []
  (print-default-zone "test2-async-fn4")
  (is (= (get x) 1001)))

(defn test2 []
  (is (= (get x) nil))
  (binding [x 1]
    (print-default-zone "test2: inside binding-1")
    (is (= (get x) 1))
    (js/setTimeout (bound-fn* test2-async-fn1) 200)
    (js/setTimeout (bound-fn* test2-async-fn2) 400)
    (binding [x 1000]
      (print-default-zone "test2: inside binding-1-2")
      (js/setTimeout (bound-fn* test2-async-fn3) 300)
      (js/setTimeout (bound-fn* test2-async-fn4) 600))
    (is (= (get x) 1)))
  (is (= (get x) nil)))

(deftest T2
  (testing "two nested binding frames with mixed async calls and set!"
    (async done
      (test2)
      (js/setTimeout done 1000))))

; -- T3 ---------------------------------------------------------------------------------------------------------------------

(defn test3-async-fn1 []
  (print-default-zone "test3-async-fn1: before set!")
  (is (= (get x) 1))
  (zones/set! x 100)
  (print-default-zone "test3-async-fn1: after set!")
  (is (= (get x) 100)))

(defn test3-async-fn2 []
  (print-default-zone "test3-async-fn2")
  (is (= (get x) 100)))

(defn test3-async-fn3 []
  (print-default-zone "test3-async-fn3: before set!")
  (is (= (get x) 1000))
  (zones/set! x 1001)
  (print-default-zone "test3-async-fn3: after set!")
  (is (= (get x) 1001)))

(defn test3-async-fn4 []
  (print-default-zone "test3-async-fn4")
  (is (= (get x) 1001)))

(defn test3 []
  (is (= (get x) nil))
  (binding [x 1]
    (print-default-zone "test3: inside binding-1")
    (is (= (get x) 1))
    (js/setTimeout (bound-fn* test3-async-fn1) 200)
    (js/setTimeout (bound-fn* test3-async-fn2) 400))
  (binding [x 1000]
    (print-default-zone "test3: inside binding-1-2")
    (js/setTimeout (bound-fn* test3-async-fn3) 300)
    (js/setTimeout (bound-fn* test3-async-fn4) 600))
  (is (= (get x) nil)))

(deftest T3
  (testing "two parallel binding frames with mixed async calls and set!"
    (async done
      (test3)
      (js/setTimeout done 1000))))

; -- T4 ---------------------------------------------------------------------------------------------------------------------

(defn test4-async-fn1 []
  (print-default-zone "test4-async-fn1: before set!")
  (is (= (get s) 1))
  (zones/set! s 100)
  (print-default-zone "test4-async-fn1: after set!")
  (is (= (get s) 100)))

(defn test4-async-fn2 []
  (print-default-zone "test4-async-fn2")
  (is (= (get s) 100)))

(defn test4-async-fn3 []
  (print-default-zone "test4-async-fn3: before set!")
  (is (= (get s) 100))
  (zones/set! s 1001)
  (print-default-zone "test4-async-fn3: after set!")
  (is (= (get s) 1001)))

(defn test4-async-fn4 []
  (print-default-zone "test4-async-fn4")
  (is (= (get s) 1001)))

(defn test4-async-fn5 []
  (print-default-zone "test4-async-fn5")
  (is (= (get s) 1001)))

(defn test4 []
  (is (= (get s) nil))
  (binding [s 1]
    (print-default-zone "test4: inside binding-1")
    (is (= (get s) 1))
    (js/setTimeout (bound-fn* test4-async-fn1) 200)
    (js/setTimeout (bound-fn* test4-async-fn2) 250)
    (js/setTimeout (bound-fn* test4-async-fn4) 400)
    (binding [x 1000]
      (print-default-zone "test4: inside binding-1-2")
      (js/setTimeout (bound-fn* test4-async-fn3) 300)
      (js/setTimeout (bound-fn* test4-async-fn5) 600))
    (is (= (get s) 1)))
  (is (= (get s) nil)))

(deftest T4
  (testing "two nested binding frames with mixed async calls and set! on shared var from binding-1"
    (async done
      (test4)
      (js/setTimeout done 1000))))
