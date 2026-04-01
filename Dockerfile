# syntax=docker/dockerfile:1

# ── Build stage ───────────────────────────────────────────────────────────────
# Uses the Maven + Eclipse Temurin JDK 26 image.
# If eclipse-temurin:26-jdk-jammy is not yet available, substitute with 21.
FROM eclipse-temurin:26-jdk-jammy AS build

WORKDIR /build

# Copy wrapper and pom first to cache the dependency download layer
COPY .mvn .mvn
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw dependency:go-offline -B

COPY src ./src
RUN ./mvnw package -DskipTests -B

# ── Runtime stage ─────────────────────────────────────────────────────────────
FROM eclipse-temurin:26-jre-jammy

WORKDIR /app

RUN groupadd -r mcp && useradd -r -g mcp mcp

COPY --from=build /build/target/mcp-*.jar app.jar

# Kubeconfig is bind-mounted at /app/kubeconfig at runtime (read-only)
ENV KUBECONFIG=/app/kubeconfig

# Override at container start with -e SPRING_PROFILES_ACTIVE=local for dev
ENV SPRING_PROFILES_ACTIVE=runner

USER mcp

EXPOSE 8080

# -Xmx384m keeps the JVM under the 512m container memory limit
ENTRYPOINT ["java", "-Xmx384m", "-jar", "app.jar"]
