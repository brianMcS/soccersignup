FROM openjdk:17-jdk-slim

WORKDIR /app

# Copy the JAR file into the image
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar

# Run the Spring Boot application
ENTRYPOINT ["java", "-jar", "app.jar"]
