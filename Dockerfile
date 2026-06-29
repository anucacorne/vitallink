FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY cloud-service/pom.xml cloud-service/
COPY edge-hub/pom.xml edge-hub/
RUN mvn dependency:go-offline -pl cloud-service -am -q || true
COPY . .
RUN mvn package -pl cloud-service -am -Dmaven.test.skip=true -q

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/cloud-service/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar", "--spring.profiles.active=prod"]
