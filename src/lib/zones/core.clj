(ns zones.core
  (:refer-clojure :exclude [binding bound-fn bound-fn* get])
  (:require [environ.core :refer [env]]))

(def ES2015? (boolean (env :cljs-zones-es2015)))

(defmacro get-compilation-mode []
  (if ES2015? "ES2015" "ES3"))

; -- helpers ----------------------------------------------------------------------------------------------------------------

(defn munge-name [name]
  (str name))

(defn gen-make-prop [val]
  (list 'js-obj
        "value" val
        "writable" true
        "enumerable" true))

(defn gen-bindings-props [bindings]
  (let [names (take-nth 2 bindings)
        munged-names (map munge-name names)
        vals (take-nth 2 (next bindings))
        props (map gen-make-prop vals)]
    (concat '(js-obj) (interleave munged-names props))))

(defn gen-bindings-obj [bindings]
  (let [names (take-nth 2 bindings)
        munged-names (map munge-name names)
        vals (take-nth 2 (next bindings))]
    (concat '(js-obj) (interleave munged-names vals))))

(defn gen-get-prototype-ES2015 [o]
  `(.getPrototypeOf js/Object ~o))

(defn gen-get-prototype-ES3 [o]
  `(aget ~o "__proto__"))

(defn gen-get-prototype [o]
  (if ES2015?
    (gen-get-prototype-ES2015 o)
    (gen-get-prototype-ES3 o)))

(defn gen-create-object-ES2015 [proto bindings]
  `(.create js/Object ~proto ~(gen-bindings-props bindings)))

(defn gen-create-object-ES3 [proto bindings]
  `(let [obj# ~(gen-bindings-obj bindings)]
     (set! (.. obj# -__proto__) ~proto)
     obj#))

(defn gen-create-object [proto bindings]
  (if ES2015?
    (gen-create-object-ES2015 proto bindings)
    (gen-create-object-ES3 proto bindings)))

(defmacro get-prototype [o]
  (gen-get-prototype o))

; -- general zone operations ------------------------------------------------------------------------------------------------

(defmacro make-zone
  ([] (gen-bindings-obj []))
  ([bindings] (gen-bindings-obj bindings)))

(defmacro zone-binding [zone bindings & body]
  `(let [prev-zone# ~zone
         new-zone# ~(gen-create-object zone bindings)]
     (set! ~zone new-zone#)
     (try
       ~@body
       (finally
         (set! ~zone prev-zone#)))))

(defmacro zone-get [zone name]
  `(goog.object/get ~zone ~(munge-name name)))

(defmacro zone-set! [zone name val]
  `(zones.core/prototype-aware-set! ~zone ~(munge-name name) ~val))

(defmacro zone-bound-fn* [zone f]
  `(let [lexical-zone# ~zone]
     (fn [& args#]
       (let [active-zone# ~zone]
         (set! ~zone lexical-zone#)
         (try
           (apply ~f args#)
           (finally
             (set! ~zone active-zone#)))))))

(defmacro zone-bound-fn [zone & fntail]
  `(zone-bound-fn* ~zone (fn ~@fntail)))

; -- specific default-zone operations ---------------------------------------------------------------------------------------

(defmacro binding [bindings & body]
  `(zone-binding ~'zones.core/default-zone ~bindings ~@body))

(defmacro get [name]
  `(zone-get ~'zones.core/default-zone ~name))

(defmacro set! [name val]
  `(zone-set! ~'zones.core/default-zone ~name ~val))

(defmacro bound-fn* [f]
  `(zone-bound-fn* ~'zones.core/default-zone ~f))

(defmacro bound-fn [& fntail]
  `(zone-bound-fn ~'zones.core/default-zone ~@fntail))
