(ns dialog.output.print
  "Log appender which prints events to an output stream.")


(defn writer
  "Construct a print event writer function.

  Output options may include:

  - `:stream`

    A keyword indicating the stream to print to - either `:stdout` (the
    default) or `:stderr`."
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
