ARG SERVICE=auth-service
FROM maven:3.9.9-eclipse-temurin-17 AS build
ARG SERVICE
WORKDIR /workspace
COPY pom.xml .
COPY common-lib ./common-lib
COPY api-gateway ./api-gateway
COPY auth-service ./auth-service
COPY hrm-service ./hrm-service
COPY project-service ./project-service
COPY chatbot-service ./chatbot-service
RUN mvn -pl ${SERVICE} -am -DskipTests package

FROM eclipse-temurin:17-jre-alpine
ARG SERVICE
WORKDIR /app
COPY --from=build /workspace/${SERVICE}/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]
