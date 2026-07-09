# ---- Build stage ----------------------------------------------------------
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace

# Copy the pom alone first so dependency downloads are cached as their own
# layer and only re-run when the pom changes, not on every source edit.
COPY pom.xml .
RUN mvn -B -q dependency:go-offline

COPY src ./src
# Tests mock the LLM chain and could run here, but skipping keeps image builds
# fast; run `mvn verify` separately for tests + the coverage gate.
RUN mvn -B -q package -DskipTests

# ---- Runtime stage ---------------------------------------------------------
FROM eclipse-temurin:21-jre
WORKDIR /app

RUN useradd -r -u 1001 appuser
USER appuser

# The .original file left by the Spring Boot repackage does not match *.jar,
# so this copies exactly the executable fat jar.
COPY --from=build /workspace/target/*.jar app.jar

# Inside a container, "localhost" is the container itself — point Embabel at
# the Ollama server on the Docker host instead. Docker Desktop (Windows/macOS)
# resolves host.docker.internal automatically; on plain Linux run with
#   --add-host=host.docker.internal:host-gateway
# or override this variable with your Ollama URL.
ENV EMBABEL_AGENT_PLATFORM_MODELS_OLLAMA_BASE_URL=http://host.docker.internal:11434

EXPOSE 8082
ENTRYPOINT ["java", "-jar", "app.jar"]
