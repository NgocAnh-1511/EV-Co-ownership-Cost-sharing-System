FROM openjdk:23-jdk-slim

WORKDIR /app

# Copy pom.xml
COPY pom.xml .

# Copy source code
COPY src src

# Install Maven and build the application
RUN apt-get update && apt-get install -y maven && mvn clean package -DskipTests

# Copy the built jar
RUN cp target/group-management-service-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8082

ENTRYPOINT ["java", "-jar", "app.jar"]
