FROM amazoncorretto:17-alpine

WORKDIR /app

COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .
COPY src src

RUN chmod +x gradlew && ./gradlew bootJar --no-daemon -x test

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java -jar build/libs/oh-my-guide-*.jar"]
