FROM openjdk:8-alpine3.7

COPY ids-user-federation.jar /opt/app.jar
COPY root /
RUN chmod +x /usr/bin/run.sh

CMD [ "/usr/bin/run.sh" ]
