(ns dialog.repl
  (:require
    [clojure.java.io :as io]
    [clojure.repl :refer :all]
    [clojure.stacktrace :refer [print-cause-trace]]
    [clojure.string :as str]
    [clojure.tools.logging :as ctl]
    [clojure.tools.namespace.repl :refer [refresh]]
    [criterium.core :as crit]
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


;; ## Level Benchmarking

(def words
  (delay
    (with-open [reader (io/reader "/usr/share/dict/words")]
      (into []
            (comp
              (filter #(< (count %) 8))
              (map str/lower-case))
            (line-seq reader)))))


(defn rand-level
  "Choose a random logger level."
  []
  (rand-nth [:trace :debug :info :warn :error :fatal :off]))


(defn make-tree
  "Build a tree of loggers representing some hypothetical code structure."
  [& {:keys [max-depth curr-depth branch-factor]
      :or {max-depth 4
           curr-depth 0
           branch-factor 4}
      :as opts}]
  (when (and (< curr-depth max-depth)
             (or (< curr-depth 2)
                 (< (rand) 0.50)))
    (let [branches (inc (rand-int branch-factor))]
      (into {}
            (map (fn [_]
                   [(rand-nth @words)
                    (make-tree (assoc opts :curr-depth (inc curr-depth)))]))
            (range branches)))))


(defn list-loggers
  "List the loggers present in a tree from `make-tree`."
  [tree & {:keys [prefixes?]}]
  (letfn [(walk
            [prefix [segment children]]
            (let [logger (str prefix (when prefix ".") segment)]
              (cons
                (when (or prefixes? (empty? children))
                  logger)
                (when (seq children)
                  (mapcat (partial walk logger)
                          (sort-by key children))))))]
    (->>
      (sort-by key tree)
      (mapcat (partial walk nil))
      (remove nil?))))


(defn make-levels
  [tree n]
  (->>
    (list-loggers tree :prefixes? true)
    (shuffle)
    (take n)
    (map (fn [logger] [logger (rand-level)]))
    (into {})))


(defn bench-matching
  []
  (let [tree (make-tree :max-depth 5
                        :branch-factor 5)
        levels (make-levels tree 15)
        loggers (vec (list-loggers tree))
        match-level @#'log/match-level]
    (crit/quick-bench
      (match-level levels (rand-nth loggers)))))
