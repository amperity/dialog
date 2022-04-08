(ns ^:no-doc dialog.util
  "Implemenation utilities."
  (:import
    java.net.InetAddress))


;; ## Error Reporting

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


(defn- acquire-err-token!
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
  (when-let [suppressed (acquire-err-token! err-type)]
    (binding [*out* *err*]
      (print (str "[dialog " (name err-type) " error] "
                  (apply format message args)
                  (when (pos? suppressed)
                    (str " (" suppressed " suppressed)"))
                  \newline))
      (flush)))
  nil)


;; ## Miscellaneous

(def ^:private hostname-ref
  "A stateful reference to hold the current default hostname."
  (atom nil))


(defn set-hostname!
  "Set the default hostname to use in log events. Returns nil."
  [hostname]
  (when-not (string? hostname)
    (throw (IllegalArgumentException.
             (str "Hostname must be a string, got a "
                  (pr-str (class hostname))))))
  (reset! hostname-ref hostname)
  nil)


(defn get-hostname
  "Get the string name of the local host computer."
  []
  (or @hostname-ref
      (let [hostname (try
                       (.getHostName (InetAddress/getLocalHost))
                       (catch Exception ex
                         (print-err :hostname
                                    "Failed to resolve hostname with InetAddress: %s"
                                    (ex-message ex))
                         "localhost"))]
        (reset! hostname-ref hostname)
        hostname)))
