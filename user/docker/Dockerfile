FROM openjdk:8u151-jdk-alpine


COPY ids-service-user.jar /opt/app.jar
COPY run.sh /opt/
COPY init.sh /opt
RUN chmod +x /opt/run.sh /opt/init.sh
WORKDIR /opt
RUN mkdir -p /opt/ids/config
COPY bootstrap.yml /opt/ids/config/
ADD ./server.cer $JAVA_HOME/jre/lib/security/server.cer
RUN  keytool -import -alias server -keystore $JAVA_HOME/jre/lib/security/cacerts -file $JAVA_HOME/jre/lib/security/server.cer -storepass changeit -noprompt
ENTRYPOINT /opt/run.sh

