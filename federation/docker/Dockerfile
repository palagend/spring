FROM openjdk:8u151-jdk-alpine

COPY ids-user-federation.jar /opt/app.jar
COPY startup.sh /opt/
RUN chmod +x /opt/startup.sh
RUN mkdir -p /opt/ids/config
COPY bootstrap.yml /opt/ids/config/
WORKDIR /opt
EXPOSE 8080

ENTRYPOINT /opt/startup.sh
