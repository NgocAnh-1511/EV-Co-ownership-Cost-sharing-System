# 📊 Database Seeding Scripts

## Mô Tả

Các file SQL này chèn dữ liệu mẫu vào 2 databases của hệ thống.

## Cấu Trúc Files

- `01_legal_contract_seed.sql` - Dữ liệu mẫu cho database legal_contract
- `02_vehicle_management_seed.sql` - Dữ liệu mẫu cho database vehicle_management

## Cách Chạy

### Option 1: MySQL Command Line
```bash
# Kết nối MySQL
mysql -u root -p

# Chạy script cho legal_contract
source D:/MicroService/database/01_legal_contract_seed.sql

# Chạy script cho vehicle_management
source D:/MicroService/database/02_vehicle_management_seed.sql
```

### Option 2: MySQL Workbench
1. Mở MySQL Workbench
2. Connect to server
3. Mở file `01_legal_contract_seed.sql`
4. Click "Execute" button
5. Lặp lại với `02_vehicle_management_seed.sql`

### Option 3: Copy & Paste
```bash
# Mở file SQL
cat database/01_legal_contract_seed.sql

# Copy toàn bộ nội dung
# Paste vào MySQL client và execute
```

## Dữ Liệu Mẫu

### legal_contract Database
- **checkinoutlog**: 5 records (check-in/check-out)
- **legalcontract**: 8 contracts (various statuses)
- **contractsignatures**: 3 signatures
- **contracthistory**: 5 history records

### vehicle_management Database
- **vehiclegroup**: 5 groups
- **vehicle**: 10 vehicles
- **vehicleservice**: 8 services (maintenance, repair, cleaning)
- **vehiclehistory**: 10 usage history records

## Dữ Liệu Chi Tiết

### Vehicles (10 xe)
- 2x Tesla Model 3 (30A-12345, 30B-67890)
- 2x BMW i3 (30C-11111, 30D-22222)
- 2x Nissan Leaf (30E-33333, 30F-44444)
- 2x Toyota Prius (30G-55555, 30H-66666)
- 2x Hyundai Kona (30I-77777, 30J-88888)

### Vehicle Groups (5 nhóm)
- Nhóm Tesla Model 3 (5 thành viên)
- Nhóm BMW i3 (3 thành viên)
- Nhóm Nissan Leaf (4 thành viên)
- Nhóm Toyota Prius (6 thành viên)
- Nhóm Hyundai Kona (4 thành viên)

### Services (8 dịch vụ)
- 3 Maintenance (pending, in_progress, completed)
- 2 Repair (pending, completed)
- 2 Cleaning (in_progress, completed)
- 1 Inspection (pending)

### Contracts (8 hợp đồng)
- 3 Draft
- 2 Pending
- 3 Signed
- 1 Archived

## Sau Khi Chạy

Bạn có thể:
- ✅ Xem danh sách xe với trạng thái đầy đủ
- ✅ Xem lịch sử check-in/check-out
- ✅ Quản lý dịch vụ xe với nhiều trạng thái
- ✅ Quản lý hợp đồng với các status khác nhau
- ✅ Xem lịch sử sử dụng xe theo nhóm

## Kiểm Tra Dữ Liệu

```sql
-- Legal Contract Database
SELECT * FROM checkinoutlog;
SELECT * FROM legalcontract;
SELECT * FROM contractsignatures;

-- Vehicle Management Database
SELECT * FROM vehiclegroup;
SELECT * FROM vehicle;
SELECT * FROM vehicleservice;
SELECT * FROM vehiclehistory;
```

## Reset Dữ Liệu

Để xóa dữ liệu mẫu:
```sql
USE legal_contract;
DELETE FROM checkinoutlog;
DELETE FROM legalcontract;
DELETE FROM contractsignatures;
DELETE FROM contracthistory;

USE vehicle_management;
DELETE FROM vehiclehistory;
DELETE FROM vehicleservice;
DELETE FROM vehicle;
DELETE FROM vehiclegroup;
```




