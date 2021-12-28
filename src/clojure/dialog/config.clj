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
    [dialog.output.syslog :as out.syslog]))


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


(defn- collect-levels
  "Look in the JVM system properties and process environment for logger level
  configs. Returns a map of collected level settings."
  ([]
   (collect-levels (System/getenv) (System/getProperties)))
  ([env properties]
   (merge
     (let [prefix "dialog.level."]
       (into {}
             (comp
               (filter #(str/starts-with? (key %) prefix))
               (map (juxt #(subs (key %) (count prefix))
                          (comp keyword str/lower-case val))))
             properties))
     (let [prefix "DIALOG_LEVEL_"]
       (into {}
             (comp
               (filter #(str/starts-with? (key %) prefix))
               (map (juxt #(-> (key %)
                               (subs (count prefix))
                               (str/lower-case)
                               (str/replace "_" "."))
                          (comp keyword str/lower-case val))))
             env)))))


(defn- resolve-fn
  "Safely resolve the given value to a function. Returns the function, or prints
  an error and returns nil."
  [kind x]
  (try
    (cond
      (fn? x)
      x

      (var? x)
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
  (if-let [raw-mw (seq (:middleware config))]
    (let [middleware (into []
                           (keep (partial resolve-fn "middleware"))
                           raw-mw)]
      (if (seq middleware)
        (assoc config :middleware middleware)
        (dissoc config :middleware)))
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
      (not (contains? #{:null :file :print :syslog}
                      (:type output)))
      (print-err "output %s has invalid type: %s"
                 id
                 (:type output))

      ;; Check that format type is understood.
      (not (contains? #{:message :simple :pretty :json}
                      (:format output)))
      (print-err "output %s has invalid format: %s"
                 id
                 (:format output))

      ;; Initialize format and output functions.
      :else
      (try
        (let [formatter (case (:format output)
                          :message :message
                          :simple  (fmt.simple/formatter output)
                          :pretty  (fmt.pretty/formatter output)
                          :json    (fmt.json/formatter output))
              writer (case (:type output)
                       :null   nil
                       :file   (out.file/writer output)
                       :print  (out.print/writer output)
                       :syslog (out.syslog/writer output))]
          [id (assoc output
                     :formatter formatter
                     :writer writer)])
        (catch Exception ex
          (print-err "output %s failed to initialize: %s %s"
                     (.getName (class ex))
                     (ex-message ex)))))))


(defn- initialize-outputs
  "Process a map of outputs and return an updated version with each output
  value fully expanded and initialized."
  [outputs]
  (into {}
        (keep initialize-output)
        outputs))


(defn load-config
  "Read logging configuration from an EDN resource (if available) and set any
  runtime overrides from JVM properties an the process environment."
  []
  (let [profile-key (keyword (some-setting "DIALOG_PROFILE"
                                           "dialog.profile"
                                           :default))
        base-config (if-let [config-edn (io/resource "dialog/config.edn")]
                      (try
                        (aero/read-config config-edn {:profile profile-key})
                        (catch Exception ex
                          (print-err "failed to read dialog config file: %s"
                                     (ex-message ex))
                          {:level :info}))
                      {:level :info})
        root-level (some-setting "DIALOG_LEVEL"
                                 "dialog.level"
                                 nil)]
    (->
      base-config
      (update :levels merge (collect-levels))
      (cond->
        root-level
        (assoc :level (keyword root-level))

        (nil? (:outputs base-config))
        (assoc :outputs {:console :print}))
      (resolve-init)
      (apply-init)
      (resolve-middleware)
      (update :outputs initialize-outputs))))
