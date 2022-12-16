(ns dialog.logger
  "Logging implementation logic and integration with SLF4J."
  (:require
    [clojure.string :as str]
    [dialog.config :as config]
    [dialog.util :as u])
  (:import
    (dialog.logger
      DialogLogger
      Level)
    java.time.Instant))


;; What do log events look like?
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


;; ## Logging Configuration

(def config
  "Global logging configuration reference."
  nil)


(declare reset-level-cache!)


(defn initialize!
  "Load and initialize the logging system configuration."
  []
  (let [cfg (config/load-config)]
    (alter-var-root #'config (constantly cfg))
    (reset-level-cache!)
    nil))


;; ## Logger Levels

(def ^:private level-cache
  "Stateful cache of computed levels for specific loggers."
  (atom {} :validator map?))


(defn- reset-level-cache!
  "Reset the cache of logger levels and increment the Java cache version so
  loggers re-check."
  []
  (swap! level-cache empty)
  (DialogLogger/bumpCache)
  nil)


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


(defn ^:private some-prefixed?
  "Return true if logger is a prefix of at least one item in prefixes."
  [prefixes logger]
  (boolean
    (and (string? logger)
         (some #(prefixed? logger %)
               prefixes))))


(defn blocked?
  "True if the logger is blocked by configuration, otherwise false."
  ([logger]
   (blocked? (:blocked config) logger))
  ([blocked logger]
   (some-prefixed? blocked logger)))


(defn- match-block
  "Return `:off` if there is a blocking prefix matching this logger."
  ([logger]
   (when (blocked? logger)
     :off))
  ([levels logger]
   (when (blocked? levels logger)
     :off)))


(defn match-level
  "Get the value for the key which is the deepest prefix of the given logger
  name, or nil if no keys prefix the logger.
  Uses :levels from config by default."
  ([logger]
   (match-level (:levels config) logger))
  ([levels logger]
   (when-let [match (->> levels
                         (sort-by (comp count key) (comp - compare))
                         (filter #(prefixed? logger (key %)))
                         (first))]
     (val match))))


(defn valid-level?
  "True if the provided value is a valid logger level keyword."
  [k]
  (Level/isValid k))


(defn get-levels
  "Return a map of all configured logger names to level keywords."
  []
  (:levels config))


(defn get-level*
  ([logger options]
   (let [{:keys [level blocked levels level-cache]} options]
     (or (get @level-cache logger)
         (let [level (or (match-block blocked logger)
                         (match-level levels logger)
                         level)]
           (swap! level-cache assoc logger level)
           level)))))


(defn get-level
  "Get the current level setting for a logger. If no logger name is provided,
  this returns the root logger's level."
  ([]
   (or (:level config) :info))
  ([logger]
   (get-level* logger {:level (:level config)
                       :levels (:levels config)
                       :blocked (:blocked config)
                       :level-cache level-cache})))


(defn set-level!
  "Dynamically adjust the level for the named logger. If no name is provided,
  adjusts the level of the root logger. Returns nil."
  ([level]
   (when-not (valid-level? level)
     (throw (IllegalArgumentException.
              (str "Provided level " (pr-str level) " is not valid"))))
   (alter-var-root #'config assoc :level level)
   (reset-level-cache!))
  ([logger level]
   (when-not (string? logger)
     (throw (IllegalArgumentException.
              (str "Provided logger must be a string, got: " (pr-str logger)))))
   (when-not (valid-level? level)
     (throw (IllegalArgumentException.
              (str "Provided level " (pr-str level) " is not valid"))))
   (alter-var-root #'config assoc-in [:levels logger] level)
   (reset-level-cache!)))


(defn clear-levels!
  "Dynamically adjust the configuration to remove all logger levels. Does not
  change the root level. Returns nil."
  []
  (alter-var-root #'config assoc :levels {})
  (reset-level-cache!))


(defn enabled?
  "True if the given logger is enabled at the provided level."
  ([logger level]
   (enabled? {:level (:level config)
              :levels (:levels config)
              :blocked (:blocked config)
              :level-cache level-cache}
             logger level))
  ([opts logger level]
   (Level/isAllowed
     (Level/ofKeyword (get-level* logger opts))
     (Level/ofKeyword level))))


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


(defn- write-output!
  "Write an event to an output, applying any configured formatter."
  [id output event]
  (when-let [write! (:writer output)]
    (try
      (let [format-message (or (:formatter output) :message)
            message (format-message event)]
        (write! event message)
        nil)
      (catch Exception ex
        (u/print-err :output
                     "Failed to write to output %s: %s"
                     (name id)
                     (ex-message ex))))))


(defn log-event
  "Pass an event into the logging system."
  [event]
  (when-not config
    (initialize!))
  (when-let [event (and (string? (:logger event))
                        (keyword? (:level event))
                        (enabled? (:logger event) (:level event))
                        (-> event
                            (apply-defaults)
                            (apply-middleware (:middleware config))))]
    (run!
      (fn write-event
        [[id output]]
        (when-let [event (apply-middleware event (:middleware output))]
          (write-output! id output event)))
      (:outputs config))))


(defn ^:no-doc -log-slf4j
  "Pass a message into the logging system. Used by the SLF4J logging
  integration - consumers should call `log-event` directly."
  [logger level msg err]
  (log-event {:level level
              :logger logger
              :message msg
              :error err}))


(defn ^:no-doc -log-macro
  "Pass a message into the logging system. Used by the logging macro
  expansion - consumers should call `log-event` directly."
  [event f x & more]
  (log-event
    (if (instance? Throwable x)
      (assoc event
             :message (apply f more)
             :error x)
      (assoc event
             :message (apply f x more)))))


;; ## Logging APIs

(defn- coverage-mode?
  "True if the log macros should emit code in a 'coverage-friendly' mode."
  []
  (= "true" (or (System/getenv "DIALOG_COVERAGE")
                (System/getProperty "dialog.coverage"))))


(defmacro logp
  "Log a message using print style args. Can optionally take a throwable as its
  second arg."
  {:arglists '([level message & more] [level throwable message & more])}
  [level x & more]
  (let [logger (str (ns-name *ns*))
        line (:line (meta &form))]
    (cond
      ;; Coverage-friendly form. Args are always evaluated and passed to the
      ;; helper function.
      (coverage-mode?)
      `(-log-macro
         {:logger ~logger
          :level ~level
          :line ~line}
         print-str
         ~x
         ~@more)

      ;; First argument can't be an error, use simpler form.
      (or (string? x) (nil? more))
      `(let [logger# ~logger
             level# ~level]
         (when (enabled? logger# level#)
           (log-event
             {:logger logger#
              :level level#
              :line ~line
              :message (print-str ~x ~@more)})))

      ;; General form.
      :else
      `(let [logger# ~logger
             level# ~level]
         (when (enabled? logger# level#)
           (-log-macro
             {:logger logger#
              :level level#
              :line ~line}
             print-str
             ~x
             ~@more))))))


(defmacro logf
  "Log a message using a format string and args. Can optionally take a
  throwable as its second arg."
  {:arglists '([level fmt & fmt-args] [level throwable fmt & fmt-args])}
  [level x & more]
  (let [logger (str (ns-name *ns*))
        line (:line (meta &form))]
    (cond
      ;; Coverage-friendly form. Args are always evaluated and passed to the
      ;; helper function.
      (coverage-mode?)
      `(-log-macro
         {:logger ~logger
          :level ~level
          :line ~line}
         format
         ~x
         ~@more)

      ;; First argument can't be an error, use simpler form.
      (or (string? x) (nil? more))
      `(let [logger# ~logger
             level# ~level]
         (when (enabled? logger# level#)
           (log-event
             {:logger logger#
              :level level#
              :line ~line
              :message (format ~x ~@more)})))

      ;; General form.
      :else
      `(let [logger# ~logger
             level# ~level]
         (when (enabled? logger# level#)
           (-log-macro
             {:logger logger#
              :level level#
              :line ~line}
             format
             ~x
             ~@more))))))


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


(defmacro fatal
  "Fatal level logging using print-style args."
  {:arglists '([message & more] [throwable message & more])}
  [& args]
  `(logp :fatal ~@args))


(defmacro fatalf
  "Fatal level logging using format."
  {:arglists '([fmt & fmt-args] [throwable fmt & fmt-args])}
  [& args]
  `(logf :fatal ~@args))
