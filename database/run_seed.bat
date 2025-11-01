@echo off
REM =====================================================
REM Script để chạy seed data cho 2 databases
REM =====================================================

echo ==========================================
echo  SEEDING DATABASES - CarRental System
echo ==========================================
echo.

REM Kiểm tra MySQL đang chạy
tasklist | find /i "mysqld.exe" > nul
if errorlevel 1 (
    echo [ERROR] MySQL is not running!
    echo Please start MySQL service first.
    pause
    exit /b 1
)

echo [INFO] MySQL is running...
echo.

REM Nhập thông tin MySQL
set /p MYSQL_USER="Enter MySQL username (default: root): "
if "%MYSQL_USER%"=="" set MYSQL_USER=root

set /p MYSQL_PASS="Enter MySQL password: "

echo.
echo ==========================================
echo Seeding all data...
echo ==========================================

mysql -u %MYSQL_USER% -p%MYSQL_PASS% < database\seed_data.sql

if errorlevel 1 (
    echo [ERROR] Failed to seed data!
    pause
    exit /b 1
)

echo [SUCCESS] All data seeded successfully!
echo.

echo ==========================================
echo ALL DATABASES SEEDED SUCCESSFULLY!
echo ==========================================
echo.
echo You can now start the services and test the application.
pause

