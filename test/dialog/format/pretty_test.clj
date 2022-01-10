(ns dialog.format.pretty-test
  (:require
    [clojure.string :as str]
    [clojure.test :refer [deftest testing is]]
    [dialog.format.pretty :as pretty]
    [io.aviso.ansi :as ansi])
  (:import
    java.time.Instant))


(deftest timestamp-formatting
  (let [timestamp-formatter #'pretty/timestamp-formatter
        inst (Instant/parse "2021-12-27T15:33:18.123Z")]
    (testing "bad argument"
      (is (thrown? IllegalArgumentException
            (timestamp-formatter :none))))
    (testing "full"
      (let [fmt (timestamp-formatter :full)]
        (is (= "2021-12-27T15:33:18.123Z" (fmt inst)))))
    (testing "short"
      (let [fmt (timestamp-formatter :short)]
        (is (= "15:33:18.123" (fmt inst)))))))


(deftest thread-formatting
  (let [format-thread (comp ansi/strip-ansi #'pretty/format-thread)]
    (is (= "[-]" (format-thread nil)))
    (is (= "[main]" (format-thread "main")))
    (is (= "[nREPL]" (format-thread "nREPL-session-1234-5678-90ab")))))


(deftest level-formatting
  (let [format-level (comp ansi/strip-ansi #'pretty/format-level)]
    (is (= "TRACE" (format-level :trace)))
    (is (= "DEBUG" (format-level :debug)))
    (is (= "INFO"  (format-level :info)))
    (is (= "WARN"  (format-level :warn)))
    (is (= "ERROR" (format-level :error)))
    (is (= "FATAL" (format-level :fatal)))))


(deftest logger-formatting
  (let [format-logger (comp ansi/strip-ansi #'pretty/format-logger)]
    (is (= "one" (format-logger "one" 30)))
    (is (= "one" (format-logger "one" 3)))
    (is (= "o" (format-logger "one" 2)))
    (is (= "one.two.three.four" (format-logger "one.two.three.four" 20)))
    (is (= "o.t.three.four" (format-logger "one.two.three.four" 15)))
    (is (= "o.t.t.four" (format-logger "one.two.three.four" 10)))
    (is (= "o.t.t.f" (format-logger "one.two.three.four" 7)))
    (is (= "o.t.t" (format-logger "one.two.three.four" 5)))))


(deftest message-formatting
  (let [fmt (comp ansi/strip-ansi (pretty/formatter {}))
        inst (Instant/parse "2021-12-27T15:33:18Z")]
    (testing "basic format"
      (is (= "2021-12-27T15:33:18Z [thread-pool-123]        INFO  foo.bar.baz                     Hello, logger!"
             (fmt {:time inst
                   :level :info
                   :logger "foo.bar.baz"
                   :message "Hello, logger!"
                   :thread "thread-pool-123"}))))
    (testing "with duration"
      (is (= "2021-12-27T15:33:18Z [main]                   WARN  foo.bar.baz                     Another thing (123.456 ms)"
             (fmt {:time inst
                   :level :warn
                   :logger "foo.bar.baz"
                   :message "Another thing"
                   :thread "main"
                   :duration 123.456}))))
    (testing "with custom tail"
      (is (= "2021-12-27T15:33:18Z [main]                   WARN  foo.bar.baz                     Another thing <CUSTOM TAIL>"
             (fmt (vary-meta
                    {:time inst
                     :level :warn
                     :logger "foo.bar.baz"
                     :message "Another thing"
                     :thread "main"}
                    assoc
                    :dialog.format/tail "<CUSTOM TAIL>")))))
    (testing "with extra info"
      (is (= "2021-12-27T15:33:18Z [main]                   WARN  foo.bar.baz                     Another thing"
             (fmt {:time inst
                   :level :warn
                   :logger "foo.bar.baz"
                   :message "Another thing"
                   :thread "main"
                   :foo.alpha 123}))
          "default extra info is ignored")
      (is (= "2021-12-27T15:33:18Z [main]                   WARN  foo.bar.baz                     Another thing  {:bar :xyz}"
             (fmt (vary-meta
                    {:time inst
                     :level :warn
                     :logger "foo.bar.baz"
                     :message "Another thing"
                     :thread "main"
                     :foo.alpha 123}
                    assoc
                    :dialog.format/extra {:bar :xyz})))
          "custom extra info shows"))
    (testing "throwables"
      (let [ex (RuntimeException. "BOOM")
            message (fmt {:level :error
                          :logger "foo.bar.baz"
                          :error ex})]
        (is (str/includes? message "java.lang.RuntimeException: BOOM"))))
    (testing "without padding"
      (let [fmt (comp ansi/strip-ansi (pretty/formatter {:padding false}))]
        (is (= "2021-12-27T15:33:18Z [thread-pool-123] INFO foo.bar.baz  Hello, logger!"
               (fmt {:time inst
                     :level :info
                     :logger "foo.bar.baz"
                     :message "Hello, logger!"
                     :thread "thread-pool-123"})))))))
