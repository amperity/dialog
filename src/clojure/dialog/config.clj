(ns dialog.config
  "Functions for loading and managing the dialog configuration."
  (:require
    [aero.core :as aero]
    [clojure.java.io :as io]
    [clojure.string :as str]
    [dialog.format.json :as fmt.json]
    [dialog.format.pretty :as fmt.pretty]
    [dialog.format.simple :as fmt.simple]
    [dialog.output.file :as out.file]
    [dialog.output.print :as out.print]
    [dialog.output.syslog :as out.syslog])
  (:import
    dialog.logger.Level))


(defn- print-err
  "Print a message to stderr when something goes wrong during initialization."
  [message & args]
  (binding [*out* *err*]
    (apply printf (str "[dialog config error] " message \newline) args)
    (flush))
  nil)


(defn- some-setting
  "Check the named environment variable and JVM property for a runtime setting,
  or return the default if neither is set."
  [env-var jvm-prop default]
  (or (System/getenv env-var)
      (System/getProperty jvm-prop)
      default))


(defn- select-profile
  "Choose the dialog profile keyword by looking at the environment and system
  properties."
  []
  (keyword (some-setting "DIALOG_PROFILE"
                         "dialog.profile"
                         :default)))


(defn- collect-root-level
  "Choose the dialog root logger level keyword by looking at the environment and
  system properties."
  []
  (some->
    (some-setting "DIALOG_LEVEL"
                  "dialog.level"
                  nil)
    (str/lower-case)
    (keyword)))


