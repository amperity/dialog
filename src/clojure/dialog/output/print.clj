(ns dialog.output.print
  "Log appender which prints events to an output stream.")


(defn writer
  "Construct a print event writer function."
  [output]
  (let [stream (case (:stream output)
                 nil     *out*
                 :stdout *out*
                 :stderr *err*)]
    (fn write-event
      [_event message]
      (binding [*out* stream]
        (print (str message \newline))
        (flush)))))
