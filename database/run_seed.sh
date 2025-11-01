#!/bin/bash

# =====================================================
# Script để chạy seed data cho 2 databases
# =====================================================

echo "=========================================="
echo " SEEDING DATABASES - CarRental System"
echo "=========================================="
echo ""

# Kiểm tra MySQL đang chạy
if ! pgrep -x mysqld > /dev/null; then
    echo "[ERROR] MySQL is not running!"
    echo "Please start MySQL service first."
    exit 1
fi

echo "[INFO] MySQL is running..."
echo ""

# Nhập thông tin MySQL
read -p "Enter MySQL username (default: root): " MYSQL_USER
MYSQL_USER=${MYSQL_USER:-root}

read -sp "Enter MySQL password: " MYSQL_PASS
echo ""

read -p "Enter MySQL host (default: localhost): " MYSQL_HOST
MYSQL_HOST=${MYSQL_HOST:-localhost}

echo ""
echo "=========================================="
echo "Seeding legal_contract database..."
echo "=========================================="

mysql -h $MYSQL_HOST -u $MYSQL_USER -p$MYSQL_PASS < database/01_legal_contract_seed.sql

if [ $? -ne 0 ]; then
    echo "[ERROR] Failed to seed legal_contract database!"
    exit 1
fi

echo "[SUCCESS] legal_contract database seeded successfully!"
echo ""

echo "=========================================="
echo "Seeding vehicle_management database..."
echo "=========================================="

mysql -h $MYSQL_HOST -u $MYSQL_USER -p$MYSQL_PASS < database/02_vehicle_management_seed.sql

if [ $? -ne 0 ]; then
    echo "[ERROR] Failed to seed vehicle_management database!"
    exit 1
fi

echo "[SUCCESS] vehicle_management database seeded successfully!"
echo ""

echo "=========================================="
echo "ALL DATABASES SEEDED SUCCESSFULLY!"
echo "=========================================="
echo ""
echo "You can now start the services and test the application."




