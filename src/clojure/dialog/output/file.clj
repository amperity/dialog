(ns dialog.output.file
  "Log appender which writes events to a file.

  **NOTE:** this output is NOT safe for use when multiple processes are writing
  to the same file!"
  (:require
    [clojure.java.io :as io])
  (:import
    java.io.BufferedWriter))


(defn writer
  "Construct a file event writer function.

  Output options should include:

  - `:path`

    String path to the log file to write to."
  [output]
  (when-not (string? (:path output))
    (throw (IllegalArgumentException.
             (str "File output writer requires a string :path, got: "
                  (pr-str (:path output))))))
  (let [path (:path output)
        lock (Object.)]
    (io/make-parents path)
    (fn write-event
      [_event message]
      (locking lock
        (with-open [^BufferedWriter out (io/writer path :append true)]
          (.write out (str message))
          (.newLine out))))))
