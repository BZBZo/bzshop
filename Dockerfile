# 첫 번째 스테이지: 빌드 스테이지
FROM gradle:jdk21-graal-jammy AS builder

# 작업 디렉토리 설정
WORKDIR /workspace

# Gradle 래퍼와 프로젝트 관련 파일 복사
COPY gradlew .
COPY gradle ./gradle
COPY build.gradle .
COPY settings.gradle .

# Gradle 캐시 디렉토리 생성 및 설정 복사
RUN mkdir -p /home/gradle/.gradle && chown -R gradle:gradle /workspace /home/gradle/.gradle
COPY gradle.properties /home/gradle/.gradle/gradle.properties

# Gradle 래퍼에 실행 권한 부여
RUN chmod +x gradlew && chown -R gradle:gradle /workspace

# Gradle JVM 옵션 설정
ENV GRADLE_OPTS="-Xmx1024m -XX:MaxMetaspaceSize=256m -XX:+HeapDumpOnOutOfMemoryError"

# Gradle 종속성 설치
RUN ./gradlew dependencies --no-daemon --no-parallel \
    -Dorg.gradle.jvmargs="-Xmx1024m -XX:MaxMetaspaceSize=256m" \
    --stacktrace --info

# 소스 코드 복사
COPY src src

# Gradle 컴파일
RUN ./gradlew compileJava --no-daemon --no-parallel \
    -Dorg.gradle.jvmargs="-Xmx1024m -XX:MaxMetaspaceSize=256m" \
    --stacktrace --info

# Gradle 패키징
RUN ./gradlew assemble --no-daemon --no-parallel \
    -Dorg.gradle.jvmargs="-Xmx1024m -XX:MaxMetaspaceSize=256m" \
    --stacktrace --info

# 두 번째 스테이지: 실행 스테이지
FROM container-registry.oracle.com/graalvm/jdk:21

# 작업 디렉토리 설정
WORKDIR /workspace

# 첫 번째 스테이지에서 빌드된 JAR 파일 복사
COPY --from=builder /workspace/build/libs/*.jar app.jar

# 실행할 JAR 파일 지정
ENTRYPOINT ["java", "-jar", "app.jar"]
