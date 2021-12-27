(ns dialog.logger
  "Logging implementation logic and integration with SLF4J."
  (:require
    [clojure.string :as str]
    [dialog.config :as config]
    [dialog.logger.util :as u])
  (:import
    java.time.Instant))


;; ## Logging Configuration

(def config
  "Global logging configuration reference."
  nil)


(defn initialize!
  "Load and initialize the logging system configuration."
  []
  (let [cfg (config/load-config)]
    (alter-var-root #'config (constantly cfg))
    nil))


;; ## Logger Levels

(def ^:private level-cache
  "Stateful cache of computed levels for specific loggers."
  (atom {} :validator map?))


(defn valid-level?
  "True if the provided value is a valid logger level keyword."
  [x]
  (contains? #{:trace :debug :info :warn :error} x))


(defn level-above?
  "True if the level keyword is equal to or greater in severity than the
  threshold."
  [threshold level]
  (case threshold
    :trace
    true

    :debug
    (case level
      :trace false
      true)

    :info
    (case level
      (:trace :debug) false
      true)

    :warn
    (case level
      (:warn :error) true
      false)

    :error
    (identical? :error level)

    :off
    false))


(defn- prefixed?
  "True if `logger-name` is equal to or prefixed by `prefix`. Here 'prefixing'
  means adding a period to the end of `prefix` to ensure the logger is a
  descendant and not merely a similarly-named logger.

  For example, if we're given the prefix `\"foo.bar\"` then the following
  logger-names evaluate to:

      \"foo.bar\" => true
      \"foo.bar.baz\" => true
      \"foo.bartle\" => false
  "
  [logger prefix]
  (or (= logger prefix)
      (str/starts-with? logger (str prefix \.))))


(defn- match-block
  "Return `:off` if there is a blocking prefix matching this logger."
  [logger]
  (when (first (filter #(prefixed? logger %) (:blocked config)))
    :off))


(defn- match-level
  "Get the value for the key which is the deepest prefix of the given logger
  name, or nil if no keys prefix the logger."
  [logger]
  (when-let [match (->> (:levels config)
                        (sort-by (comp count key) (comp - compare))
                        (filter #(prefixed? logger (key %)))
                        (first))]
    (val match)))


(defn get-levels
  "Return a map of all configured logger names to level keywords."
  []
  (:levels config))


(defn get-level
  "Get the current level setting for a logger. If no logger name is provided,
  this returns the root logger's level."
  ([]
   (or (:level config) :info))
  ([logger]
   (or (get @level-cache logger)
       (let [level (or (match-block logger)
                       (match-level logger)
                       (get-level))]
         (swap! level-cache assoc logger level)
         level))))


(defn set-level!
  "Dynamically adjust the level for the named logger. If no name is provided,
  adjusts the level of the root logger. Returns nil."
  ([level]
   {:pre [(valid-level? level)]}
   (alter-var-root #'config assoc :level level)
   (swap! level-cache empty)
   nil)
  ([logger level]
   {:pre [(string? logger) (valid-level? level)]}
   (alter-var-root #'config assoc-in [:levels logger] level)
   (swap! level-cache empty)
   nil))


(defn clear-levels!
  "Dynamically adjust the configuration to remove all logger levels. Does not
  change the root level. Returns nil."
  []
  (alter-var-root #'config assoc :levels {})
  (swap! level-cache empty)
  nil)


(defn enabled?
  "True if the given logger is enabled at the provided level."
  [logger level]
  (level-above? (get-level logger) level))


;; ## Event Logging

(defn- apply-defaults
  "Apply defaults to the given event, efficiently inserting information that is
  not present."
  [event]
  (cond-> event
    (nil? (:time event))
    (assoc :time (Instant/now))

    (nil? (:thread event))
    (assoc :thread (.getName (Thread/currentThread)))

    (nil? (:host event))
    (assoc :host (u/get-hostname))

    (nil? (:error event))
    (dissoc :error)))


(defn- apply-middleware
  "Apply a sequence of middleware functions to an event."
  [event middleware]
  (reduce
    (fn [event f]
      (when event
        (try
          (f config event)
          (catch Exception ex
            (u/print-err :middleware
                         "Failed to apply middleware function %s: %s"
                         (.getName (class f))
                         (ex-message ex))
            event))))
    event
    middleware))


(defn log-event
  "Pass an event into the logging system."
  [event]
  (when-let [event (and (string? (:logger event))
                        (keyword? (:level event))
                        (enabled? (:logger event) (:level event))
                        (-> event
                            (apply-defaults)
                            (apply-middleware (:middleware config))))]
    (run!
      (fn write-output
        [[id output]]
        (try
          (let [format-event (or (:formatter output) :message)
                write-event (:writer output)]
            (when write-event
              (let [message (format-event event)]
                (write-event event message))))
          (catch Exception ex
            (u/print-err :output
                         "Failed to write to output %s: %s"
                         (name id)
                         (ex-message ex)))))
      (:outputs config))))


(defn log-message
  "Pass a message into the logging system. Used primarily by the SLF4J logging
  integration."
  [logger level msg err]
  (log-event {:level level
              :logger logger
              :message msg
              :error err}))


;; ## Logging APIs

(defmacro logp
  "Log a message using print style args. Can optionally take a throwable as its
  second arg."
  {:arglists '([level message & more] [level throwable message & more])}
  [level x & more]
  (let [logger (str (ns-name *ns*))
        line (:line (meta &form))]
    (if (or (string? x) (nil? more))
      `(when (enabled? ~logger ~level)
         (log-event {:level ~level
                     :logger ~logger
                     :line ~line
                     :message (print-str ~x ~@more)}))
      `(let [logger# ~logger]
         (when (enabled? logger# ~level)
           (let [x# ~x
                 more# (print-str ~@more)]
             (if (instance? Throwable x#)
               (log-event {:level ~level
                           :logger logger#
                           :line ~line
                           :message more#
                           :error x#})
               (log-event {:level ~level
                           :logger logger#
                           :line ~line
                           :message (str (print-str x#) " " more#)}))))))))


(defmacro logf
  "Log a message using a format string and args. Can optionally take a
  throwable as its second arg."
  {:arglists '([level fmt & fmt-args] [level throwable fmt & fmt-args])}
  [level x & more]
  (let [logger (str (ns-name *ns*))
        line (:line (meta &form))]
    (if (or (string? x) (nil? more))
      `(when (enabled? ~logger ~level)
         (log-event {:level ~level
                     :logger ~logger
                     :line ~line
                     :message (format ~x ~@more)}))
      `(let [logger# ~logger]
         (when (enabled? logger# ~level)
           (let [x# ~x]
             (if (instance? Throwable x#)
               (log-event {:level ~level
                           :logger logger#
                           :line ~line
                           :message (format ~@more)
                           :error x#})
               (log-event {:level ~level
                           :logger logger#
                           :line ~line
                           :message (format x# ~@more)}))))))))


(defmacro trace
  "Trace level logging using print-style args.
  Use the 'logging.readable' namespace to avoid wrapping args in pr-str."
  {:arglists '([message & more] [throwable message & more])}
  [& args]
  `(logp :trace ~@args))


(defmacro tracef
  "Trace level logging using format."
  {:arglists '([fmt & fmt-args] [throwable fmt & fmt-args])}
  [& args]
  `(logf :trace ~@args))


(defmacro debug
  "Debug level logging using print-style args."
  {:arglists '([message & more] [throwable message & more])}
  [& args]
  `(logp :debug ~@args))


(defmacro debugf
  "Debug level logging using format."
  {:arglists '([fmt & fmt-args] [throwable fmt & fmt-args])}
  [& args]
  `(logf :debug ~@args))


(defmacro info
  "Info level logging using print-style args."
  {:arglists '([message & more] [throwable message & more])}
  [& args]
  `(logp :info ~@args))


(defmacro infof
  "Info level logging using format."
  {:arglists '([fmt & fmt-args] [throwable fmt & fmt-args])}
  [& args]
  `(logf :info ~@args))


(defmacro warn
  "Warn level logging using print-style args."
  {:arglists '([message & more] [throwable message & more])}
  [& args]
  `(logp :warn ~@args))


(defmacro warnf
  "Warn level logging using format."
  {:arglists '([fmt & fmt-args] [throwable fmt & fmt-args])}
  [& args]
  `(logf :warn ~@args))


(defmacro error
  "Error level logging using print-style args."
  {:arglists '([message & more] [throwable message & more])}
  [& args]
  `(logp :error ~@args))


(defmacro errorf
  "Error level logging using format."
  {:arglists '([fmt & fmt-args] [throwable fmt & fmt-args])}
  [& args]
  `(logf :error ~@args))
