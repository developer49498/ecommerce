# --- Build Stage ---
FROM gradle:8.5.0-jdk21 AS build

# Copy the project files
COPY --chown=gradle:gradle . /home/gradle/project
WORKDIR /home/gradle/project

# Build the project
RUN gradle build -x test

# --- Runtime Stage ---
FROM eclipse-temurin:21-jdk-jammy

WORKDIR /app

# Copy the built jar from the build stage
COPY --from=build /home/gradle/project/build/libs/*.jar app.jar


# Copy configuration files (if not included inside JAR)
COPY --from=build /home/gradle/project/src/main/resources/application.properties .
COPY --from=build /home/gradle/project/src/main/resources/serviceAccountKey.json .
COPY --from=build /home/gradle/project/src/main/resources/isms-file-store-6a965ee8bd7c.json .
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
