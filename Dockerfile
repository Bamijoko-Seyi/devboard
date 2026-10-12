# ---------- Stage 1: Build ----------
FROM eclipse-temurin:21-jdk-alpine AS build

WORKDIR /app

# Copy Maven wrapper and pom first (better layer caching)
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./

# Make wrapper executable (Git strips +x on Windows commits)
RUN chmod +x mvnw

# Download dependencies — cached until pom.xml changes
RUN ./mvnw -B dependency:go-offline

# Copy source and build
COPY src ./src
RUN ./mvnw -B clean package -DskipTests

# ---------- Stage 2: Runtime ----------
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

RUN addgroup -S spring && adduser -S spring -G spring
USER spring

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]