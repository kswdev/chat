# message-connection-flux/message-auth와 똑같은 패턴입니다 (같은 monorepo라 재사용).
# message-social도 Java 21 toolchain(build.gradle 참고)이라 jdk21 빌드 이미지를 그대로 씁니다.
FROM gradle:8.10-jdk21 AS build
WORKDIR /workspace
COPY . .
RUN gradle :message-social:bootJar -x test --no-daemon

FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
RUN groupadd -r spring && useradd -r -g spring spring
USER spring:spring
COPY --from=build /workspace/message-social/build/libs/*.jar app.jar
EXPOSE 8087
ENTRYPOINT ["java", "-jar", "app.jar"]
