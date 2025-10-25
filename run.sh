#!/bin/bash

echo "🚀 Building EV Co-ownership System..."

# Build Group Management Service
echo "📦 Building Group Management Service..."
cd group-management-service
mvn clean package -DskipTests
cd ..

# Build Cost Payment Service
echo "📦 Building Cost Payment Service..."
cd cost-payment-service
mvn clean package -DskipTests
cd ..

# Build UI Service
echo "📦 Building UI Service..."
cd ui-service
mvn clean package -DskipTests
cd ..

echo "✅ All services built successfully!"
echo "🐳 Starting Docker Compose..."

# Start with Docker Compose
docker-compose up --build

echo "🎉 System is running!"
echo "📱 Access the application at: http://localhost:8080"
echo "📊 Group Management API: http://localhost:8082/api/groups"
echo "💰 Cost Payment API: http://localhost:8083/api/costs"
cd D:\JAVAWEB\EV-Co-ownership-Cost-sharing-System
