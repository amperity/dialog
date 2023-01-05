(ns build
  "Build instructions for dialog."
  (:require
    [clojure.java.io :as io]
    [clojure.tools.build.api :as b]
    [deps-deploy.deps-deploy :as d]))


(def lib-name 'com.amperity/dialog)
(def version (str "1.1." (b/git-count-revs nil)))

(def clojure-src-dir "src/clojure")
(def java-src-dir "src/java")
(def class-dir "target/classes")
(def jar-file (format "target/%s-%s.jar" (name lib-name) version))

(def basis (b/create-basis {:project "deps.edn"}))


(defn clean
  "Remove compiled artifacts."
  [_]
  (b/delete {:path "target"}))


(defn javac
  "Compile Java source files in the project."
  [_]
  (b/javac
    {:src-dirs [java-src-dir]
     :class-dir class-dir
     :javac-opts ["-Xlint:unchecked"]
     :basis basis}))


(defn pom
  "Write out a pom.xml file for the project."
  [_]
  (b/write-pom
    {:src-dirs [clojure-src-dir #_java-src-dir]
     :class-dir class-dir
     :version version
     :lib lib-name
     :basis basis}))


(defn jar
  "Build a JAR file for distribution."
  [_]
  (javac nil)
  (pom nil)
  (b/copy-dir
    {:src-dirs [clojure-src-dir]
     :target-dir class-dir})
  (b/jar
    {:class-dir class-dir
     :jar-file jar-file}))


(defn install
  "Install a JAR into the local Maven repository."
  [_]
  (when-not (.exists (io/file jar-file))
    (jar nil))
  (b/install
    {:basis basis
     :lib lib-name
     :version version
     :jar-file jar-file
     :class-dir class-dir}))


(defn deploy
  "Deploy the JAR to Clojars."
  [opts]
  (when-not (.exists (io/file jar-file))
    (jar nil))
  (d/deploy
    (assoc opts
           :installer :remote
           :sign-releases? true
           :artifact jar-file)))
