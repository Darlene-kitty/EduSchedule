#!/bin/bash
# Rebuild all Spring Boot services

echo "Building all services..."

# Build each service
mvn clean package -DskipTests -pl config-server
mvn clean package -DskipTests -pl api-gateway
mvn clean package -DskipTests -pl user-service
mvn clean package -DskipTests -pl resource-service
mvn clean package -DskipTests -pl notification-service

echo "Build complete! Now restart with: docker-compose up -d --build"
