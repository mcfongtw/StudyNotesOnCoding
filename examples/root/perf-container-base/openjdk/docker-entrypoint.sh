#!/bin/bash

function on_error() {
  local frame=0

  echo "*******************************"
  echo "*** ERROR DETECTED ***"
  echo "$0"
  echo "----------------------"
  echo "Stack Trace Analysis"
  echo "----------------------"
  while caller $frame; do
	  ((frame++));
  done
  echo "$*"
  echo "*******************************"

  exit -1
}

# -E  If set, the ERR trap is inherited by shell functions.
set -E

trap 'on_error' ERR

bash -c "uname -a"
bash -c "stap --version"
bash -c "java -version"
bash -c "javac -version"
bash -c "javac -version"
bash -c "perf -h"

exec "$@"
