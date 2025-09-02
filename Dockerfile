FROM maven:3.9.11-eclipse-temurin-24@sha256:db74cfbb321f7c1c0ffa735d188dc4215a279e8f677fd62aed4a955606face85 AS builder
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

FROM eclipse-temurin:17.0.16_8-jre-noble@sha256:67ec7c0734126209658b8b1bffdd20f321418971417d0a4c646dd03619d4169d
WORKDIR /app
COPY --from=builder /app/target/vehicle-catalog-*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
