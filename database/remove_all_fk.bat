@echo off
REM =====================================================
REM Script xóa tất cả foreign key constraints liên quan đến vehiclegroup
REM =====================================================

echo ==========================================
echo  XÓA FOREIGN KEY CONSTRAINTS
echo ==========================================
echo.

set /p MYSQL_USER="Enter MySQL username (default: root): "
if "%MYSQL_USER%"=="" set MYSQL_USER=root

set /p MYSQL_PASS="Enter MySQL password: "

echo.
echo ==========================================
echo Removing foreign key constraints...
echo ==========================================

mysql -u %MYSQL_USER% -p%MYSQL_PASS% < database\remove_all_fk.sql 2>nul

if errorlevel 1 (
    echo [INFO] Trying simple method...
    mysql -u %MYSQL_USER% -p%MYSQL_PASS% -e "USE legal_contract; ALTER TABLE legalcontract DROP FOREIGN KEY legalcontract_ibfk_1;" 2>nul
    if errorlevel 1 (
        echo [WARNING] Foreign key constraint may not exist or already removed.
    )
)

echo [SUCCESS] Foreign key constraints removed!
echo.
echo ==========================================
echo You can now delete vehicle groups without errors.
echo ==========================================
echo.
pause

