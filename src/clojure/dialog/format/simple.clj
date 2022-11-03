(ns dialog.format.simple
  "Log format which presents events in simple plain text."
  (:require
    [clojure.stacktrace :as stacktrace]
    [clojure.string :as str])
  (:import
    java.util.Locale))


(defn- field-widths
  "Determine the configured field padding widths for the output formatter."
  [output]
  (let [padding (:padding output true)]
    (merge
      {:level 5
       :thread 24
       :logger 30}
      (cond
        (map? padding)
        padding

        (false? padding)
        {:level 0
         :thread 0
         :logger 0}))))


(defn- rpad
  "Pad a string on the right with spaces to make it fit a certain visual width."
  [string width]
  (if (pos-int? width)
    (let [vlen (count string)]
      (if (<= width vlen)
        string
        (apply str string (repeat (- width vlen) " "))))
    string))


(defn- format-thread
  "Format a thread name for printing."
  [thread]
  (str "[" (or thread "-") "]"))


(defn- format-level
  "Format a log level for printing."
  [level]
  (str/upper-case (name level)))


(defn- format-duration
  [locale fmt duration]
  (String/format locale fmt (to-array [duration])))


(defn formatter
  "Construct a plain-text event formatting function.

  Formatting options may include:

  - `:padding`

    Either true (the default) to pad fields to standard fixed widths, false to
    print them with no padding, or a map with `:level`, `:thread`, and `:logger`
    widths to specify custom amounts.

  - `:locale`

    The locale to use when formatting dureation. 
    Defaults to default platform locale (java.util.Locale/getDefault)."
  [output]
  (let [widths (field-widths output)
        level-width (:level widths)
        thread-width (:thread widths)
        logger-width (:logger widths)
        locale (or (:locale output)
                   (Locale/getDefault))]
    (fn format-event
      [event]
      (str
        ;; Timestamp
        (str (:time event))
        " "
        ;; Thread
        (rpad (format-thread (:thread event)) thread-width)
        " "
        ;; Level
        (rpad (format-level (:level event)) level-width)
        " "
        ;; Logger
        (rpad (str (:logger event)) logger-width)
        "  "
        ;; Message
        (or (:message event) "-")
        ;; Duration
        (when-let [duration (:duration event)]
          (format-duration locale " (%.3f ms)" duration))
        ;; Custom trailer
        (when-let [tail (:dialog.format/tail (meta event))]
          (str " " tail))
        ;; Extra Data
        (when-let [extra (:dialog.format/extra (meta event))]
          (str "  " (pr-str extra)))
        ;; Exceptions
        (when-let [ex (:error event)]
          (str "\n" (with-out-str (stacktrace/print-cause-trace ex))))))))
