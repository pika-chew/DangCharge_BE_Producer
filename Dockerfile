# 실행 이미지. JAR는 Jenkinsfile의 CI 단계(./gradlew bootJar)에서 미리 만들어 둔 것을 복사한다
FROM eclipse-temurin:17-jre

WORKDIR /app

RUN groupadd --system app && useradd --system --gid app app

COPY build/libs/*.jar app.jar

USER app

EXPOSE 8080

# 컨테이너 메모리 제한 기준으로 힙 크기를 잡음
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75"

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
