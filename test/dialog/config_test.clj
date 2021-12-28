(ns dialog.config-test
  (:require
    [clojure.string :as str]
    [clojure.test :refer [deftest testing is]]
    [dialog.config :as cfg])
  (:import
    java.io.StringWriter))


(defmacro with-err-capture
  "Evaluate the body of expressions with the `*err*` stream bound to a string
  writer."
  [& body]
  `(binding [*err* (StringWriter.)]
     ~@body))


(deftest setting-priority
  (is (= (System/getenv "HOME") (#'cfg/some-setting "HOME" "user.home" "~")))
  (System/setProperty "dialog.config-test.abc" "xyz")
  (is (= "xyz" (#'cfg/some-setting "DIALOG_CONFIG_TEST_ABC"
                                   "dialog.config-test.abc"
                                   :default)))
  (is (= :oh-well (#'cfg/some-setting "DIALOG_CONFIG_TEST_DEF"
                                      "dialog.config-test.def"
                                      :oh-well))))


(deftest level-collection
  (testing "from jvm properties"
    (testing "with bad prefix"
      (is (= {}
             (#'cfg/collect-prop-levels
              {"dialog.level." "warn"}))))
    (testing "with bad levels"
      (with-err-capture
        (is (= {}
               (#'cfg/collect-prop-levels
                {"dialog.level.abc" "bad"
                 "dialog.level.def" "oops"})))
        (is (str/includes? (str *err*)
                           "dialog.level.abc specifies invalid level bad"))))
    (testing "good"
      (is (= {"foo.bar" :info
              "foo.abc" :warn}
             (#'cfg/collect-prop-levels
              {"dialog.level.foo.bar" "INFO"
               "dialog.level.foo.abc" "warn"})))))
  (testing "from env vars"
    (testing "with bad prefix"
      (is (= {}
             (#'cfg/collect-env-levels
              {"DIALOG_LEVEL_" "warn"}))))
    (testing "with bad levels"
      (with-err-capture
        (is (= {}
               (#'cfg/collect-env-levels
                {"DIALOG_LEVEL_ABC" "BAD"
                 "DIALOG_LEVEL_DEF" "oops"})))
        (is (str/includes? (str *err*)
                           "DIALOG_LEVEL_ABC specifies invalid level BAD"))))
    (testing "good"
      (is (= {"foo.bar" :info
              "foo.abc" :warn}
             (#'cfg/collect-env-levels
              {"DIALOG_LEVEL_FOO_BAR" "INFO"
               "DIALOG_LEVEL_FOO_ABC" "warn"}))))))


(deftest function-resolution
  (testing "with bad input"
    (with-err-capture
      (is (nil? (#'cfg/resolve-fn "test" 123)))
      (is (str/includes? (str *err*) "test function 123 is not a known type"))))
  (testing "with nil"
    (is (nil? (#'cfg/resolve-fn "test" nil))))
  (testing "with function"
    (is (identical? inc (#'cfg/resolve-fn "test" inc))))
  (testing "with var"
    (is (identical? #'str (#'cfg/resolve-fn "test" #'str))))
  (testing "with symbol"
    (testing "for missing namespace"
      (with-err-capture
        (is (nil? (#'cfg/resolve-fn "test" 'dialog.missing/foo)))
        (is (str/includes? (str *err*) "test function dialog.missing/foo could not be resolved"))))
    (testing "not found in ns"
      (with-err-capture
        (is (nil? (#'cfg/resolve-fn "test" 'dialog.config/foo)))
        (is (str/includes? (str *err*) "test function dialog.config/foo was not found"))))
    (testing "for fn"
      (is (identical? #'cfg/resolve-fn (#'cfg/resolve-fn "test" 'dialog.config/resolve-fn))))))


(deftest middleware-resolution
  (testing "without middleware"
    (is (= {} (#'cfg/resolve-middleware {})))
    (is (= {:middleware []}
           (#'cfg/resolve-middleware
            {:middleware []}))))
  (testing "with bad middleware"
    (with-err-capture
      (is (= {:middleware []}
             (#'cfg/resolve-middleware
              {:middleware [123 "abc"]})))))
  (testing "with good middleware"
    (is (= {:middleware [inc #'str]}
           (#'cfg/resolve-middleware
            {:middleware [inc 'clojure.core/str]})))))


(deftest init-function
  (testing "resolution"
    (testing "without init"
      (is (= {} (#'cfg/resolve-init {}))))
    (testing "with bad init"
      (with-err-capture
        (is (= {} (#'cfg/resolve-init {:init "bob"})))))
    (testing "with good init"
      (is (= {:init #'identity}
             (#'cfg/resolve-init {:init 'clojure.core/identity})))))
  (testing "application"
    (testing "without init"
      (is (= {:level :info}
             (#'cfg/apply-init {:level :info}))))
    (testing "with update"
      (let [init #(assoc % :level :warn)]
        (is (= {:level :warn
                :init init}
               (#'cfg/apply-init
                {:level :info
                 :init init})))))
    (testing "returning nil"
      (let [init (constantly nil)]
        (is (= {:level :info
                :init init}
               (#'cfg/apply-init
                {:level :info
                 :init init}))
            "should not change config")))
    (testing "with error"
      (let [init (fn [_] (throw (RuntimeException. "BOOM")))]
        (with-err-capture
          (is (= {:level :info
                  :init init}
                 (#'cfg/apply-init
                  {:level :info
                   :init init}))
              "should not change config")
          (is (str/includes? (str *err*) "error applying init function")))))))


(deftest output-initialization
  (let [formatter (fn [])
        writer (fn [])]
    (testing "with keyword shorthand"
      (with-redefs [cfg/output-formatter (fn [_] formatter)
                    cfg/output-writer (fn [_] writer)]
        (is (= {:console {:type :print
                          :format :simple
                          :formatter formatter
                          :writer writer}}
               (#'cfg/initialize-outputs
                {:console :print}))
            "should expand output with default formatter")))
    (testing "with nil value"
      (is (= {} (#'cfg/initialize-outputs {:foo nil}))
          "should omit output"))
    (testing "with bad config"
      (with-err-capture
        (is (= {}
               (#'cfg/initialize-outputs
                {:non-map "xyz"
                 :bad-type {:type :foo}
                 :bad-format {:type :print
                              :format :bar}})))
        (is (str/includes? (str *err*) "output :non-map has unknown configuration"))
        (is (str/includes? (str *err*) "output :bad-type has invalid type"))
        (is (str/includes? (str *err*) "output :bad-format has invalid format"))))
    (testing "with init error"
      (with-redefs [cfg/output-formatter (fn [_] formatter)
                    cfg/output-writer (fn [_] (throw (RuntimeException. "BOOM")))]
        (with-err-capture
          (is (= {}
                 (#'cfg/initialize-outputs
                  {:boom {:type :print}})))
          (is (str/includes? (str *err*) "output :boom could not be initialized: java.lang.RuntimeException BOOM")))))
    (testing "success"
      (with-redefs [cfg/output-formatter (fn [_] formatter)
                    cfg/output-writer (fn [_] writer)]
        (is (= {:console {:type :print
                          :format :simple
                          :formatter formatter
                          :writer writer}}
               (#'cfg/initialize-outputs
                {:console {:type :print}})))))))


(deftest config-loading
  (testing "default config"
    (is (map? (cfg/load-config))))
  (testing "with error"
    (with-redefs [aero.core/read-config (fn [_resource _profile]
                                          (throw (RuntimeException. "BOOM")))]
      (with-err-capture
        (is (map? (cfg/load-config)))
        (is (str/includes? (str *err*) "failed to read dialog config file: BOOM"))))))
