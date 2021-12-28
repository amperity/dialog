(ns dialog.format.simple
  "Log format which presents events in simple plain text."
  (:require
    [clojure.stacktrace :as stacktrace]
    [clojure.string :as str]))


(defn- rpad
  "Pad a string on the right with spaces to make it fit a certain visual width."
  [string width]
  (let [vlen (count string)]
    (if (<= width vlen)
      string
      (apply str string (repeat (- width vlen) " ")))))


(defn- format-thread
  "Format a thread name for printing."
  [thread]
  (str "[" (or thread "-") "]"))


(defn- format-level
  "Format a log level for printing."
  [level]
  (str/upper-case (name level)))


(defn formatter
  "Construct a plain-text event formatting function."
  [_output]
  (fn format-event
    [event]
    (str
      ;; Timestamp
      (str (:time event))
      " "
      ;; Thread
      (rpad (format-thread (:thread event)) 24)
      " "
      ;; Level
      (rpad (format-level (:level event)) 5)
      " "
      ;; Logger
      (rpad (str (:logger event)) 30)
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
        (str "  " (pr-str extra)))
      ;; Exceptions
      (when-let [ex (:error event)]
        (str "\n" (with-out-str (stacktrace/print-cause-trace ex)))))))
