echo -------------Environment variables--------------
echo SERVER_PORT
echo IDS_KEYCLOAK_ADMIN_USERNAME
echo IDS_KEYCLOAK_ADMIN_PASSWORD
echo IDS_KEYCLOAK_SERVERURL
echo IDS_KEYCLOAK_COMPANY
echo SPRING_LOGSTASH_DESTINATION
echo SPRING_DATASOURCE_URL
echo SPRING_DATASOURCE_USERNAME
echo SPRING_DATASOURCE_PASSWORD
echo EUREKA_CLIENT_SERVICEURL_DEFAULTZONE
echo IDS_DOCKER_HOST
echo IDS_DOCKER_PORT
echo REDIS_HOST
echo REDIS_PORT
echo REDIS_CHECK
echo ------------------------------------------------
if [ -z "${IDS_SET_EXTERNAL_CONFIG+x}" ]; then
  echo "使用内部配置启动 ids-user-federation"
  java -jar /opt/app.jar 
else
  echo "使用外部配置启动 ids-user-federation"
  java -jar /opt/app.jar --spring.config.location=/opt/ids/config/
fi
