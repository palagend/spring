FROM founder/openjdk:8-jre-slim

COPY ids-user-federation.jar /opt/app.jar
COPY root /
RUN chmod +x /usr/bin/run.sh

CMD [ "/usr/bin/run.sh" ]
