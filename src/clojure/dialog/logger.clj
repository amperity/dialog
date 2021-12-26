(ns dialog.logger
  "Logging implementation logic and integration with SLF4J."
  (:require
    [dialog.config :as config]
    [dialog.logger.util :as u]))


(def config
  "Global logging configuration reference."
  nil)


(defn initialize!
  "Load and initialize the logging system configuration."
  []
  (let [cfg (config/load-config)]
    (alter-var-root #'config (constantly cfg))
    nil))


(defn enabled?
  "True if the given logger is enabled at the provided level."
  [logger level]
  ;; TODO: implement
  true)


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
  (when-let [event (apply-middleware event (:middleware config))]
    (run!
      (fn write-output
        [[id output]]
        (try
          (let [format-event (or (:formatter output) :message)
                write-event (:writer output)]
            (when write-event
              (let [payload (format-event event)]
                (write-event event payload))))
          (catch Exception ex
            (u/print-err :output
                         "Failed to write to output %s: %s"
                         (name id)
                         (ex-message ex)))))
      (:outputs config))))


(defn log-message
  "Pass a message into the logging system. Used primarily by the SLF4J logging
  integration."
  [level msg err]
  (log-event {:level level
              :message msg
              :error err}))
