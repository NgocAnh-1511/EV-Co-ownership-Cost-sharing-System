# 🚨 SỬA NHANH - KHÔNG TẢI ĐƯỢC DỮ LIỆU

## ⚡ CÁCH SỬA NHANH NHẤT

### Chạy Script Tự Động:
```batch
# Double-click file này
START_PROJECT.bat
```

Script này sẽ:
1. ✅ Kiểm tra MySQL
2. ✅ Setup database nếu chưa có
3. ✅ Start tất cả 3 services
4. ✅ Mở browser tự động

## 🔧 NẾU VẪN KHÔNG ĐƯỢC - KIỂM TRA TỪNG BƯỚC

### Bước 1: Đảm Bảo MySQL Đang Chạy
```bash
# Mở MySQL Workbench hoặc Command Line
# Test connection
mysql -u root -p
```

### Bước 2: Kiểm Tra Databases Có Dữ Liệu
```sql
USE legal_contract;
SELECT COUNT(*) FROM legalcontract;
-- Phải có 8 records

USE vehicle_management;
SELECT COUNT(*) FROM vehiclegroup;
-- Phải có 5 records
```

**Nếu count = 0 → Chạy:**
```batch
database\run_all.bat
```

### Bước 3: Kiểm Tra Services Có Chạy

Mở 3 cửa sổ terminal:

**Terminal 1:**
```bash
cd LegalContractService
mvn spring-boot:run
```
Đợi đến khi thấy: `Started LegalContractServiceApplication`

**Terminal 2:**
```bash
cd VehicleServiceManagementService
mvn spring-boot:run
```
Đợi đến khi thấy: `Started VehicleServiceManagementServiceApplication`

**Terminal 3:**
```bash
cd ui-service
mvn spring-boot:run
```
Đợi đến khi thấy: `Started UiServiceApplication`

### Bước 4: Test API Trong Browser

Mở các URLs sau trong browser:

```
✅ http://localhost:8083/api/vehiclegroups/all
   → Phải thấy JSON với 5 groups

✅ http://localhost:8082/api/legalcontracts/all
   → Phải thấy JSON với 8 contracts

✅ http://localhost:8083/api/vehicleservices/vehicles
   → Phải thấy JSON với 10 vehicles
```

**Nếu thấy "Connection refused":**
- ❌ Service chưa chạy trên port đó
- Chạy lại service tương ứng

**Nếu thấy "[]":**
- ❌ Database chưa có dữ liệu
- Chạy `database\run_all.bat`

**Nếu thấy dữ liệu JSON:**
- ✅ OK! API hoạt động
- Vấn đề có thể ở JavaScript

### Bước 5: Kiểm Tra JavaScript Console

Mở browser → F12 → Console tab

Vào trang: `http://localhost:8080/admin/vehicle-group`

Xem có lỗi:
- ❌ `Error loading groups: Failed to fetch` → Service port 8083 chưa chạy
- ❌ `404 Not Found` → URL sai
- ❌ `CORS policy` → Thiếu @CrossOrigin

## 🎯 NGUYÊN NHÂN THƯỜNG GẶP

### 1. Services Chưa Chạy
**Fix**: Chạy 3 services trong 3 terminal riêng

### 2. Database Chưa Có Dữ Liệu
**Fix**: Chạy `database\run_all.bat`

### 3. Port Bị Trùng
**Fix**: Kill process đang dùng ports 8082, 8083, 8080
```bash
netstat -ano | findstr :8082
taskkill /PID <process_id> /F
```

### 4. MySQL Không Chạy
**Fix**: Start MySQL service

### 5. Wrong Password
**Fix**: Sửa password trong application.properties

## ✅ CHECKLIST CUỐI CÙNG

- [ ] MySQL đang chạy
- [ ] Databases đã tạo (legal_contract, vehicle_management)
- [ ] Databases có dữ liệu (check counts)
- [ ] LegalContractService chạy trên port 8082
- [ ] VehicleServiceManagementService chạy trên port 8083
- [ ] UI Service chạy trên port 8080
- [ ] Có thể truy cập http://localhost:8083/api/vehiclegroups/all
- [ ] Không có lỗi CORS
- [ ] Console browser không có errors

## 🎬 CÁCH KHỞI ĐỘNG ĐÚNG

**Option 1 - Tự động (Nhanh nhất):**
```batch
START_PROJECT.bat
```

**Option 2 - Thủ công:**
```bash
# Terminal 1
cd LegalContractService && mvn spring-boot:run

# Terminal 2 (sau khi terminal 1 ready)
cd VehicleServiceManagementService && mvn spring-boot:run

# Terminal 3 (sau khi terminal 2 ready)
cd ui-service && mvn spring-boot:run
```

Sau đó mở: http://localhost:8080

## 📞 SUPPORT

Nếu vẫn không được, check:
1. File `TEST_CONNECTION.md` - Hướng dẫn chi tiết
2. File `database/SETUP_INSTRUCTIONS.md` - Setup database
3. File `PROJECT_SUMMARY.md` - Tổng hợp dự án




