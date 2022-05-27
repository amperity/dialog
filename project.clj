(defproject com.amperity/dialog "1.0.0-SNAPSHOT"
  :description "Logging library for integration with Ken and various outputs."
  :url "https://github.com/amperity/dialog"
  :license {:name "MIT License"
            :url "https://mit-license.org/"}

  :deploy-branches ["main"]
  :pedantic? :abort

  :plugins
  [[lein-cloverage "1.2.2"]]

  :dependencies
  [[org.clojure/clojure "1.11.1"]
   [org.clojure/data.json "2.4.0"]
   [org.slf4j/slf4j-api "1.7.36"]
   [org.slf4j/jul-to-slf4j "1.7.36"]
   [org.slf4j/jcl-over-slf4j "1.7.36"]
   [io.aviso/pretty "1.1.1"]
   [aero "1.1.6"]]

  :source-paths ["src/clojure"]
  :java-source-paths ["src/java"]
  :javac-options ["-Xlint:unchecked"]

  :hiera
  {:cluster-depth 2
   :vertical false
   :show-external false}

  :cloverage
  {:ns-exclude-regex #{#"dialog\.util"}}

  :profiles
  {:dev
   {:dependencies [[org.clojure/tools.logging "1.2.4"]]}

   :repl
   {:source-paths ["dev"]
    :repl-options {:init-ns dialog.repl}
    :dependencies [[org.clojure/tools.namespace "1.3.0"]]
    :jvm-opts ["-XX:-OmitStackTraceInFastThrow"
               "-Ddialog.profile=repl"]}})
