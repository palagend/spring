#!/usr/bin/env sh

cd /app
wait-for-it.sh db:3306 -s -t 180 -- java -jar /app/app.jar
