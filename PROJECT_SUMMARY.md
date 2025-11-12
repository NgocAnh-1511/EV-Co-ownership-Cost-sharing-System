# 📋 TỔNG HỢP DỰ ÁN - CarRental MicroServices System

## 🎯 TỔNG QUAN

Hệ thống quản lý xe thuê đồng sở hữu sử dụng kiến trúc microservices với 3 services chính.

## 🏗️ CẤU TRÚC DỰ ÁN

### 1️⃣ LegalContractService (Port 8082)
**Database**: `legal_contract`

#### 📁 Files Backend
| File | Chức Năng |
|------|-----------|
| `CheckinoutlogAPI.java` | API quản lý check-in/check-out |
| `LegalContractAPI.java` | API quản lý hợp đồng pháp lý |
| `CheckinoutlogService.java` | Business logic cho check-in/out |
| `LegalContractService.java` | Business logic cho hợp đồng |
| `CheckinoutlogRepository.java` | Data access cho check-in/out |
| `LegalContractRepository.java` | Data access cho hợp đồng |

#### 🔌 API Endpoints
```
POST   /api/checkinout/checkin      - Check-in xe (UC13)
POST   /api/checkinout/checkout     - Check-out xe (UC13)
GET    /api/checkinout/all          - Lấy tất cả logs
GET    /api/checkinout/by-vehicle/{id} - Lọc theo xe
GET    /api/legalcontracts/all      - Lấy tất cả hợp đồng
POST   /api/legalcontracts/create   - Tạo hợp đồng (UC12.2)
PUT    /api/legalcontracts/update/{id} - Sửa hợp đồng (UC12.3)
PUT    /api/legalcontracts/sign/{id} - Ký hợp đồng (UC12.4)
PUT    /api/legalcontracts/archive/{id} - Lưu trữ (UC12.5)
```

#### 📊 Database Tables
- `checkinoutlog` - Lịch sử nhận/trả xe
- `legalcontract` - Hợp đồng pháp lý
- `contractsignature` - Chữ ký hợp đồng
- `contracthistory` - Lịch sử hợp đồng

---

### 2️⃣ VehicleServiceManagementService (Port 8083)
**Database**: `vehicle_management`

#### 📁 Files Backend
| File | Chức Năng |
|------|-----------|
| `VehicleServiceAPI.java` | API quản lý dịch vụ xe |
| `VehicleGroupAPI.java` | API quản lý nhóm xe |
| `VehicleHistoryAPI.java` | API lịch sử sử dụng |
| `VehicleServiceService.java` | Business logic dịch vụ |
| `VehicleGroupService.java` | Business logic nhóm |
| `VehicleServiceRepository.java` | Data access dịch vụ |
| `VehicleGroupRepository.java` | Data access nhóm |
| `VehicleHistoryRepository.java` | Data access lịch sử |

#### 🔌 API Endpoints
```
GET    /api/vehicleservices/all           - Lấy tất cả dịch vụ (UC14.1)
POST   /api/vehicleservices/create        - Tạo dịch vụ (UC14.2)
PUT    /api/vehicleservices/update/{id}   - Cập nhật dịch vụ (UC14.3)
GET    /api/vehicleservices/by-status/{status} - Lọc theo trạng thái
GET    /api/vehicleservices/requests     - Yêu cầu chờ xử lý (UC14.4)
GET    /api/vehicleservices/vehicles     - Lấy danh sách xe
GET    /api/vehiclegroups/all            - Lấy tất cả nhóm (UC11.1)
POST   /api/vehiclegroups/create         - Tạo nhóm (UC11.2)
PUT    /api/vehiclegroups/update/{id}    - Sửa nhóm (UC11.4)
DELETE /api/vehiclegroups/{id}           - Xóa nhóm (UC11.3)
GET    /api/vehiclehistory/by-group/{id} - Lịch sử (UC11.5)
```

#### 📊 Database Tables
- `vehicle` - Thông tin xe
- `vehicleservice` - Dịch vụ xe
- `vehiclegroup` - Nhóm xe đồng sở hữu
- `vehiclehistory` - Lịch sử sử dụng

---

### 3️⃣ ui-service (Port 8080)

