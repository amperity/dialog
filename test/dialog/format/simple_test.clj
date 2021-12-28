(ns dialog.format.simple-test
  (:require
    [clojure.string :as str]
    [clojure.test :refer [deftest testing is]]
    [dialog.format.simple :as simple]
    [io.aviso.ansi :as ansi])
  (:import
    java.time.Instant))


(deftest thread-formatting
  (let [format-thread #'simple/format-thread]
    (is (= "[-]" (format-thread nil)))
    (is (= "[main]" (format-thread "main")))))


(deftest level-formatting
  (let [format-level #'simple/format-level]
    (is (= "TRACE" (format-level :trace)))
    (is (= "DEBUG" (format-level :debug)))
    (is (= "INFO"  (format-level :info)))
    (is (= "WARN"  (format-level :warn)))
    (is (= "ERROR" (format-level :error)))
    (is (= "FATAL" (format-level :fatal)))))


(deftest message-formatting
  (let [fmt (simple/formatter {})
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
      (is (= "2021-12-27T15:33:18Z [main]                   WARN  foo.bar.baz                     Another thing  {:foo.alpha 123}"
             (fmt {:time inst
                   :level :warn
                   :logger "foo.bar.baz"
                   :message "Another thing"
                   :thread "main"
                   :foo.alpha 123}))
          "default extra info shows")
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
            message (ansi/strip-ansi
                      (fmt {:level :error
                            :logger "foo.bar.baz"
                            :error ex}))]
        (is (str/includes? message "java.lang.RuntimeException: BOOM"))))))
