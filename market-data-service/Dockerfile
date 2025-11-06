# Stage 1: Build with Maven
# Use an official Maven image with JDK 17 to build the application
FROM maven:3.8.5-openjdk-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
# Build the application and skip tests for a faster build
RUN mvn clean package -DskipTests

# Stage 2: Create the final, small image
# Use a lightweight JRE-only image to run the application
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
# Copy the built JAR from the build stage. Update the JAR name if your pom.xml differs.
COPY --from=build /app/target/market-data-service-0.0.1-SNAPSHOT.jar app.jar
# Render's web services automatically listen on 8080, which Spring Boot uses by default
EXPOSE 8080
# Run the application
ENTRYPOINT ["java","-jar","app.jar"]