# Use Eclipse Temurin JDK 17
FROM eclipse-temurin:17-jdk

# Working directory inside the container
WORKDIR /app

# Copy the generated JAR file
COPY target/*.jar app.jar

# Spring Boot runs on port 8080
EXPOSE 8080

# Start the application
ENTRYPOINT ["java", "-jar", "app.jar"]