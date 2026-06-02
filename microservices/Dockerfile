ARG SERVICE=auth-service
FROM eclipse-temurin:17-jre-alpine
ARG SERVICE
WORKDIR /app
COPY ${SERVICE}/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]