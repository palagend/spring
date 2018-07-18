#!/usr/bin/env sh

wait-for-it.sh db:3306 -s -t 180 -- java -jar /opt/app.jar --spring.config.location=${SPRING_CONFIG_LOCATION:=/etc/ids/conf.d/}
