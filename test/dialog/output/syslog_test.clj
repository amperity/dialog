(ns dialog.output.syslog-test
  (:require
    [clojure.test :as test :refer [deftest is]]
    [dialog.output.syslog :as syslog]))


(deftest message-encoding
  (letfn [(encode
            [level message]
            (String.
              ^bytes
              (#'syslog/encode-payload
               "2020-04-05T08:37:15.023Z"
               "executor.local"
               "foo"
               "foo.0"
               level
               message)))]
    (is (= "<166>1 2020-04-05T08:37:15.023Z executor.local foo foo.0 - - Engaging the foo frobbler\n"
           (encode :debug "Engaging the foo frobbler")))
    (is (= "<165>1 2020-04-05T08:37:15.023Z executor.local foo foo.0 - - Cranking the bar bazzer\n"
           (encode :info "Cranking the bar bazzer")))
    (is (= "<164>1 2020-04-05T08:37:15.023Z executor.local foo foo.0 - - Noticed a short in the qux capacitor!\n"
           (encode :warn "Noticed a short in the qux capacitor!")))
    (is (= "<163>1 2020-04-05T08:37:15.023Z executor.local foo foo.0 - - SHIT SHIT WHO IS SHOOTING US\n"
           (encode :error "SHIT SHIT WHO IS SHOOTING US")))
    (is (= "<160>1 2020-04-05T08:37:15.023Z executor.local foo foo.0 - - x_x\n"
           (encode :fatal "x_x")))))
