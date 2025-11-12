# Test API Endpoints

## 1. Test Backend API

### Test VehicleServiceManagementService API
```bash
# Test endpoint
curl http://localhost:8083/api/vehicleservices/test

# Get all vehicle services
curl http://localhost:8083/api/vehicleservices

# Get all vehicles
curl http://localhost:8082/api/vehicles
```

## 2. Test từ Browser

Mở browser và truy cập:
- http://localhost:8083/api/vehicleservices/test
- http://localhost:8083/api/vehicleservices
- http://localhost:8082/api/vehicles

## 3. Kiểm tra Logs

Xem logs trong console của:
- VehicleServiceManagementService (port 8083)
- ui-service (port 8082)

## 4. Kiểm tra Database

Chạy SQL query:
```sql
SELECT * FROM vehicle_management.vehicleservice;
SELECT COUNT(*) FROM vehicle_management.vehicleservice;
SELECT DISTINCT vehicle_id FROM vehicle_management.vehicleservice;
SELECT service_type, COUNT(*) FROM vehicle_management.vehicleservice GROUP BY service_type;
```

