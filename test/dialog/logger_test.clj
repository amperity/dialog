(ns dialog.logger-test
  (:require
    [clojure.string :as str]
    [clojure.test :refer [deftest testing is]]
    [dialog.logger :as log])
  (:import
    java.io.StringWriter
    java.time.Instant))


(defmacro with-err-capture
  "Evaluate the body of expressions with the `*err*` stream bound to a string
  writer."
  [& body]
  `(binding [*err* (StringWriter.)]
     (with-redefs [dialog.util/acquire-err-token! (constantly 0)]
       ~@body)))


(deftest initialization
  (is (nil? (log/initialize!)))
  (is (map? log/config))
  (is (keyword? (:level log/config))))


(deftest log-levels
  (log/clear-levels!)
  (alter-var-root #'log/config assoc
                  :level :info
                  :levels {"foo" :debug
                           "foo.bar" :warn
                           "baz" :error}
                  :blocked #{"bad"})
  (testing "valid keywords"
    (is (every? log/valid-level? [:trace :debug :info :warn :error :fatal :off])))
  (testing "level resolution"
    (is (= {"foo" :debug
            "foo.bar" :warn
            "baz" :error}
           (log/get-levels)))
    (with-redefs [log/config {}]
      (is (= :info (log/get-level))
          "root level should default to info with no config"))
    (is (= :info (log/get-level)))
    (is (= :debug (log/get-level "foo")))
    (is (= :debug (log/get-level "foo.abc.def")))
    (is (= :warn (log/get-level "foo.bar.baz")))
    (is (= :error (log/get-level "baz.xyz")))
    (is (= :info (log/get-level "something.else")))
    (is (= :off (log/get-level "bad.code"))))
  (testing "enabled checks"
    (is (log/enabled? "foo.abc.def" :debug))
    (is (log/enabled? "foo.abc.def" :warn))
    (is (not (log/enabled? "baz.xyz" :trace)))
    (is (not (log/enabled? "bad.f00d" :fatal))))
  (testing "level setting"
    (testing "with bad args"
      (is (thrown? IllegalArgumentException
            (log/set-level! :foo)))
      (is (thrown? IllegalArgumentException
            (log/set-level! 123 :info)))
      (is (thrown? IllegalArgumentException
            (log/set-level! "baz" :wrong))))
    (testing "for root logger"
      (is (nil? (log/set-level! :warn)))
      (is (= :warn (log/get-level))))
    (testing "for specific loggers"
      (is (nil? (log/set-level! "foo" :error)))
      (is (= :error (log/get-level "foo.abc.def")))))
  (testing "level clearing"
    (is (nil? (log/clear-levels!)))
    (is (empty? (log/get-levels)))))


(deftest event-defaults
  (let [t (Instant/parse "2021-12-28T11:01:50Z")
        defaults (#'log/apply-defaults {:error nil})]
    (is (inst? (:time defaults))
        "should fill in current time")
    (is (string? (:thread defaults))
        "should fill in current thread")
    (is (string? (:host defaults))
        "should fill in hostname")
    (is (not (contains? defaults :error))
        "should not set error")
    (is (= {:time t
            :thread "main"
            :host "test"
            :error "boom"}
           (#'log/apply-defaults
            {:time t
             :thread "main"
             :host "test"
             :error "boom"}))
        "should not override present values")))


(deftest middleware-application
  (testing "without functions"
    (is (= {:x 1} (#'log/apply-middleware {:x 1} []))))
  (testing "with error"
    (with-err-capture
      (is (= {:x 2, :y 3}
             (#'log/apply-middleware
              {:x 1}
              [(fn [_ event] (assoc event :y 3))
               (fn [_ _] (throw (RuntimeException. "BOOM")))
               (fn [_ event] (update event :x inc))])))
      (is (str/includes? (str *err*) "Failed to apply middleware function"))))
  (testing "filtering events"
    (is (nil? (#'log/apply-middleware
               {:x 1}
               [(fn [_ event] (assoc event :y 3))
                (fn [_ _] nil)
                (fn [_ event] (update event :x inc))])))))


(deftest output-writing
  (testing "with error"
    (with-err-capture
      (is (nil? (#'log/write-output!
                 :test
                 {:writer (fn [_ _] (throw (RuntimeException. "BOOM")))
                  :formatter :message}
                 {:logger "foo.bar"
                  :level :info
                  :message "hello"})))
      (is (str/includes? (str *err*)
                         "Failed to write to output test: BOOM"))))
  (testing "without writer"
    (with-err-capture
      (is (nil? (#'log/write-output!
                 :test
                 {:formatter :message}
                 {:logger "foo.bar"
                  :level :info
                  :message "hello"})))
      (is (= "" (str *err*)))))
  (testing "without formatter"
    (let [writes (atom [])]
      (is (nil? (#'log/write-output!
                 :test
                 {:writer (fn [event message] (swap! writes conj message))}
                 {:logger "foo.bar"
                  :level :info
                  :message "hello"})))
      (is (= ["hello"] @writes))))
  (testing "happy path"
    (let [writes (atom [])]
      (is (nil? (#'log/write-output!
                 :test
                 {:writer (fn [event message] (swap! writes conj message))
                  :formatter (fn [event] (str "**" (:message event) "!**"))}
                 {:logger "foo.bar"
                  :level :info
                  :message "hello"})))
      (is (= ["**hello!**"] @writes)))))


(deftest event-logging
  (log/set-level! "foo.bar" :info)
  (testing "log-event"
    (let [logged (atom [])]
      (with-redefs [log/config (assoc log/config :outputs {:test {:type :null}})
                    log/write-output! (fn [id output event]
                                        (swap! logged conj [id (:level event) (:logger event)]))]
        (testing "with bad properties"
          (is (nil? (log/log-event {:logger 123
                                    :level :info})))
          (is (nil? (log/log-event {:logger "foo.bar.baz"
                                    :level true})))
          (is (empty? @logged)))
        (testing "with unmet threshold"
          (is (nil? (log/log-event {:logger "foo.bar.baz"
                                    :level :debug})))
          (is (empty? @logged)))
        (testing "passed event"
          (is (nil? (log/log-event {:logger "foo.bar.baz"
                                    :level :info
                                    :message "heyo"})))
          (is (= [[:test :info "foo.bar.baz"]] @logged))))))
  (testing "log-message"
    (let [logged (atom [])]
      (with-redefs [log/log-event (fn [event]
                                    (swap! logged conj event)
                                    nil)]
        (is (nil? (log/log-message "abc.xyz" :info "a thing happened" nil)))
        (is (= [{:level :info
                 :logger "abc.xyz"
                 :message "a thing happened"
                 :error nil}]
               @logged))))))


(deftest logging-macros
  (log/set-level! "dialog.logger-test" :info)
  (let [logged (atom [])
        value "string value"
        ex (RuntimeException. "BOOM")]
    (with-redefs [log/log-event (fn [event]
                                  (swap! logged conj event)
                                  nil)]
      (testing "logp"
        (testing "simple form"
          (is (nil? (log/logp :info "one" "two" "three")))
          (is (nil? (log/logp :warn value)))
          (is (nil? (log/logp :debug "not enabled")))
          (is (nil? (log/logp :trace "not evaluated" (throw ex)))
              "should not evaluate arguments when not enabled")
          (is (= [{:level :info
                   :logger "dialog.logger-test"
                   :line 205
                   :message "one two three"}
                  {:level :warn
                   :logger "dialog.logger-test"
                   :line 206
                   :message "string value"}]
                 @logged))
          (swap! logged empty))
        (testing "maybe exception"
          (is (nil? (log/logp :info value "two" "three")))
          (is (nil? (log/logp :error ex "a boom happened")))
          (is (nil? (log/logp :debug value "not evaluated" (throw ex)))
              "should not evaluate arguments when not enabled")
          (is (= [{:level :info
                   :logger "dialog.logger-test"
                   :line 221
                   :message "string value two three"}
                  {:level :error
                   :logger "dialog.logger-test"
                   :line 222
                   :message "a boom happened"
                   :error ex}]
                 @logged))
          (swap! logged empty)))
      (testing "logf"
        (testing "simple form"
          (is (nil? (log/logf :info "one %s %s" "two" "three")))
          (is (nil? (log/logf :warn value)))
          (is (nil? (log/logf :debug "not enabled")))
          (is (nil? (log/logf :trace "%s not evaluated" (throw ex)))
              "should not evaluate arguments when not enabled")
          (is (= [{:level :info
                   :logger "dialog.logger-test"
                   :line 238
                   :message "one two three"}
                  {:level :warn
                   :logger "dialog.logger-test"
                   :line 239
                   :message "string value"}]
                 @logged))
          (swap! logged empty))
        (testing "maybe exception"
          (is (nil? (log/logf :info value "two" "three")))
          (is (nil? (log/logf :error ex "a boom happened")))
          (is (nil? (log/logf :debug value "not evaluated" (throw ex)))
              "should not evaluate arguments when not enabled")
          (is (= [{:level :info
                   :logger "dialog.logger-test"
                   :line 254
                   :message "string value"}
                  {:level :error
                   :logger "dialog.logger-test"
                   :line 255
                   :message "a boom happened"
                   :error ex}]
                 @logged))
          (swap! logged empty)))
      (testing "level macros"
        (is (nil? (log/trace "hello")))
        (is (nil? (log/tracef "hello")))
        (is (nil? (log/debug "hello")))
        (is (nil? (log/debugf "hello")))
        (is (nil? (log/info "hello")))
        (is (nil? (log/infof "hello")))
        (is (nil? (log/warn "hello")))
        (is (nil? (log/warnf "hello")))
        (is (nil? (log/error "hello")))
        (is (nil? (log/errorf "hello")))
        (is (nil? (log/fatal "hello")))
        (is (nil? (log/fatalf "hello")))))))
