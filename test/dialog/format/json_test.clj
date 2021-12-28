(ns dialog.format.json-test
  (:require
    [clojure.string :as str]
    [clojure.test :refer [deftest testing is]]
    [dialog.format.json :as json])
  (:import
    java.time.Instant))


(deftest message-formatting
  (let [fmt (json/formatter {})]
    (testing "basic operation"
      (is (= "{}" (fmt {})))
      (is (= "{\"level\":\"info\",\"message\":\"Hello!\"}"
             (fmt {:level :info, :message "Hello!"})))
      (is (= "{\"time\":\"2021-12-27T15:17:31Z\"}"
             (fmt {:time (Instant/parse "2021-12-27T15:17:31Z")}))
          "instants format as strings"))
    (testing "throwables"
      (let [ex (RuntimeException. "BOOM")
            message (fmt {:error ex})]
        (is (str/starts-with? message "{\"error\":[{\"class-name\":\"java.lang.RuntimeException\","))
        ,,,))))
