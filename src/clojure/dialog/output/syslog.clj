(ns dialog.output.syslog
  "Log appender implementation for syslog via UDP."
  (:require
    [clojure.stacktrace :as stacktrace]
    [clojure.string :as str])
  (:import
    (java.io
      ByteArrayOutputStream
      IOException
      OutputStream)
    (java.net
      DatagramPacket
      DatagramSocket
      InetAddress
      Socket
      SocketException
      UnknownHostException)
    java.time.Instant))


;; ## Message Encoding

(def ^:private ^:const max-message-length
  "Maximum length a syslog message can be before being truncated.

  The loopback device in Linux has a MTU of 64K - per RFC 5426 §3.2 we
  must limit the message size to avoid packet fragmentation."
  ;; This _could_ go up to 64K - 20 (ip4 header) - 8 (udp header). Instead,
  ;; set a lower threshold to give ourselves lots of headroom - UTF-8 can mean
  ;; more than one byte per counted character, for example. Log messages
  ;; _really_ shouldn't be this big anyway.
  32768)


(def ^:private ^:const facility
  "Facility to use for syslog messages. This value encodes the `local4`
  facility after bit-shifting."
  0xA0)


(def ^:private ^:const priorities
  "Map of priority keywords to their numeric syslog codes."
  ;; NOTE: these don't map exactly as stated to normal "log levels" so we do
  ;; some slight remapping here. Original priority names in comments.
  {:fatal    0     ; Emergency: system is unusable
   :alert    1     ; Alert: action must be taken immediately
   :critical 2     ; Critical: critical conditions
   :error    3     ; Error: error conditions
   :warn     4     ; Warning: warning conditions
   :info     5     ; Notice: normal but significant condition
   :debug    6     ; Informational: informational messages
   :trace    7})   ; Debug: debug-level messages


(defmacro ^:private write-char
  "Write a single character to the output stream."
  [out  c]
  `(.write ~out (int ~c)))


(defmacro ^:private write-str
  "Write a string to the output stream."
  [out s]
  `(.write ~out (.getBytes (str ~s))))


(defn- write-field
  "Write a space followed by the value string's byte content to the given
  output stream.  Writes `-` if the value is nil."
  [^OutputStream out value]
  (write-char out \space)
  (if (some? value)
    (write-str out value)
    (write-char out \-)))


(defn- encode-payload
  "Construct a byte array encoding a syslog message as packet data."
  ^bytes
  [timestamp host app proc level message]
  (let [payload (ByteArrayOutputStream. 1024)]
    ;; RFC 5424 §6.2
    ;; <PRI>VERSION TIMESTAMP HOSTNAME APP-NAME PROCID MSGID STRUCTURED-DATA MSG
    (doto payload
      ;; PRI
      (write-char \<)
      (write-str
        (bit-or (get priorities level 5)
                facility))
      (write-char \>)
      ;; VERSION
      (write-char \1)
      ;; TIMESTAMP
      (write-field (or timestamp (str (Instant/now))))
      ;; HOSTNAME
      (write-field host)
      ;; APP-NAME
      (write-field app)
      ;; PROCID
      (write-field proc)
      ;; MSGID (not used)
      (write-field nil)
      ;; STRUCTURED-DATA (not used)
      (write-field nil)
      ;; MSG
      (write-field
        (let [message (str/trim-newline (or message ""))]
          (if (< max-message-length (count message))
            (subs message 0 (dec max-message-length))
            message)))
      (write-char \newline))
    (.toByteArray payload)))


;; ## Output Functions

(defn- connect!
  "Open a connection to the local syslog daemon. Returns a map containing an
  open socket and address/port information on success, or nil on failure."
  []
  (try
    {:socket (DatagramSocket.)
     :address (InetAddress/getLocalHost)
     :port 514}
    (catch UnknownHostException ex
      (binding [*out* *err*]
        (println "Unable to resolve localhost for syslog")
        (stacktrace/print-throwable ex)
        nil))
    (catch SocketException ex
      (binding [*out* *err*]
        (println "Error connecting to syslog UDP socket")
        (stacktrace/print-throwable ex)
        nil))
    (catch Exception ex
      (binding [*out* *err*]
        (println "Error initializing syslog connection")
        (stacktrace/print-cause-trace ex)
        nil))))


(defn- write-message
  "Write a log event to the syslog socket. Returns true if the message was
  successfully delivered, false if not."
  [conn data]
  (let [{:keys [socket address port]} conn
        {:keys [level timestamp_ hostname_ context output-fn]} data
        ;; TODO: un-timbre this
        payload (encode-payload
                  @timestamp_
                  @hostname_
                  (:sys context)
                  (:host context)
                  level
                  (output-fn data))
        packet (DatagramPacket.
                 payload
                 (count payload)
                 ^InetAddress address
                 (int port))]
    (try
      (.send ^DatagramSocket socket packet)
      true
      (catch IOException ex
        (binding [*out* *err*]
          (println "Error sending syslog packet")
          (stacktrace/print-throwable ex))
        false))))


(defn appender
  "Construct a new appender which will write to the local syslog."
  [opts]
  ;; TODO: options to control address and port
  (let [conn (connect!)]
    (assoc opts :fn (partial write-message conn))))
