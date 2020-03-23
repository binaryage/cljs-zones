#!/usr/bin/env bash

set -e -o pipefail

pushd() {
  command pushd "$@" >/dev/null
}

popd() {
  command popd >/dev/null
}

pushd .

cd "$(dirname "${BASH_SOURCE[0]}")/.."

ROOT=$(pwd)
PROJECT_VERSION_FILE="src/lib/zones/core.clj"
PROJECT_FILE="project.clj"

popd
