FROM eclipse-temurin:17-jdk-jammy AS builder

WORKDIR /workspace
COPY . .
RUN chmod +x gradlew && ./gradlew clean bootJar --no-daemon

FROM eclipse-temurin:17-jre-jammy

WORKDIR /app
COPY --from=builder /workspace/build/libs/*.jar /app/app.jar
COPY --from=builder /workspace/generated-stylings /app/generated-stylings

EXPOSE 10000
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
