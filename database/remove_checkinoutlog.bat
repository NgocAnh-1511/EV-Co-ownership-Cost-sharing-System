@echo off
REM =====================================================
REM Script xóa bảng checkinoutlog và foreign key constraint
REM =====================================================

echo ==========================================
echo  XÓA BẢNG CHECKINOUTLOG
echo ==========================================
echo.

set /p MYSQL_USER="Enter MySQL username (default: root): "
if "%MYSQL_USER%"=="" set MYSQL_USER=root

set /p MYSQL_PASS="Enter MySQL password: "

echo.
echo ==========================================
echo Removing checkinoutlog table and constraints...
echo ==========================================

mysql -u %MYSQL_USER% -p%MYSQL_PASS% < database\remove_checkinoutlog_simple.sql

if errorlevel 1 (
    echo [ERROR] Failed to remove checkinoutlog!
    pause
    exit /b 1
)

echo [SUCCESS] checkinoutlog table and constraints removed!
echo.
echo ==========================================
echo You can now delete vehicle groups without errors.
echo ==========================================
echo.
pause

