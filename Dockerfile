# STAGE 1: Builder
# Use Maven with JDK 21 to build the project
FROM maven:3.9-eclipse-temurin-21 AS builder

WORKDIR /build

# Copy the local library jar which should be placed in libs/ by the prepare script
COPY libs/pinapp-notify-sdk-1.0.0-SNAPSHOT.jar .

# Install the local library into the container's local Maven repository
RUN mvn install:install-file \
    -Dfile=pinapp-notify-sdk-1.0.0-SNAPSHOT.jar \
    -DgroupId=com.pinapp \
    -DartifactId=pinapp-notify-sdk \
    -Dversion=1.0.0-SNAPSHOT \
    -Dpackaging=jar

# Copy pom.xml and source code
COPY pom.xml .
COPY src ./src

# Build the application, skipping tests to speed up the process
RUN mvn clean package -DskipTests

# STAGE 2: Runner
# Use a lightweight Alpine JRE image for running the application
FROM eclipse-temurin:21-jre-alpine AS runner

WORKDIR /app

# Create a non-root user and group for security
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Copy the built JAR from the builder stage
COPY --from=builder /build/target/*.jar app.jar

# Change ownership of the application file to the non-root user
RUN chown appuser:appgroup app.jar

# Switch to the non-root user
USER appuser

# Define the entrypoint to run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
