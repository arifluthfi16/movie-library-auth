FROM openjdk:17-jdk-slim
WORKDIR /app

COPY target/auth-service-1.0.jar app.jar
COPY config-prod.yml config.yml

EXPOSE 8080
CMD ["java", "-jar", "app.jar", "server", "config.yml"]