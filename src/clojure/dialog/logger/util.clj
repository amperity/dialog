(ns dialog.logger.util
  "Logging implementation utilities."
  (:require
    [clojure.java.shell :as sh]
    [clojure.string :as str])
  (:import
    java.net.InetAddress))


;; What do events look like?
#_
{;; Time the event occurred, as a java.time.Instant
 :time #inst "2021-12-25T10:56:32Z"
 ;; Event severity level.
 :level :info
 ;; Human-friendly message for the event.
 :message "foo bar baz"
 ;; Logger this event is for - usually the namespace or class file.
 :logger "com.acme.foo"
 ;; (optional) Line the log statement was sent from.
 :line 123
 ;; (optional) Hostname this event was generated on.
 :host "foo.acme.systems"
 ;; (optional) Process identifier.
 :proc "app.0"
 ;; (optional) Application system name.
 :sys "app"
 ;; (optional) Thread this event was generated in.
 :thread "main"
 ;; (optional) Duration in milliseconds for this event
 :duration 0.123
 ;; (optional) Throwable error associated with this event.
 :error (ex-info "..." {,,,})
 ;; (optional) Any other fields
 ,,,}


(let [hostname (delay
                 (or (try
                       (let [proc (sh/sh "hostname")]
                         (when (zero? (:exit proc))
                           (str/trim (:out proc))))
                       (catch Exception _
                         nil))
                     (try
                       (.getHostName (InetAddress/getLocalHost))
                       (catch Exception _
                         nil))
                     "localhost"))]
  (defn get-hostname
    "Get the string name of the local host computer."
    []
    @hostname))


;; ## Throttled Error Reporting

(def ^:private err-buckets
  "Atom containing a map from error type keywords to a bucket structure. Each
  bucket is a tuple of a timestamp since the last check, a token amount, and a
  count of messages suppressed since the last report."
  (atom {}))


(def ^:private err-bucket-size
  "Maximum depth to allow for error buckets."
  3.0)


(def ^:private err-bucket-rate
  "Rate at which errors can be reported. Expressed as a value per nanosecond."
  (/ 1 60 1e9))


(defn- acquire-token!
  "Attempt to acquire a token from the bucket for the given key. Updates the
  `err-buckets` state. Returns nil if a token was not available, or the number
  of suppressed messages if a token was available."
  [err-type]
  (let [prev @err-buckets
        now (System/nanoTime)
        [last-time supply suppressed] (or (get prev err-type) [now 3.0 0])
        supply' (-> (- now last-time)
                    (* err-bucket-rate)
                    (+ supply)
                    (min err-bucket-size))
        available? (<= 1.0 supply')
        bucket' (if available?
                  ;; Enough supply to emit a mesage.
                  [now (- supply' 1.0) 0]
                  ;; Not enough supply.
                  [now supply' (inc suppressed)])]
    (if (compare-and-set! err-buckets prev (assoc prev err-type bucket'))
      (when available?
        suppressed)
      (recur err-type))))


(defn print-err
  "Print a message to stderr when something goes wrong. Throttles output to
  roughly once a minute per unique key."
  [err-type message & args]
  (when-let [suppressed (acquire-token! err-type)]
    (binding [*out* *err*]
      (print (str "[dialog " (name err-type) " error] "
                  (apply format message args)
                  (when (pos? suppressed)
                    (str " (" suppressed " suppressed)"))
                  \newline))
      (flush)))
  nil)
