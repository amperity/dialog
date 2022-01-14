#!/usr/bin/env bash

# Check that instances of the `dialog.logger.DialogLogger` class serialize
# correctly.

set -eo pipefail

cd "$(dirname "${BASH_SOURCE[0]}")/.."

readonly TEST_DIR="target/test"
readonly TEST_OBJ="$TEST_DIR/logger.object"
readonly TEST_LOG="$TEST_DIR/out.log"

rm -rf $TEST_DIR
mkdir -p $TEST_DIR

echo "Writing dialog config..."
mkdir -p target/classes
cat > target/classes/dialog.edn <<EOF
{:outputs {:file {:type :file, :path "$TEST_LOG"}}}
EOF

echo "Building classpath..."
readonly CLASSPATH="$(lein classpath 2> /dev/null)"

echo "Compiling classes..."
java -cp "$CLASSPATH" clojure.main - <<EOF
(binding [*compile-path* "target/classes"]
  (compile 'dialog.logger))
EOF

echo "Serializing logger..."
java -cp "$CLASSPATH" clojure.main - <<EOF
(require '[clojure.java.io :as io])

(def logger (org.slf4j.LoggerFactory/getLogger "foo.bar.baz"))

(.info logger "Hello, test")

(with-open [out (java.io.ObjectOutputStream. (io/output-stream (io/file "$TEST_OBJ")))]
  (.writeObject out logger))

(System/exit 0)
EOF

echo "Checking files..."
ls -l $TEST_DIR

if [[ ! -f "$TEST_LOG" ]]; then
    echo "Log output file $TEST_LOG not found!" >&2
    exit 2
fi

if [[ ! -f "$TEST_OBJ" ]]; then
    echo "Serialized logger object $TEST_OBJ not found!" >&2
    exit 2
fi

readonly FIRST_LOG="$(cat "$TEST_LOG")"
rm "$TEST_LOG"

echo "Deserializing logger..."
java -cp "$CLASSPATH" clojure.main - <<EOF
(require '[clojure.java.io :as io])

(with-open [in (java.io.ObjectInputStream. (io/input-stream (io/file "$TEST_OBJ")))]
  (def logger (.readObject in)))

(.info logger "Hello, test")

(System/exit 0)
EOF

if [[ ! -f "$TEST_LOG" ]]; then
    echo "Log output file $TEST_LOG not found!" >&2
    exit 2
fi

echo "Comparing log outputs..."
diff -u <(cut -d ' ' -f 2- <<<"$FIRST_LOG") <(cut -d ' ' -f 2- < "$TEST_LOG")
