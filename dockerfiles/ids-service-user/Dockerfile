FROM founder/openjdk:8-jre-slim

ADD ./server.cer $JAVA_HOME/jre/lib/security/server.cer
RUN  keytool -import -alias server -keystore $JAVA_HOME/jre/lib/security/cacerts -file $JAVA_HOME/jre/lib/security/server.cer -storepass changeit -noprompt

COPY ids-service-user.jar /app/app.jar
COPY root /
RUN chmod +x /usr/bin/run.sh

CMD [ "/usr/bin/run.sh" ]

