(ns dialog.logger
  "Logging implementation logic and integration with SLF4J."
  ,,,)


(defn enabled?
  "True if the given logger is enabled at the provided level."
  [logger-name level]
  ;; TODO: implement
  true)


(defn log-event
  "Pass an event into the logging system."
  [config event]
  ;; TODO: implement
  (printf (prn-str event)))
