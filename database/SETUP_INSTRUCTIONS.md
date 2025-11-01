# 🗄️ HƯỚNG DẪN SETUP DATABASE

## 📁 Files Trong Thư Mục

1. **create_schema.sql** - Tạo databases và tables
2. **seed_data.sql** - Chèn dữ liệu mẫu
3. **run_all.bat** - Script tự động tạo cả schema và data
4. **run_seed.bat** - Script chỉ chạy seed data (đã có schema)

## 🚀 Cách Chạy

### Option 1: Tự Động (Khuyến Nghị)
```batch
# Double-click
database\run_all.bat
```

Script này sẽ:
1. Tạo databases
2. Tạo tables
3. Chèn dữ liệu mẫu

### Option 2: Thủ Công
```bash
# Bước 1: Tạo schema
mysql -u root -p < database/create_schema.sql

# Bước 2: Seed data
mysql -u root -p < database/seed_data.sql
```

## 📊 Cấu Trúc Database

### legal_contract Database
```
- checkinoutlog (10 fields)
- legalcontract (5 fields)
- contractsignatures (4 fields)
- contracthistory (4 fields)
```

### vehicle_management Database
```
- vehiclegroup (3 fields)
- vehicle (5 fields)
- vehicleservice (8 fields)
- vehiclehistory (5 fields)
```

## 📈 Dữ Liệu Mẫu

### legal_contract
- ✅ 8 hợp đồng (signed, pending, draft, archived)
- ✅ 6 lịch sử hợp đồng
- ✅ 4 chữ ký
- ✅ 5 check-in/out logs

### vehicle_management
- ✅ 5 nhóm xe (Sedan, SUV, City, Tesla, BMW)
- ✅ 10 xe với statuses khác nhau
- ✅ 5 dịch vụ (maintenance, repair, cleaning, inspection)
- ✅ 8 lịch sử sử dụng

## 🔍 Kiểm Tra

Sau khi chạy, kiểm tra bằng:
```sql
-- Legal Contract
USE legal_contract;
SELECT COUNT(*) FROM legalcontract;
SELECT COUNT(*) FROM checkinoutlog;

-- Vehicle Management
USE vehicle_management;
SELECT COUNT(*) FROM vehiclegroup;
SELECT COUNT(*) FROM vehicle;
SELECT COUNT(*) FROM vehicleservice;
```

## ⚠️ Lưu Ý

1. **Passwords**: Cập nhật passwords trong MySQL command
2. **Database exist**: Nếu databases đã tồn tại, sử dụng `run_seed.bat`
3. **Backup**: Nếu muốn backup, chạy trước:
   ```bash
   mysqldump -u root -p vehicle_management > backup.sql
   ```

## ✅ Sau Khi Setup

Bạn có thể:
- ✅ Start các services
- ✅ Truy cập http://localhost:8080
- ✅ Xem danh sách xe, hợp đồng, dịch vụ
- ✅ Test các chức năng CRUD
- ✅ Kiểm tra API endpoints

**Database đã sẵn sàng!** 🎉




