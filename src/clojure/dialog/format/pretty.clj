(ns dialog.format.pretty
  "Log format which presents events in ANSI-colored text, suitable for human
  consumption in a REPL console."
  (:require
    [clojure.string :as str]
    [io.aviso.ansi :as ansi]
    [io.aviso.exception :as ex])
  (:import
    (java.time
      LocalDateTime
      ZoneId)
    (java.time.format
      DateTimeFormatter)))


(defn- rpad
  "Pad a string on the right with spaces to make it fit a certain visual width."
  [string width]
  (if (zero? width)
    string
    (let [vlen (ansi/visual-length string)]
      (if (<= width vlen)
        string
        (apply str string (repeat (- width vlen) " "))))))


(defn- timestamp-formatter
  "Constructs a new function to format instants using the given format string."
  [format-key]
  (case format-key
    :full
    str

    :short
    (let [dt-fmt (DateTimeFormatter/ofPattern "HH:mm:ss.SSS")
          utc (ZoneId/of "UTC")]
      (fn format-short
        [instant]
        (.format dt-fmt (LocalDateTime/ofInstant instant utc))))))


(defn- format-thread
  "Format a thread name for printing."
  [thread]
  (str "["
       (ansi/green
         (if thread
           (if (str/starts-with? (str/lower-case thread) "nrepl-session-")
             "nREPL"
             thread)
           "-"))
       "]"))


(defn- format-level
  "Format a log level for printing."
  [level]
  (let [level-str (str/upper-case (name level))]
    (case level
      :fatal (ansi/bold-magenta level-str)
      :error (ansi/bold-red level-str)
      :warn (ansi/red level-str)
      :info (ansi/blue level-str)
      level-str)))


(defn- format-logger
  "Format a logger name to fit within the desired max length."
  [logger max-length]
  (ansi/cyan
    (cond
      ;; Don't do trimming
      (zero? max-length)
      logger

      ;; Logger name fits in limit
      (<= (count logger) max-length)
      logger

      ;; Collapse logger segments
      :else
      (loop [collapsed []
             parts (str/split logger #"\.")]
        (let [candidate (str/join "." (concat collapsed parts))]
          (if (< max-length (count candidate))
            ;; Need to trim more.
            (if-let [next-part (first parts)]
              ;; Collapse next part in the ns into a single character.
              (recur (conj collapsed (subs next-part 0 1))
                     (rest parts))
              ;; No more parts, just truncate it.
              (subs candidate 0 max-length))
            ;; Abbreviated logger fits within limit.
            candidate))))))


(defn formatter
  "Construct a pretty event formatting function.

  Formatting options may include:

  - `:padding`

    Either true (the default) to pad fields to standard fixed widths, false to
    print them with no padding, or a map with `:level`, `:thread`, and `:logger`
    widths to specify custom amounts.

  - `:timestamp`

    Either `:full` (the default) which shows the entire timestamp value, or
    `:short` which will render only the local time portion."
  [output]
  (let [padding (:padding output true)
        widths (merge
                 {:level 5
                  :thread 24
                  :logger 30}
                 (cond
                   (map? padding)
                   padding

                   (false? padding)
                   {:level 0
                    :thread 0
                    :logger 0}))
        thread-width (:thread widths)
        level-width (:level widths)
        logger-width (:logger widths)
        format-time (timestamp-formatter (:timestamp output :full))]
    (fn format-message
      [event]
      (str
        ;; Timestamp
        (format-time (:time event))
        " "
        ;; Thread
        (rpad (format-thread (:thread event)) thread-width)
        " "
        ;; Level
        (rpad (format-level (:level event)) level-width)
        " "
        ;; Logger
        (rpad (format-logger (str (:logger event)) logger-width) logger-width)
        "  "
        ;; Message
        (or (:message event) "-")
        ;; Duration
        (when-let [duration (:duration event)]
          (format " (%.3f ms)" duration))
        ;; Custom trailer
        (when-let [tail (:dialog.format/tail (meta event))]
          (str " " tail))
        ;; Extra Data
        (when-let [extra (:dialog.format/extra (meta event))]
          (str "  " (ansi/cyan (pr-str extra))))
        ;; Exceptions
        (when-let [ex (:error event)]
          (str "\n" (ex/format-exception ex)))))))
