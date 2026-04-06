# Spring Boot 4 requires Java 21, so the container runtime is fixed to 21.
FROM bellsoft/liberica-openjdk-alpine:21 AS build
WORKDIR /app

COPY gradlew ./
COPY gradle gradle
COPY build.gradle settings.gradle ./

RUN chmod +x gradlew
RUN ./gradlew dependencies --no-daemon

COPY src src

RUN ./gradlew bootJar -x test --no-daemon

FROM bellsoft/liberica-openjre-alpine:21
WORKDIR /app

COPY --from=build /app/build/libs/*.jar /app/app.jar

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "exec java ${JAVA_OPTS:--XX:+UseG1GC -Xms256m -Xmx384m -Xss512k -Djava.security.egd=file:/dev/./urandom} -jar /app/app.jar"]
