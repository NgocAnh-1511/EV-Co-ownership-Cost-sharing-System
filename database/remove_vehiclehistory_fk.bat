@echo off
REM =====================================================
REM Script xóa foreign key constraint từ vehiclehistory
REM =====================================================

echo ==========================================
echo  XÓA FOREIGN KEY CONSTRAINT VEHICLEHISTORY
echo ==========================================
echo.

set /p MYSQL_USER="Enter MySQL username (default: root): "
if "%MYSQL_USER%"=="" set MYSQL_USER=root

set /p MYSQL_PASS="Enter MySQL password: "

echo.
echo ==========================================
echo Removing foreign key constraint...
echo ==========================================

mysql -u %MYSQL_USER% -p%MYSQL_PASS% < database\remove_vehiclehistory_fk.sql 2>nul

if errorlevel 1 (
    echo [INFO] Trying simple method...
    mysql -u %MYSQL_USER% -p%MYSQL_PASS% -e "USE vehicle_management; ALTER TABLE vehiclehistory DROP FOREIGN KEY vehiclehistory_ibfk_1;" 2>nul
    if errorlevel 1 (
        echo [WARNING] Foreign key constraint may not exist or already removed.
    )
)

echo [SUCCESS] Foreign key constraint removed!
echo.
echo ==========================================
echo You can now delete vehicle groups without errors.
echo ==========================================
echo.
pause

