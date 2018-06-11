#!/usr/bin/env sh
echo ------------------Environment Variables-------------
echo "SERVER_PORT=${SERVER_PORT} (default is 8080)"
echo "SERVER_ROOT_PATH=${SERVER_ROOT_PATH} (default is http://gateway.service-governance/ids-file-server/)"
echo "LOGSTASH_DESTINATION=${LOGSTASH_DESTINATION} (default is elk.service-governance:5044)"
echo "EUREKA_REGISTRY_URL=${EUREKA_REGISTRY_URL} (default is http://registry.service-governance/eureka/)"
echo "SPRING_CONFIG_LOCATION=${SPRING_CONFIG_LOCATION} (default is ${SPRING_CONFIG_LOCATION:=/etc/ids/conf.d/})"
echo -----------------------------------------------------
[ ${SPRING_CONFIG_LOCATION:0-1} = '/' ] || SPRING_CONFIG_LOCATION=${SPRING_CONFIG_LOCATION}'/'
java -jar /opt/app.jar --spring.config.location=${SPRING_CONFIG_LOCATION}