#### 📁 Controllers
| File | Chức Năng | Routes |
|------|-----------|---------|
| `HomeController.java` | Quản lý routes chính | 4 routes |
| `CheckinoutUIController.java` | UC13 - Check-in/out | 1 route |
| `VehicleServiceUIController.java` | UC14 - Dịch vụ | 1 route |

#### 🌐 Routes
```
/admin/checkin-checkout      → UC13 - Quản lý check-in/check-out
/admin/vehicle-service       → UC14 - Quản lý dịch vụ xe
/admin/vehicle-group         → UC11 - Quản lý nhóm xe
/admin/enhanced-contract     → UC12 - Quản lý hợp đồng
/admin/staff-management      → Quản lý nhân viên
/admin/vehicle-manager       → Quản lý xe
```

#### 📄 Templates (6 files)
- `checkin-checkout.html` - UI check-in/check-out
- `vehicle-service-management.html` - UI quản lý dịch vụ xe
- `vehicle-group-management.html` - UI quản lý nhóm xe
- `enhanced-contract-management.html` - UI hợp đồng điện tử
- `staff-management.html` - UI quản lý nhân viên
- `vehicle-manager.html` - UI quản lý xe

#### 🎨 CSS Files (4 files)
- `checkin-checkout.css` - Style cho check-in/out
- `vehicle-service-management.css` - Style quản lý dịch vụ
- `enhanced-contract.css` - **Professional admin style**
- Các file khác cho existing pages

#### 💻 JavaScript Files (4 files)
- `checkin-checkout.js` - QR scanner, signature, API calls
- `vehicle-service-management.js` - CRUD dịch vụ xe
- `vehicle-group-management.js` - CRUD nhóm xe, lịch sử
- `enhanced-contract-management.js` - CRUD hợp đồng, ký điện tử

## 🚀 HƯỚNG DẪN CHẠY

### Bước 1: Chuẩn Bị
```bash
# Yêu cầu hệ thống
- Java 17+
- Maven 3.8+
- MySQL 8.0+
```

### Bước 2: Setup Database
```batch
# Chạy script tự động
database\run_all.bat
```

Hoặc thủ công:
```bash
mysql -u root -p < database/create_schema.sql
mysql -u root -p < database/seed_data.sql
```

### Bước 3: Cấu Hình
Cập nhật passwords trong `application.properties` của mỗi service.

### Bước 4: Start Services
```bash
# Terminal 1 - LegalContractService
cd LegalContractService
mvn spring-boot:run
# Port: 8082

# Terminal 2 - VehicleServiceManagementService
cd VehicleServiceManagementService
mvn spring-boot:run
# Port: 8083

# Terminal 3 - UI Service
cd ui-service
mvn spring-boot:run
# Port: 8080
```

### Bước 5: Truy Cập
- UI: http://localhost:8080
- Check-in/out: http://localhost:8080/admin/checkin-checkout
- Vehicle Service: http://localhost:8080/admin/vehicle-service
- Vehicle Group: http://localhost:8080/admin/vehicle-group
- Contract: http://localhost:8080/admin/enhanced-contract

## 📋 USE CASES ĐÃ HOÀN THÀNH

✅ UC11: Quản lý nhóm xe đồng sở hữu  
✅ UC12: Quản lý hợp đồng pháp lý điện tử  
✅ UC13: Quản lý Check-in/Check-out  
✅ UC14: Quản lý dịch vụ xe  

## ✅ CHECKLIST HOÀN THÀNH

- [x] Không trùng URL
- [x] CSS chuyên nghiệp
- [x] Files không cần thiết đã xóa
- [x] CRUD operations đầy đủ
- [x] API endpoints hoàn chỉnh
- [x] Database schema đầy đủ
- [x] Dữ liệu mẫu đầy đủ
- [x] Script tự động setup

## 📞 SUPPORT

Files quan trọng:
- `PROJECT_SUMMARY.md` - Tổng hợp dự án
- `database/README.md` - Hướng dẫn database
- `database/SETUP_INSTRUCTIONS.md` - Hướng dẫn setup
- `database/create_schema.sql` - Tạo schema
- `database/seed_data.sql` - Dữ liệu mẫu
- `database/run_all.bat` - Script tự động

**Dự án đã sẵn sàng chạy!** 🚀
