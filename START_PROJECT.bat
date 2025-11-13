@echo off
REM =====================================================
REM SCRIPT KHỞI ĐỘNG TOÀN BỘ DỰ ÁN
REM =====================================================

echo ==========================================
echo  STARTING CarRental MicroServices System
echo ==========================================
echo.

REM Kiểm tra MySQL
tasklist | find /i "mysqld.exe" > nul
if errorlevel 1 (
    echo [ERROR] MySQL is not running!
    echo Please start MySQL service first.
    pause
    exit /b 1
)
echo [OK] MySQL is running
echo.

REM Kiểm tra databases
echo Checking databases...
mysql -u root -p15112005!Nah -e "USE legal_contract;" 2>nul
if errorlevel 1 (
    echo [INFO] Database not found, running setup...
    call database\run_all.bat
) else (
    echo [OK] Databases exist
)
echo.

REM Start services
echo ==========================================
echo Starting services...
echo ==========================================
echo.

echo [1/3] Starting LegalContractService on port 8082...
start "LegalContractService" cmd /k "cd LegalContractService && mvn spring-boot:run"

timeout /t 5 /nobreak > nul

echo [2/3] Starting VehicleServiceManagementService on port 8083...
start "VehicleManagementService" cmd /k "cd VehicleServiceManagementService && mvn spring-boot:run"

timeout /t 5 /nobreak > nul

echo [3/3] Starting UI Service on port 8080...
start "UI Service" cmd /k "cd ui-service && mvn spring-boot:run"

timeout /t 15 /nobreak > nul

echo ==========================================
echo ALL SERVICES STARTED!
echo ==========================================
echo.
echo Access URLs:
echo - UI: http://localhost:8080
echo - Vehicle Group: http://localhost:8080/admin/vehicle-group
echo - Contract: http://localhost:8080/admin/enhanced-contract
echo - Service: http://localhost:8080/admin/vehicle-service
echo - Check-in/out: http://localhost:8080/admin/checkin-checkout
echo.
echo Press any key to open browser...
pause > nul

start http://localhost:8080

echo.
echo Services are running in separate windows.
echo Close those windows to stop the services.
echo.
pause




