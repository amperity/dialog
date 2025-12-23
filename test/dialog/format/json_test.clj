(ns dialog.format.json-test
  (:require
    [clojure.string :as str]
    [clojure.test :refer [deftest testing is]]
    [clj-time.core :as time]
    [dialog.format.json :as json])
  (:import
    java.time.Instant))


(deftype Weird
  [x]

  Object

  (toString
    [_]
    (str "<Weird:" x ">")))


(deftest message-formatting
  (let [fmt (json/formatter {})]
    (testing "basic operation"
      (is (= "{}" (fmt {})))
      (is (= "{\"level\":\"info\",\"message\":\"Hello!\"}"
             (fmt {:level :info, :message "Hello!"})))
      (is (= "{\"time\":\"2021-12-27T15:17:31Z\"}"
             (fmt {:time (Instant/parse "2021-12-27T15:17:31Z")}))
          "instants format as strings"))
    (testing "weird values"
      (is (= "{\"nil\":null}"
             (fmt {nil nil})))
      (is (= "{\"wat\":\"<Weird:true>\"}"
             (fmt {:wat (->Weird true)}))))
    (testing "namespaced keys"
      (is (= "{\"foo.bar/baz?\":true}"
             (fmt {:foo.bar/baz? true}))))
    (testing "nested maps"
      (is (= "{\"abc\":{\"def\":true}}"
             (fmt {"abc" {"def" true}}))))
    (testing "nested collections"
      (is (= "{\"coll\":[\"def\",123,\"<Weird:abc>\"]}"
             (fmt {'coll [:def 123 (->Weird "abc")]}))))
    (testing "throwables"
      (let [ex (RuntimeException. "BOOM")
            message (fmt {:error ex})]
        (is (str/starts-with? message "{\"error\":[{\"class-name\":\"java.lang.RuntimeException\","))))
    (testing "types that don't implement JSONWriter"
      (let [date-time (time/date-time 2025 5 5)
            event {:date-time date-time}]
        (with-redefs [json/default-write-fn (fn [& _] (throw (Exception. "default-write-fn called")))]
          (is (thrown? Exception #"^default-write-fn called$"
                (fmt event))))
        (is (= "{\"date-time\":\"2025-05-05T00:00:00.000Z\"}"
               (fmt {:date-time date-time})))))))
