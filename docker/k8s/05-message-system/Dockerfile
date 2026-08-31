# message-connection-flux와 똑같은 패턴입니다 (같은 monorepo라 재사용).
FROM gradle:8.10-jdk21 AS build
WORKDIR /workspace
COPY . .
RUN gradle :message-system:bootJar -x test --no-daemon

FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
RUN groupadd -r spring && useradd -r -g spring spring
USER spring:spring
COPY --from=build /workspace/message-system/build/libs/*.jar app.jar
EXPOSE 8070
ENTRYPOINT ["java", "-jar", "app.jar"]
