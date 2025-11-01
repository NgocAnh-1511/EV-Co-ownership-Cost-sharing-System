# 🔍 KIỂM TRA KẾT NỐI DATABASE VÀ API

## 📋 CHECKLIST KIỂM TRA

### ✅ Bước 1: Kiểm Tra Services Có Đang Chạy Không

#### Terminal 1 - LegalContractService (Port 8082)
```bash
# Kiểm tra
curl http://localhost:8082/api/legalcontracts/all

# Nếu thấy JSON data → ✅ OK
# Nếu thấy "Connection refused" → ❌ Service chưa chạy
```

#### Terminal 2 - VehicleServiceManagementService (Port 8083)  
```bash
# Kiểm tra
curl http://localhost:8083/api/vehiclegroups/all

# Nếu thấy JSON data → ✅ OK
# Nếu thấy "Connection refused" → ❌ Service chưa chạy
```

### ✅ Bước 2: Kiểm Tra Database Có Dữ Liệu

Mở MySQL và chạy:
```sql
USE legal_contract;
SELECT COUNT(*) FROM legalcontract;
SELECT COUNT(*) FROM checkinoutlog;

USE vehicle_management;
SELECT COUNT(*) FROM vehiclegroup;
SELECT COUNT(*) FROM vehicle;
SELECT COUNT(*) FROM vehicleservice;
```

**Kết quả mong đợi:**
- legalcontract: 8 records
- checkinoutlog: 5 records
- vehiclegroup: 5 records
- vehicle: 10 records
- vehicleservice: 5 records

### ✅ Bước 3: Kiểm Tra API Endpoints Hoạt Động

#### Test từ Browser:
```
# LegalContractService
http://localhost:8082/api/legalcontracts/all
http://localhost:8082/api/checkinout/all

# VehicleServiceManagementService
http://localhost:8083/api/vehiclegroups/all
http://localhost:8083/api/vehicleservices/all
http://localhost:8083/api/vehicleservices/vehicles
```

## 🔧 NGUYÊN NHÂN VÀ CÁCH SỬA

### ❌ Vấn Đề 1: "Không thể tải danh sách nhóm"
**Nguyên nhân**: Service trên port 8083 chưa chạy

**Giải pháp**:
```bash
cd VehicleServiceManagementService
mvn spring-boot:run
```

Kiểm tra log có dòng:
```
Started VehicleServiceManagementServiceApplication in X.XXX seconds
```

### ❌ Vấn Đề 2: "Empty response from server"
**Nguyên nhân**: Database chưa có dữ liệu

**Giải pháp**:
```bash
# Chạy script setup database
database\run_all.bat
```

Hoặc thủ công:
```bash
mysql -u root -p < database/create_schema.sql
mysql -u root -p < database/seed_data.sql
```

### ❌ Vấn Đề 3: "CORS policy blocked"
**Nguyên nhân**: Thiếu @CrossOrigin annotation

**Giải pháp**: Đã có rồi trong các controller:
- CheckinoutlogAPI.java ✅
- LegalContractAPI.java ✅
- VehicleServiceAPI.java ✅
- VehicleGroupAPI.java ✅

### ❌ Vấn Đề 4: "404 Not Found"
**Nguyên nhân**: Wrong port hoặc path

**Giải pháp**: Kiểm tra URLs:
- UI JS dùng port 8083 cho vehicle groups → ✅
- UI JS dùng port 8082 cho contracts → ✅

## 🧪 TEST SCRIPT

Tạo file `test_apis.html` trong browser để test:

```html
<!DOCTYPE html>
<html>
<head>
    <title>API Test</title>
</head>
<body>
    <h1>API Connection Test</h1>
    <button onclick="testGroupAPI()">Test Groups API</button>
    <button onclick="testContractAPI()">Test Contracts API</button>
    <button onclick="testVehiclesAPI()">Test Vehicles API</button>
    <div id="result"></div>

    <script>
        function testGroupAPI() {
            fetch('http://localhost:8083/api/vehiclegroups/all')
                .then(r => r.json())
                .then(d => document.getElementById('result').innerHTML = `<pre>${JSON.stringify(d, null, 2)}</pre>`)
                .catch(e => document.getElementById('result').innerHTML = `ERROR: ${e}`);
        }
        
        function testContractAPI() {
            fetch('http://localhost:8082/api/legalcontracts/all')
                .then(r => r.json())
                .then(d => document.getElementById('result').innerHTML = `<pre>${JSON.stringify(d, null, 2)}</pre>`)
                .catch(e => document.getElementById('result').innerHTML = `ERROR: ${e}`);
        }
        
        function testVehiclesAPI() {
            fetch('http://localhost:8083/api/vehicleservices/vehicles')
                .then(r => r.json())
                .then(d => document.getElementById('result').innerHTML = `<pre>${JSON.stringify(d, null, 2)}</pre>`)
                .catch(e => document.getElementById('result').innerHTML = `ERROR: ${e}`);
        }
    </script>
</body>
</html>
```

## 📊 KIỂM TRA NHANH

### Chạy Lệnh Này:
```bash
# Kiểm tra services
netstat -ano | findstr :8082
netstat -ano | findstr :8083

# Kiểm tra databases
mysql -u root -p -e "SELECT COUNT(*) FROM vehicle_management.vehiclegroup;"
mysql -u root -p -e "SELECT COUNT(*) FROM legal_contract.legalcontract;"
```

## ✅ KẾT QUẢ MONG ĐỢI

### Services:
- ✅ LegalContractService running on port 8082
- ✅ VehicleServiceManagementService running on port 8083
- ✅ UI Service running on port 8080

### Databases:
- ✅ legal_contract has 8 contracts
- ✅ vehicle_management has 5 groups
- ✅ vehicle_management has 10 vehicles

### API:
- ✅ All endpoints return JSON data
- ✅ No CORS errors
- ✅ No 404 errors




