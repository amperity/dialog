(ns dialog.output.file
  "Log appender which writes events to a file.")


(defn writer
  "Construct a file event writer function."
  [output]
  (fn write-event
    [event payload]
    ;; TODO: implement
    ,,,))
