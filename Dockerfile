FROM maven:3.9-eclipse-temurin-25-alpine AS builder

WORKDIR /app

COPY pom.xml .

RUN mvn dependency:go-offline -B

COPY src ./src

RUN mvn clean package -DskipTests

# Stage 2
FROM eclipse-temurin:25-jre-alpine-3.21 AS runtinme

WORKDIR /app

COPY --from=builder /app/target/RetailFlow_API-1.0.0.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java","-XX:+UseParallelGC","-jar","app.jar"]