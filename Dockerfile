# ── 1단계: 빌드 ──────────────────────────────────────────────
# 멀티모듈 프로젝트라서 settings.gradle과 다른 모듈들도 다 있어야
# ":message-connection-flux:bootJar" 태스크가 의존성을 제대로 찾습니다.
# 그래서 프로젝트 루트 전체를 복사해서 빌드합니다.
FROM gradle:8.10-jdk21 AS build
WORKDIR /workspace
COPY . .
RUN gradle :message-connection-flux:bootJar -x test --no-daemon

# ── 2단계: 실행 ──────────────────────────────────────────────
# JDK가 아니라 JRE만 있는 slim 이미지로 최종 크기를 줄입니다.
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# non-root로 실행 (컨테이너가 루트 권한으로 뜨는 걸 막는 기본적인 보안 습관)
RUN groupadd -r spring && useradd -r -g spring spring
USER spring:spring

# bootJar 결과물이 하나만 나온다는 전제입니다. 혹시 plain jar까지 같이 나오면
# 와일드카드가 2개를 잡아서 COPY가 실패할 수 있어요 — 그럴 땐 정확한 파일명으로 바꿔주세요.
COPY --from=build /workspace/message-connection-flux/build/libs/*.jar app.jar

EXPOSE 8090
ENTRYPOINT ["java", "-jar", "app.jar"]
