#!/bin/bash

echo "🔨 Building User Service..."
cd user-service
mvn clean compile -DskipTests

if [ $? -eq 0 ]; then
    echo "✅ Build successful!"
else
    echo "❌ Build failed!"
    exit 1
fi
