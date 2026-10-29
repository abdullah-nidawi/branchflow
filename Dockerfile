FROM eclipse-temurin:21-jdk AS build
WORKDIR /workspace
COPY . .
RUN chmod +x mvnw && ./mvnw -B -DskipTests clean package

FROM eclipse-temurin:21-jre
RUN useradd --system --create-home --uid 10001 appuser
WORKDIR /app
COPY --from=build --chown=appuser:appuser /workspace/target/branchflow-*.jar /app/app.jar
USER 10001
EXPOSE 8080
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "/app/app.jar"]
