(ns dialog.format.pretty
  "Log format which presents events in ANSI-colored text, suitable for human
  consumption in a REPL console."
  (:require
    [clojure.string :as str]
    [io.aviso.ansi :as ansi]
    [io.aviso.exception :as ex])
  (:import
    (java.time
      Instant
      LocalDateTime
      ZoneId)
    (java.time.format
      DateTimeFormatter)))


(defn- rpad
  "Pad a string on the right with spaces to make it fit a certain visual width."
  [string width]
  (let [vlen (ansi/visual-length string)]
    (if (<= width vlen)
      string
      (apply str string (repeat (- width vlen) " ")))))


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
    (if-let [logger (some-> logger str)]
      (if (< max-length (count logger))
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
              candidate)))
        ;; Logger name fits in limit.
        logger)
      "-")))


(defn formatter
  "Construct a pretty event formatting function."
  [output]
  (let [format-time (timestamp-formatter (:timestamp output :full))]
    (fn format-message
      [event]
      (str
        ;; Timestamp
        (format-time (:time event))
        " "
        ;; Thread
        (rpad (format-thread (:thread event)) 24)
        " "
        ;; Level
        (rpad (format-level (:level event)) 5)
        " "
        ;; Logger
        (rpad (format-logger (:logger event) 30) 30)
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
        (when-let [extra (-> (:dialog.format/extra (meta event) event)
                             (dissoc :time :level :logger :line :message
                                     :duration :host :proc :sys :thread :error)
                             (not-empty))]
          (str "  " (ansi/cyan (pr-str extra))))
        ;; Exceptions
        (when-let [ex (:error event)]
          (str "\n" (ex/format-exception ex)))))))
