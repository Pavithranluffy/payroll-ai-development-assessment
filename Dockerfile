FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY target/payroll-sync-service-*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
