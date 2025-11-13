# CarRental - MicroServices System

Hệ thống quản lý xe thuê đồng sở hữu sử dụng kiến trúc microservices.

## 🏗️ Cấu Trúc Dự Án

### Services

#### 1. LegalContractService (Port 8082)
**Database**: `legal_contract`

**APIs**:
- `GET /api/checkinout/all` - Lấy tất cả check-in/out
- `POST /api/checkinout/checkin` - Check-in xe
- `POST /api/checkinout/checkout` - Check-out xe
- `GET /api/legalcontracts/all` - Lấy tất cả hợp đồng
- `POST /api/legalcontracts/create` - Tạo hợp đồng mới
- `PUT /api/legalcontracts/sign/{id}` - Ký hợp đồng

#### 2. VehicleServiceManagementService (Port 8083)
**Database**: `vehicle_management`

**APIs**:
- `GET /api/vehicleservices/all` - Lấy tất cả dịch vụ
- `POST /api/vehicleservices/create` - Tạo dịch vụ
- `GET /api/vehiclegroups/all` - Lấy tất cả nhóm xe
- `POST /api/vehiclegroups/create` - Tạo nhóm xe
- `GET /api/vehiclehistory/by-group/{id}` - Lịch sử

#### 3. ui-service (Port 8080)
**Routes**:
- `/admin/checkin-checkout` - Quản lý check-in/out
- `/admin/vehicle-service` - Quản lý dịch vụ xe
- `/admin/vehicle-group` - Quản lý nhóm xe
- `/admin/enhanced-contract` - Hợp đồng điện tử

## 🚀 Cách Chạy

### Yêu Cầu
- Java 17+
- Maven
- MySQL 8.0+

### Bước 1: Tạo Databases
```sql
CREATE DATABASE legal_contract;
CREATE DATABASE vehicle_management;
```

### Bước 2: Cấu Hình MySQL
Cập nhật passwords trong `application.properties` của mỗi service.

### Bước 3: Start Services
```bash
# Terminal 1 - LegalContractService
cd LegalContractService
mvn spring-boot:run

# Terminal 2 - VehicleServiceManagementService
cd VehicleServiceManagementService
mvn spring-boot:run

# Terminal 3 - UI Service
cd ui-service
mvn spring-boot:run
```

### Bước 4: Truy Cập
- UI: http://localhost:8080
- Check-in/out: http://localhost:8080/admin/checkin-checkout
- Vehicle Service: http://localhost:8080/admin/vehicle-service
- Vehicle Group: http://localhost:8080/admin/vehicle-group
- Contract: http://localhost:8080/admin/enhanced-contract

## 📋 Use Cases

### UC11: Quản lý nhóm xe đồng sở hữu
- Xem danh sách nhóm xe
- Thêm/Xóa/Sửa nhóm xe
- Quản lý lịch sử sử dụng

### UC12: Quản lý hợp đồng pháp lý điện tử
- Xem/Tạo/Sửa hợp đồng
- Ký hợp đồng điện tử
- Lưu trữ hợp đồng

### UC13: Quản lý Check-in/Check-out
- Quét QR code
- Kiểm tra tình trạng xe
- Ký số khi trả xe
- Ghi nhận thời gian

### UC14: Quản lý dịch vụ xe
- Xem dịch vụ
- Đặt dịch vụ
- Cập nhật trạng thái
- Quản lý yêu cầu

## 📁 File Structure

```
ui-service/
├── templates/
│   ├── checkin-checkout.html
│   ├── vehicle-service-management.html
│   ├── vehicle-group-management.html
│   ├── enhanced-contract-management.html
│   └── fragments/
├── static/
│   ├── css/ (4 files)
│   └── js/ (4 files)
└── java/
    ├── controller/ (3 files)
    └── model/ (4 DTOs)
```

## 🛠️ Tech Stack

- **Backend**: Spring Boot, JPA, MySQL
- **Frontend**: Thymeleaf, JavaScript, CSS3
- **Architecture**: Microservices
- **Ports**: 8080, 8082, 8083

## ⚙️ Configuration

Ports:
- UI Service: 8080
- LegalContract Service: 8082
- Vehicle Management Service: 8083

Databases:
- legal_contract
- vehicle_management




