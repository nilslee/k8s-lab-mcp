# syntax=docker/dockerfile:1

# ── Build stage ───────────────────────────────────────────────────────────────
# Java 26 does not exist yet. Use Java 21 (current LTS).
FROM eclipse-temurin:25-jdk-jammy AS build

WORKDIR /build

# Copy wrapper and pom first to cache the dependency download layer
COPY .mvn .mvn
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw dependency:go-offline -B

COPY src ./src
RUN ./mvnw package -DskipTests -B

# ── Runtime stage ─────────────────────────────────────────────────────────────
FROM eclipse-temurin:25-jre-jammy

WORKDIR /app

RUN groupadd -r mcp && useradd -r -g mcp mcp

COPY --from=build /build/target/mcp-*.jar app.jar

# Shell scripts used by MCP tools (e.g. SystemTools); must exist under /app/scripts
COPY scripts ./scripts
RUN find scripts -type f -name "*.sh" -exec chmod +x {} + \
  && chown -R mcp:mcp scripts

# Kubeconfig is bind-mounted at /app/kubeconfig at runtime (read-only)
ENV KUBECONFIG=/app/kubeconfig

# Override at container start with -e SPRING_PROFILES_ACTIVE=local for dev
ENV SPRING_PROFILES_ACTIVE=runner

USER mcp

EXPOSE 9000

# -Xmx384m keeps the JVM under the 512m container memory limit
ENTRYPOINT ["java", "-Xmx384m", "-jar", "app.jar"]
