(ns zones.core
  (:refer-clojure :exclude [binding bound-fn bound-fn* get set]))

; note: ES2015 only due to Object.create, can be ported to ES3

(defn make-prop [val]
  (list 'js-obj
        "value" val
        "writable" true
        "enumerable" true))

(defn munge-name [name]
  (str name))

(defn bindings-to-props [bindings]
  (let [names (take-nth 2 bindings)
        munged-names (map munge-name names)
        vals (take-nth 2 (next bindings))
        props (map make-prop vals)]
    (concat '(js-obj) (interleave munged-names props))))

(defmacro zone-binding [zone bindings & body]
  (let [props (bindings-to-props bindings)]
    `(let [prev-zone# ~zone
           new-zone# (.create js/Object ~zone ~props)]
       (set! ~zone new-zone#)
       (try
         ~@body
         (finally
           (set! ~zone prev-zone#))))))

(defmacro zone-get [zone name]
  `(goog.object/get ~zone ~(munge-name name)))

(defmacro zone-set! [zone name val]
  `(goog.object/set ~zone ~(munge-name name) ~val))

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

; ---------------------------------------------------------------------------------------------------------------------------

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
