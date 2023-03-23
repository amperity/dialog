(ns build
  "Build instructions for dialog."
  (:require
    [clojure.java.io :as io]
    [clojure.string :as str]
    [clojure.tools.build.api :as b]
    [deps-deploy.deps-deploy :as d]))


(def basis (b/create-basis {:project "deps.edn"}))
(def lib-name 'com.amperity/dialog)
(def base-version "2.0")

(def clojure-src-dir "src/clojure")
(def java-src-dir "src/java")
(def class-dir "target/classes")


(defn- lib-version
  "Construct the version of the library."
  [opts]
  (str base-version
       (if (:snapshot opts)
         "-SNAPSHOT"
         (str "." (b/git-count-revs nil)))))


(defn- jar-path
  "Construct the path to the jar artifact file."
  [opts]
  (format "target/%s-%s.jar" (name lib-name) (:version opts)))


(defn- check-java-version
  "Check the current Java version and exit with an error if it's not 1.8."
  []
  (let [java-version (System/getProperty "java.version")]
    (when-not (str/starts-with? java-version "1.8")
      (binding [*out* *err*]
        (println "Dialog should be compiled with Java 1.8 for maximum"
                 "compatibility; currently using:" java-version))
      (System/exit 1))))


;; ## Tasks

(defn clean
  "Remove compiled artifacts."
  [opts]
  (b/delete {:path "target"})
  opts)


(defn print-version
  "Print the current version of the library."
  [opts]
  (println (lib-version opts))
  opts)


(defn javac
  "Compile Java source files in the project."
  [opts]
  (b/javac
    {:src-dirs [java-src-dir]
     :class-dir class-dir
     :javac-opts ["-Xlint:unchecked"]
     :basis basis})
  opts)


(defn pom
  "Write out a pom.xml file for the project."
  [opts]
  (let [commit-sha (b/git-process {:git-args "rev-parse HEAD"})
        version (lib-version opts)]
    (b/write-pom
      {:basis basis
       :lib lib-name
       :version version
       :src-pom "doc/pom.xml"
       :src-dirs [clojure-src-dir]
       :class-dir class-dir
       :scm {:tag commit-sha}})
    (assoc opts
           :commit-sha commit-sha
           :version version
           :pom-file (b/pom-path
                       {:class-dir class-dir
                        :lib lib-name}))))


(defn jar
  "Build a JAR file for distribution."
  [opts]
  (let [opts (-> opts javac pom)
        jar-file (jar-path opts)]
    (b/copy-dir
      {:src-dirs [clojure-src-dir]
       :target-dir class-dir})
    (b/jar
      {:class-dir class-dir
       :jar-file jar-file})
    (assoc opts :jar-file jar-file)))


(defn install
  "Install a JAR into the local Maven repository."
  [opts]
  (let [opts (-> opts clean jar)]
    (b/install
      {:basis basis
       :lib lib-name
       :version (:version opts)
       :jar-file (:jar-file opts)
       :class-dir class-dir})
    opts))


(defn deploy
  "Deploy the JAR to Clojars."
  [opts]
  (check-java-version)
  (let [opts (-> opts clean jar)]
    (d/deploy
      {:installer :remote
       :sign-releases? true
       :pom-file (:pom-file opts)
       :artifact (:jar-file opts)})
    opts))
