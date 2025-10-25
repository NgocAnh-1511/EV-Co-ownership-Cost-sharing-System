@echo off
echo 🚀 Building EV Co-ownership System...

REM Build Group Management Service
echo 📦 Building Group Management Service...
cd group-management-service
mvn clean package -DskipTests
cd ..

REM Build Cost Payment Service
echo 📦 Building Cost Payment Service...
cd cost-payment-service
mvn clean package -DskipTests
cd ..

REM Build UI Service
echo 📦 Building UI Service...
cd ui-service
mvn clean package -DskipTests
cd ..

echo ✅ All services built successfully!
echo 🐳 Starting Docker Compose...

REM Start with Docker Compose
docker-compose up --build

echo 🎉 System is running!
echo 📱 Access the application at: http://localhost:8080
echo 📊 Group Management API: http://localhost:8082/api/groups
echo 💰 Cost Payment API: http://localhost:8083/api/costs
