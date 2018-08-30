FROM founder/openjdk:8-jre-alpine
WORKDIR /app
EXPOSE 8880
ENTRYPOINT ["java","-jar","/app/app.jar"]

ARG JAR_FILE
ADD target/${JAR_FILE} /app/app.jar
