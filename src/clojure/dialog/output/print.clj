(ns dialog.output.print
  "Log appender which prints events to an output stream.")


(defn writer
  "Construct a print event writer function."
  [output]
  (fn write-event
    [event payload]
    ;; TODO: implement
    ,,,))
