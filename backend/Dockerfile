FROM maven:3.9.16-eclipse-temurin-21-alpine AS build

WORKDIR /workspace
COPY pom.xml ./
RUN mvn --batch-mode --no-transfer-progress dependency:go-offline
COPY src ./src
RUN mvn --batch-mode --no-transfer-progress -DskipTests package

FROM eclipse-temurin:21.0.11_10-jre-alpine AS runtime

RUN addgroup --system societyapp \
    && adduser --system --ingroup societyapp --home /app societyapp
WORKDIR /app
COPY --from=build --chown=societyapp:societyapp /workspace/target/backend-*.jar app.jar

USER societyapp
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
