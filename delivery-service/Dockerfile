# Dockerfile for delivery-service

FROM eclipse-temurin:17-jre-alpine
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java","-Xmx256m","-Xms128m","-jar","/app.jar"]