(defn- collect-prop-levels
  "Look in the JVM system properties for logger level configs. Returns a map of
  collected level settings."
  ([]
   (collect-prop-levels (System/getProperties)))
  ([properties]
   (let [prefix "dialog.level."]
     (into {}
           (comp
             (filter #(str/starts-with? (key %) prefix))
             (remove #(= prefix (key %)))
             (keep (fn [[prop level-str]]
                     (let [level (keyword (str/lower-case level-str))]
                       (if (Level/isValid level)
                         [(subs prop (count prefix)) level]
                         (print-err "JVM property %s specifies invalid level %s"
                                    prop
                                    level-str))))))
           properties))))


(defn- collect-env-levels
  "Look in the process environment for logger level configs. Returns a map of
  collected level settings."
  ([]
   (collect-env-levels (System/getenv)))
  ([env]
   (let [prefix "DIALOG_LEVEL_"]
     (into {}
           (comp
             (filter #(str/starts-with? (key %) prefix))
             (remove #(= prefix (key %)))
             (keep (fn [[var-name level-str]]
                     (let [level (keyword (str/lower-case level-str))]
                       (if (Level/isValid level)
                         [(-> var-name
                              (subs (count prefix))
                              (str/lower-case)
                              (str/replace "_" "."))
                          level]
                         (print-err "Environment variable %s specifies invalid level %s"
                                    var-name
                                    level-str))))))
           env))))


(defn- resolvable?
  "True if the provided value can be resolved to a function by being a
  reference or one of a set of known keywords."
  [x constants]
  (or (fn? x)
      (var? x)
      (symbol? x)
      (contains? constants x)))


(defn- resolve-fn
  "Safely resolve the given value to a function. Returns the function, or prints
  an error and returns nil."
  [kind x]
  (try
    (cond
      (or (nil? x)
          (var? x)
          (fn? x))
      x

      (symbol? x)
      (or (requiring-resolve x)
          (print-err "%s function %s was not found"
                     kind x))

      :else
      (print-err "%s function %s is not a known type: %s"
                 kind x (class x)))
    (catch Exception ex
      (print-err "%s function %s could not be resolved: %s"
                 kind x (ex-message ex)))))


(defn- resolve-middleware
  "Resolve any middleware symbols in the config to functions."
  [config]
  (if-let [middleware (seq (:middleware config))]
    (assoc config
           :middleware
           (into []
                 (keep (partial resolve-fn "middleware"))
                 middleware))
    config))


(defn- resolve-init
  "Resolve the initialization function, if set."
  [config]
  (if-let [init-sym (:init config)]
    (if-let [init-fn (resolve-fn "init" init-sym)]
      (assoc config :init init-fn)
      (dissoc config :init))
    config))


(defn- apply-init
  "Apply the initialization function to the config if present, producing an
  updated config. Returning `nil` is interpreted as no changes."
  [config]
  (if-let [init-fn (:init config)]
    (try
      (or (init-fn config)
          config)
      (catch Exception ex
        (print-err "error applying init function %s: %s"
                   (.getName (class init-fn))
                   (ex-message ex))
        config))
    config))


(defn- output-formatter
  "Construct an output formatting function."
  [output]
  (let [fmt (:format output)]
    (if (keyword? fmt)
      (case fmt
        :message :message
        :simple  (fmt.simple/formatter output)
        :pretty  (fmt.pretty/formatter output)
        :json    (fmt.json/formatter output))
      (let [constructor (resolve-fn "formatter constructor" fmt)]
        (constructor output)))))


(defn- output-writer
  "Construct an output writing function."
  [output]
  (let [typ (:type output)]
    (if (keyword? typ)
      (case (:type output)
        :null   nil
        :file   (out.file/writer output)
        :print  (out.print/writer output)
        :syslog (out.syslog/writer output))
      (let [constructor (resolve-fn "output constructor" typ)]
        (constructor output)))))


(defn- initialize-output
  "Expand an output configuration map into its full form and apply
  initialization."
  [[id output]]
  ;; Expand keyword into basic map with default format.
  (let [output (if (keyword? output)
                 {:type output
                  :format :simple}
                 output)]
    (cond
      ;; Gracefully handle nil to mean omission.
      (nil? output)
      nil

      ;; Check that config has expected type.
      (not (map? output))
      (print-err "output %s has unknown configuration; expected a map: %s"
                 id
                 (pr-str output))

      ;; Check that output type is understood.
      (not (resolvable? (:type output) #{:null :file :print :syslog}))
      (print-err "output %s has invalid type: %s"
                 id
                 (:type output))

      ;; Check that format type is understood.
      (and (contains? output :format)
           (not (resolvable? (:format output)
                             #{:message :simple :pretty :json})))
      (print-err "output %s has invalid format: %s"
                 id
                 (:format output))

      ;; Initialize format and output functions.
      :else
      (try
        (let [output (merge {:format :simple} output)]
          [id (-> output
                  (resolve-middleware)
                  (assoc :formatter (output-formatter output)
                         :writer (output-writer output)))])
        (catch Exception ex
          (print-err "output %s could not be initialized: %s %s"
                     id
                     (.getName (class ex))
                     (ex-message ex)))))))


(defn- initialize-outputs
  "Process a map of outputs and return an updated version with each output
  value fully expanded and initialized."
  [outputs]
  (into {}
        (keep initialize-output)
        outputs))


(defn- read-config
  "Read the raw configuration from an EDN resource. Returns nil if the resource
  is not found or can't be read."
  [profile]
  (when-let [config-edn (or (io/resource "dialog.edn")
                            (io/resource "dialog/config.edn"))]
    (try
      (aero/read-config config-edn {:profile profile})
      (catch Exception ex
        (print-err "failed to read dialog config file: %s"
                   (ex-message ex))))))


(defn load-config
  "Read logging configuration from an EDN resource (if available) and set any
  runtime overrides from JVM properties and the process environment."
  []
  (let [profile (select-profile)
        root-level (collect-root-level)
        base-config (merge {:level :info
                            :outputs {:console :print}}
                           (read-config profile))]
    (->
      base-config
      (update :levels merge
              (collect-prop-levels)
              (collect-env-levels))
      (cond->
        (Level/isValid root-level)
        (assoc :level root-level))
      (resolve-init)
      (apply-init)
      (resolve-middleware)
      (update :outputs initialize-outputs))))
