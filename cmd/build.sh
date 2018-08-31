#!/usr/bin/env bash
set -e
test ${WORKSPACE:?WORKSPACE should been set!}
# 构建ids-api-server
if [[ ${flag_build_apiserver} != 'n' && ${flag_apiserver} != 'n' ]]; then
  cd ${WORKSPACE}/ids-api-server && mvn clean install -DskipTests -Pjenkins
fi
# 构建configserver
if [[ ${flag_build_configserver} != 'n' && ${flag_configserver} != 'n' ]]; then
  cd ${WORKSPACE}/configserver && mvn clean install -DskipTests -Pjenkins
fi