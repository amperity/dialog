(ns dialog.repl
  (:require
    [clojure.repl :refer :all]
    [clojure.stacktrace :refer [print-cause-trace]]
    [clojure.string :as str]
    [clojure.tools.logging :as ctl]
    [clojure.tools.namespace.repl :refer [refresh]]
    [dialog.config :as config]
    [dialog.logger :as log])
  (:import
    (dialog.logger
      DialogFactory
      DialogLogger
      Level)))


(defn init!
  "Initialize the logger state."
  []
  (log/initialize!))


(defn reset
  "Reset the REPL state, reloading code and ensuring the logger is
  re-initialized."
  []
  (refresh :after 'dialog.repl/init!))
