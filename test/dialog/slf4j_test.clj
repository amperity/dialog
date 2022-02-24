(ns dialog.slf4j-test
  (:require
    [clojure.test :refer [deftest is]]
    [dialog.logger :as log])
  (:import
    (org.slf4j
      LoggerFactory
      Marker)))


(deftest slf4j-logging
  (let [logged (atom [])
        logger (LoggerFactory/getLogger "dialog.test")
        ^Marker marker nil
        err (doto (RuntimeException. "BOOM")
              (.setStackTrace (into-array StackTraceElement [])))]
    (with-redefs [log/config (assoc log/config :outputs {:test {:type :null}})
                  log/write-output! (fn [_ _ event]
                                      (swap! logged conj [(:level event) (:message event) (:error event)]))]
      (.info logger "a plain message")
      (.info logger "a {} message" "formatted")
      (.warn logger marker "more {} formatted {} with {} args" ^"[Ljava.lang.Object;" (into-array Object ["complex" "message" "many"]))
      (.error logger marker "a {} message with {} and error" ^"[Ljava.lang.Object;" (into-array Object ["formatted" "args" err])))
    (let [[a b c d] @logged]
      (is (= [:info "a plain message" nil] a))
      (is (= [:info "a formatted message" nil] b))
      (is (= [:warn "more complex formatted message with many args" nil] c))
      (is (= [:error "a formatted message with args and error" err] d)))))
