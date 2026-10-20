FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace
COPY pom.xml .
COPY src ./src
RUN mvn -B -DskipTests package

FROM eclipse-temurin:21-jre
WORKDIR /app
RUN groupadd --gid 10001 appuser && useradd --uid 10001 --gid appuser --create-home --shell /usr/sbin/nologin appuser
COPY --from=build /workspace/target/*.jar /app/tnsfrp.jar
USER appuser
EXPOSE 8080
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-jar", "/app/tnsfrp.jar"]
