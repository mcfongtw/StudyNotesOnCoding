#!/usr/bin/env bash

set -o pipefail
set -e  # exit immediately on error
set -x  # display all commands

# Limit the amount of memory maven can use to avoid hitting the 3GB build limit in travis
export MAVEN_OPTS="-Xmx1024m"

profile=$1

cd examples/root/
if [[ -z "$profile" ]]; then
    mvn clean verify
else
    mvn clean verify -P $profile
fi
