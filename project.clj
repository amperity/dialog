(defproject com.amperity/dialog "0.1.0-SNAPSHOT"
  :description "Logging library for integration with Ken and various outputs."
  :url "https://github.com/amperity/dialog"
  :license {:name "MIT License"
            :url "https://mit-license.org/"}

  :plugins
  [[lein-cloverage "1.2.2"]]

  :dependencies
  [[org.clojure/clojure "1.10.3"]
   [org.clojure/data.json "2.4.0"]
   [org.clojure/tools.logging "1.1.0"]
   [com.amperity/ken "1.0.0"]]

  :profiles
  {:repl
   {:source-paths ["dev"]
    :repl-options {:init-ns dialog.repl}
    :dependencies [[org.clojure/tools.namespace "1.1.0"]]
    :jvm-opts ["-XX:-OmitStackTraceInFastThrow"]}})
