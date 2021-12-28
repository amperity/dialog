(ns dialog.output.syslog-test
  (:require
    [clojure.test :as test :refer [deftest is]]
    [dialog.output.syslog :as syslog])
  (:import
    (java.net
      DatagramPacket
      DatagramSocket)
    java.time.Instant))


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


(deftest packet-transmission
  (with-open [socket (DatagramSocket.)]
    (let [packet (DatagramPacket. (byte-array 1024) 1024)
          write (syslog/writer {:port (.getLocalPort socket)})
          receipt (future
                    (.receive socket packet))]
      (write {:time (Instant/parse "2021-12-27T18:16:08.345Z")
              :level :info
              :host "executor.local"
              :proc "foo.0"
              :sys "foo"}
             "Good news, everyone!")
      (when (identical? ::timeout (deref receipt 1000 ::timeout))
        (future-cancel receipt)
        (throw (RuntimeException. "Packet receipt timed out")))
      (let [message (String. (.getData packet) 0 (.getLength packet))]
        (is (= "<165>1 2021-12-27T18:16:08.345Z executor.local foo foo.0 - - Good news, everyone!\n"
               message))))))
