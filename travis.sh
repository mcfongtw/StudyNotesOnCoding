#!/usr/bin/env bash

set -o pipefail
set -e  # exit immediately on error
set -x  # display all commands

# Limit the amount of memory maven can use to avoid hitting the 3GB build limit in travis
export MAVEN_OPTS="-Xmx1024m"

cd examples/root/
mvn clean verify