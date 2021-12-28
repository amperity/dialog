(ns dialog.format.json
  "Log format which presents events as JSON objects for structured logging."
  (:require
    [clojure.data.json :as json]
    [io.aviso.exception :as ex]))


(defn- kw->str
  "Coerce a keyword into a string, preserving its namespace."
  [k]
  (when k
    (subs (str k) 1)))


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


(defn formatter
  "Construct a JSON event formatting function."
  [_output]
  (fn format-event
    [event]
    (json/write-str
      (cond-> event
        (:error event)
        (update :error render-exception))
      :key-fn kw->str
      :escape-slash false)))
