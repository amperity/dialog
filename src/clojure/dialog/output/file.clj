(ns dialog.output.file
  "Log appender which writes events to a file.

  NOTE: this is *NOT* a performant implementation."
  (:require
    [clojure.java.io :as io])
  (:import
    java.io.BufferedWriter))


(defn writer
  "Construct a file event writer function."
  [output]
  {:pre [(string? (:path output))]}
  (let [path (:path output)
        lock (Object.)]
    (io/make-parents path)
    (fn write-event
      [_event message]
      (locking lock
        (with-open [^BufferedWriter out (io/writer path :append true)]
          (.write out (str message))
          (.newLine out))))))
