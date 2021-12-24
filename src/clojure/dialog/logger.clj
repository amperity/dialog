(ns dialog.logger
  "Logging implementation logic and integration with SLF4J."
  ,,,)


(defn enabled?
  "True if the given logger is enabled at the provided level."
  [config logger-name level]
  ;; TODO: implement
  true)


(defn log-event
  "Pass an event into the logging system."
  [config event]
  ;; TODO: implement
  (printf (prn-str event)))


(defn log-message
  "Pass a message into the logging system. Used primarily by the SLF4J logging
  integration."
  [config level msg err]
  (log-event config {:level level
                     :message msg
                     :error err}))
