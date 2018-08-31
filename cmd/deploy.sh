#!/usr/bin/env bash
set -e
test ${WORKSPACE:?WORKSPACE MUST be set!}
export CONFIG_SERVER_URI=http://config.athena:8888
test ${CONFIG_SERVER_URI:?CONFIG_SERVER_URI MUST be set!}
test ${KEYCLOAK_USER:?KEYCLOAK_USER MUST be set!}
test ${KEYCLOAK_PASSWORD:?KEYCLOAK_PASSWORD MUST be set!}
test ${RABBITMQ_USERNAME:?RABBITMQ_USERNAME MUST be set!}
test ${RABBITMQ_PASSWORD:?RABBITMQ_PASSWORD MUST be set!}
test ${AUTH_SERVER_URL:?AUTH_SERVER_URL MUST be set!}
test ${KEYCLOAK_REALM:?KEYCLOAK_REALM MUST be set!}
# 部署configserver
if [[ flag_deploy_configserver != 'n' && ${flag_configserver} != 'n' ]]; then
  rancher-compose -p athena -f ${WORKSPACE}/configserver/compose/docker-compose.yml -r ${WORKSPACE}/configserver/compose/rancher-compose.yml up -d -p -c --force-recreate
fi
# 部署ids-api-server
if [[ ${flag_deploy_apiserver} != 'n'  && ${flag_apiserver} != 'n' ]]; then
  rancher-compose -p avatar -f ${WORKSPACE}/ids-api-server/compose/docker-compose.yml -r ${WORKSPACE}/ids-api-server/compose/rancher-compose.yml up -d -p -c --force-recreate
fi