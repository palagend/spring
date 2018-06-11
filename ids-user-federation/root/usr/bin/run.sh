#!/usr/bin/env sh

java -jar /opt/app.jar --spring.config.location=${SPRING_CONFIG_LOCATION:=/etc/ids/conf.d/}
