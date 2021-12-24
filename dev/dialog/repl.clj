(ns dialog.repl
  (:require
    [clojure.repl :refer :all]
    [clojure.stacktrace :refer [print-cause-trace]]
    [clojure.string :as str]
    [clojure.tools.logging :as ctl]
    [clojure.tools.namespace.repl :refer [refresh]]
    [dialog.config :as config]
    [dialog.core :as log]))
