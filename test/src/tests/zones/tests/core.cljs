(ns zones.tests.core
  (:refer-clojure :exclude [binding get set])
  (:require [cljs.test :refer-macros [async deftest testing is use-fixtures]]
            [zones.core :refer-macros [binding get set bound-fn* bound-fn] :refer [make-zone default-zone]]))

; -- helpers ----------------------------------------------------------------------------------------------------------------

(defn default-zone-str []
  (pr-str default-zone))

(defn default-zone-print [& args]
  (apply print (concat args [": " (default-zone-str)])))

; -- T1 ---------------------------------------------------------------------------------------------------------------------

(defn async-t1-fn2 []
  (default-zone-print "T1 timeout in async-t1-fn2"))

(defn async-t1-fn1 []
  (default-zone-print "T1 async-t1-fn1 before")
  (binding [v3 3
            v2 42]
    (default-zone-print "T1 async-fn1 second-level-binding")
    (js/setTimeout (bound-fn* async-t1-fn2) 500))
  (default-zone-print "T1 async-fn1 after"))

(defn test1 []
  (default-zone-print "T1 before")
  (binding [v1 1
            v2 2]
    (default-zone-print "T1 inside")
    (js/setTimeout (bound-fn* async-t1-fn1) 1000))
  (default-zone-print "T1 after"))

(deftest T1
  (async done
    (test1)
    (js/setTimeout done 2000)))

; -- T2 ---------------------------------------------------------------------------------------------------------------------

(defn async-t2-fn1 []
  (default-zone-print "T2 async-t2-fn1 before set")
  (set x 100)
  (default-zone-print "T2 async-t2-fn1 after set"))

(defn async-t2-fn2 []
  (default-zone-print "T2 async-t2-fn2"))

(defn async-t2-fn3 []
  (default-zone-print "T2 async-t2-fn3"))

(defn async-t2-fn4 []
  (default-zone-print "T2 async-t2-fn4"))

(defn test2 []
  (binding [x 1]
    (default-zone-print "T2 inside")
    (js/setTimeout (bound-fn* async-t2-fn1) 200)
    (js/setTimeout (bound-fn* async-t2-fn2) 400)
    (binding [x 1000]
      (js/setTimeout (bound-fn* async-t2-fn3) 300)
      (js/setTimeout (bound-fn* async-t2-fn4) 600))))

(deftest T2
  (async done
    (test2)
    (js/setTimeout done 1000)))
