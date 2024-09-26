# Build Stage
FROM maven:3.8.5-openjdk-17 AS build
WORKDIR /app
COPY . .
RUN mvn install -DskipTests=true

# Run Stage
FROM eclipse-temurin:17-jre-ubi9-minimal
WORKDIR /run
COPY --from=build /app/target/souvi-backend-0.0.1-SNAPSHOT.jar /run/souvi-backend-0.0.1-SNAPSHOT.jar
EXPOSE 8888
ENTRYPOINT java -jar /run/souvi-backend-0.0.1-SNAPSHOT.jar
