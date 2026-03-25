# Multi-stage Dockerfile for Spring Boot Maven app on Render
FROM eclipse-temurin:17-jdk-alpine as builder

WORKDIR /app
COPY . .
RUN apk add --no-cache maven openjdk17 && \
mvn clean package -DskipTests

FROM eclipse-temurin:17-jre-alpine

WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar

EXPOSE $PORT
ENV PORT=10000
ENTRYPOINT ["java", "-jar", "app.jar"]
