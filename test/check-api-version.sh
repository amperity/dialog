#!/usr/bin/env bash

# Check that the project's declared SLF4J version matches the constant in the
# StaticLoggerBinder class.

set -eo pipefail

cd "$(dirname "${BASH_SOURCE[0]}")/.."

readonly PROJECT_VERSION="$(grep org.slf4j/slf4j-api project.clj | cut -d \" -f 2)"
readonly BINDER_VERSION="$(grep REQUESTED_API_VERSION src/java/org/slf4j/impl/StaticLoggerBinder.java | cut -d \" -f 2)"

if [[ $PROJECT_VERSION != $BINDER_VERSION ]]; then
    echo "Project depends on slf4j-api version $PROJECT_VERSION but StaticLoggerBinder declares version $BINDER_VERSION" >&2
    exit 1
fi
