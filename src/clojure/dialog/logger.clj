(ns dialog.logger
  "Logging implementation logic and integration with SLF4J."
  (:require
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


(defn enabled?
  "True if the given logger is enabled at the provided level."
  [logger level]
  ;; TODO: implement
  true)


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
