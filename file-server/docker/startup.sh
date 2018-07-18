echo ------------------Environment Variables-------------
echo SERVER_PORT
echo SERVER_ROOTPATH
echo SPRING_LOGSTASH_DESTINATION
echo EUREKA_CLIENT_SERVICEURL_DEFAULTZONE
echo -----------------------------------------------------
if [ -z "${IDS_SET_EXTERNAL_CONFIG+x}" ]; then
  echo "使用内部配置启动file-server"
  java -jar /opt/app.jar 
else
  echo "使用外部配置启动file-server"
  java -jar /opt/app.jar --spring.config.location=/opt/ids/config/
fi
