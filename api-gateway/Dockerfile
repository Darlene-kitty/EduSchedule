FROM eclipse-temurin:23-jdk-alpine
WORKDIR /app
COPY target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-XX:+UseContainerSupport","-XX:MaxRAMPercentage=60.0","-XX:+UseG1GC","-XX:+OptimizeStringConcat","-Djava.security.egd=file:/dev/./urandom","-jar","app.jar"]
