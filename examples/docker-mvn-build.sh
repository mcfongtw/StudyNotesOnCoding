#!/bin/bash

source $SDKMAN_DIR/bin/sdkman-init.sh &&  mvn -Dmaven.repo.local=/vagrant/repository clean package -DskipTests
