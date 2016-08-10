(ns zones.core
  (:refer-clojure :exclude [binding bound-fn bound-fn* get]))

(def current-version "0.1.1-SNAPSHOT")                                                                                        ; this should match our project.clj

(defn read-config []
  (if cljs.env/*compiler*
    (get-in @cljs.env/*compiler* [:options :external-config :zones/config])))                                                 ; https://github.com/bhauman/lein-figwheel/commit/80f7306bf5e6bd1330287a6f3cc259ff645d899b

(def config (read-config))

(defn config-compilation-mode []
  (keyword (or (:compilation-mode config) :ES3)))

(defn compile-as-ES2015? []
  (= (config-compilation-mode) :ES2015))

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
  (if (compile-as-ES2015?)
    (gen-get-prototype-ES2015 o)
    (gen-get-prototype-ES3 o)))

(defn gen-new-zone-ES2015 [proto bindings]
  `(.create js/Object ~proto ~(gen-bindings-props bindings)))

(defn gen-new-zone-ES3 [proto bindings]
  `(let [newborn-zone# ~(gen-bindings-obj bindings)]
     (set! (.. newborn-zone# -__proto__) ~proto)
     newborn-zone#))

(defn gen-new-zone [proto bindings]
  (if (compile-as-ES2015?)
    (gen-new-zone-ES2015 proto bindings)
    (gen-new-zone-ES3 proto bindings)))

; -- aux macros -------------------------------------------------------------------------------------------------------------

(defmacro get-current-version []
  current-version)

(defmacro get-compilation-mode []
  (config-compilation-mode))

(defmacro get-prototype [o]
  (gen-get-prototype o))

(defmacro make-zone
  ([] (gen-bindings-obj []))
  ([bindings] (gen-bindings-obj bindings)))

; -- general zone operations ------------------------------------------------------------------------------------------------

(defmacro zone-binding [zone bindings & body]
  `(let [prev-zone# ~zone]
     (set! ~zone ~(gen-new-zone zone bindings))
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
     (fn []
       (let [active-zone# ~zone]
         (set! ~zone lexical-zone#)
         (try
           ; note we use js-interop here because it leads to simpler generated code
           ; using (fn [& args#]... (apply ~f args#) ...) would be more idiomatic version
           ; but it would generate some busy-work code which would not go away even under :advanced optimizations
           ; (as of clojurescript 1.9.89)
           (.apply ~f nil (cljs.core/js-arguments))
           (finally
             (set! ~zone active-zone#)))))))

(defmacro zone-bound-fn [zone & fntail]
  `(zone-bound-fn* ~zone (fn ~@fntail)))

; -- specialized default-zone operations ------------------------------------------------------------------------------------

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
