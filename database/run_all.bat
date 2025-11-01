@echo off
REM =====================================================
REM Script tạo toàn bộ database và dữ liệu mẫu
REM =====================================================

echo ==========================================
echo  SETTING UP DATABASES - CarRental System
echo ==========================================
echo.

set /p MYSQL_USER="Enter MySQL username (default: root): "
if "%MYSQL_USER%"=="" set MYSQL_USER=root

set /p MYSQL_PASS="Enter MySQL password: "

echo.
echo ==========================================
echo 1. Creating databases and tables...
echo ==========================================

mysql -u %MYSQL_USER% -p%MYSQL_PASS% < database\create_schema.sql

if errorlevel 1 (
    echo [ERROR] Failed to create schema!
    pause
    exit /b 1
)

echo [SUCCESS] Databases and tables created!
echo.

echo ==========================================
echo 2. Seeding data...
echo ==========================================

mysql -u %MYSQL_USER% -p%MYSQL_PASS% < database\seed_data.sql

if errorlevel 1 (
    echo [ERROR] Failed to seed data!
    pause
    exit /b 1
)

echo [SUCCESS] Data seeded successfully!
echo.

echo ==========================================
echo ALL DONE! You can now start the services.
echo ==========================================
echo.
pause




