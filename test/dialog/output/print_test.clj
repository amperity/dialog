(ns dialog.output.print-test
  (:require
    [clojure.test :refer [deftest testing is]]
    [dialog.output.print :as print])
  (:import
    java.io.StringWriter))


(deftest event-writing
  (testing "bad args"
    (is (thrown? IllegalArgumentException
          (print/writer {:stream :bad}))))
  (testing "default stdout"
    (let [s (StringWriter.)
          write (binding [*out* s]
                  (print/writer {}))]
      (is (nil? (write {} "foo")))
      (is (= "foo\n" (str s)))))
  (testing "explicit stdout"
    (let [s (StringWriter.)
          write (binding [*out* s]
                  (print/writer {:stream :stdout}))]
      (is (nil? (write {} "bar")))
      (is (= "bar\n" (str s)))))
  (testing "explicit stderr"
    (let [s (StringWriter.)
          write (binding [*err* s]
                  (print/writer {:stream :stderr}))]
      (is (nil? (write {} "baz")))
      (is (= "baz\n" (str s)))))
  (testing "multiple writes"
    (let [s (StringWriter.)
          write (binding [*out* s]
                  (print/writer {}))]
      (is (nil? (write {} "alpha")))
      (is (nil? (write {} "beta")))
      (is (nil? (write {} "gamma")))
      (is (= "alpha\nbeta\ngamma\n" (str s))))))
