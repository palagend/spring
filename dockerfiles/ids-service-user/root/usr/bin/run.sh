#!/usr/bin/env sh
cd /app
wait-for-it.sh db:3306 -s -t 180 -- java -jar app.jar
# --spring.config.location=${SPRING_CONFIG_LOCATION:=/etc/ids/conf.d/}
