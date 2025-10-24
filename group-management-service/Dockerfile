FROM openjdk:23-jdk-slim

WORKDIR /app

COPY target/group-management-service-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8082

ENTRYPOINT ["java", "-jar", "app.jar"]
