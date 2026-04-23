FROM eclipse-temurin:23-jdk-alpine
VOLUME /tmp
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java","-XX:+UseContainerSupport","-XX:MaxRAMPercentage=60.0","-XX:+UseG1GC","-XX:+OptimizeStringConcat","-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar"]
