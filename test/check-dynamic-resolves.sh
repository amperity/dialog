#!/usr/bin/env bash

# Check that the runtime var-resolution for hot-reloading support in
# DialogLogger is commented it out.

set -eo pipefail

cd "$(dirname "${BASH_SOURCE[0]}")/.."

readonly MATCHES=$(grep -RE '^ *IFn.*"requiring-resolve"' src/java/dialog)

if [[ -n $MATCHES ]]; then
    echo -e "Found uncommented reference to 'requiring-resolve' in logger classes:\n$MATCHES" >&2
    exit 1
fi
