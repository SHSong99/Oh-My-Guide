# Stage 1: Build
FROM amazoncorretto:17-alpine AS builder

WORKDIR /app

COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .
COPY src src

RUN chmod +x gradlew && ./gradlew bootJar --no-daemon -x test

# Stage 2: Runtime
FROM amazoncorretto:17-alpine

WORKDIR /app

COPY --from=builder /app/build/libs/oh-my-guide-*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
