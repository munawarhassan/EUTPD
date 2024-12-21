#!/bin/bash

set -euo pipefail

dir="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
set -x


source ${dir}/.env

function qpushd() {
  pushd "$1" &> /dev/null
}

function qpopd() {
  popd &> /dev/null
}


qpushd ../../
mvn clean install -Denv=docker -P skipTests
qpopd
