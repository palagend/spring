FROM openjdk:8-alpine3.7

COPY root /
RUN chmod +x /usr/bin/*.sh
EXPOSE 8080

COPY ids-file-server.jar /opt/app.jar
CMD [ "/usr/bin/run.sh" ]
