# syntax=docker/dockerfile:1
# One Dockerfile for every service: docker build --build-arg MODULE=accounts-service .
FROM eclipse-temurin:21.0.13_9-jdk-noble AS build
ARG MODULE
WORKDIR /src
COPY . .
# Tests run outside Docker (they start their own containers); the cache keeps Gradle and dependencies between builds
RUN --mount=type=cache,target=/root/.gradle sh ./gradlew ":${MODULE}:bootJar" --no-daemon -q

FROM eclipse-temurin:21.0.13_9-jre-noble
ARG MODULE
COPY --from=build /src/${MODULE}/build/libs/${MODULE}.jar /app.jar
ENV JAVA_TOOL_OPTIONS="-Xmx256m"
ENTRYPOINT ["java", "-jar", "/app.jar"]
