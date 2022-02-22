(ns dialog.format.json
  "Log format which presents events as JSON objects for structured logging."
  (:require
    [clojure.data.json :as json]
    [io.aviso.exception :as ex]))


(defn- key-fn
  "Coerce a map key into a string for use as a JSON object property."
  [k]
  (cond
    (nil? k)
    "nil"

    (keyword? k)
    (subs (str k) 1)

    :else
    (str k)))


(defn- sanitize-stack-frame
  "Sanitize a map of stack frame data for JSON serialization."
  [frame]
  (into {}
        (comp
          (map (juxt identity frame))
          (filter second))
        [:class :file :line :method :name]))


(defn- sanitize-exception
  "Sanitize a map of exception data for JSON serialization."
  [data]
  (assoc data
         :stack-trace
         (mapv sanitize-stack-frame (:stack-trace data))))


(defn- render-exception
  "Convert an exception into a data structure suitable for serializing to JSON."
  [ex]
  (into []
        (map sanitize-exception)
        (ex/analyze-exception ex {})))


(defn- value-fn
  "Coerce a value into a valid JSON type."
  ([_k v]
   (value-fn v))
  ([v]
   (cond
     (or (nil? v)
         (boolean? v)
         (number? v)
         (string? v)
         (inst? v)
         (uuid? v))
     v

     (keyword? v)
     (key-fn v)

     (instance? java.util.Map v)
     v

     (instance? java.util.Collection v)
     (mapv value-fn v)

     (instance? java.lang.Exception v)
     (render-exception v)

     :else
     (str v))))


(defn formatter
  "Construct a JSON event formatting function."
  [_output]
  (fn format-event
    [event]
    (json/write-str
      event
      :key-fn key-fn
      :value-fn value-fn
      :escape-slash false)))
