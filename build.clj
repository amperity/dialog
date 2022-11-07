;;
;; Build instructions
;;
(ns build
  (:require
    [clojure.tools.build.api :as b]))


;; if you want a version of MAJOR.MINOR.COMMITS:
(def class-dir "target/classes")
(def basis (b/create-basis {:project "deps.edn"}))
(def src-dirs ["src/java"])


(defn clean
  [_]
  (b/delete {:path "target"}))


(defn compile-java
  [opts]
  (let [opts (or opts {:src-dirs src-dirs
                       :class-dir class-dir
                       :basis basis})]
    (b/javac opts)
    (b/write-pom {:class-dir class-dir
                  :lib 'com.amperity/dialog
                  :version "1.0.1"
                  :basis basis
                  :src-dirs src-dirs})
    opts))
