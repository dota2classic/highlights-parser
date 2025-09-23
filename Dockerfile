# ---- Build stage ----
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app

# Copy Gradle/Maven wrapper & project files
COPY . .

# Build your Spring Boot jar
# If using Gradle:
RUN ./gradlew bootJar --no-daemon
# If using Maven:
# RUN ./mvnw package -DskipTests

# ---- Run stage ----
FROM eclipse-temurin:21-jre AS runtime
WORKDIR /app

# Copy only the built jar from previous stage
COPY --from=build /app/build/libs/*.jar app.jar

# Run the application
ENTRYPOINT ["java","-jar","/app/app.jar"]
