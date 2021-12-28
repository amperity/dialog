(ns dialog.output.file-test
  (:require
    [clojure.java.io :as io]
    [clojure.string :as str]
    [clojure.test :refer [deftest testing is]]
    [dialog.output.file :as file]))


(defn- test-file
  "Create a File object for a test log file. Does not actually create a file on
  disk."
  []
  (let [path "target/test/logs"
        fname (str "out-" (System/currentTimeMillis) "-" (rand-int 10000) ".log")
        file (io/file path fname)]
    (.deleteOnExit file)
    file))


(deftest event-writing
  (testing "bad args"
    (is (thrown? IllegalArgumentException
          (file/writer {}))
        "should throw error without path")
    (is (thrown? IllegalArgumentException
          (file/writer {:path 123}))
        "should throw error with non-string path"))
  (testing "basic operation"
    (let [log-file (test-file)
          write (file/writer {:path (str log-file)})]
      (write {} "hello there")
      (is (= "hello there\n" (slurp log-file)))))
  (testing "serial writes"
    (let [log-file (test-file)
          write (file/writer {:path (str log-file)})]
      (write {} "one")
      (write {} "two")
      (write {} "three")
      (is (= "one\ntwo\nthree\n" (slurp log-file)))))
  (testing "concurrent writes"
    (let [log-file (test-file)
          write (file/writer {:path (str log-file)})
          alpha (future
                  (write {} "alpha"))
          beta (future
                 (write {} "beta"))
          gamma (future
                  (write {} "gamma"))]
      @alpha
      @beta
      @gamma
      (let [text (slurp log-file)]
        (is (= 17 (count text)))
        (is (str/includes? text "alpha\n"))
        (is (str/includes? text "beta\n"))
        (is (str/includes? text "gamma\n"))))))
