# Database Setup Guide - EV Co-ownership System

## Tổng quan Database

Hệ thống EV Co-ownership sử dụng **8 database riêng biệt** cho từng microservice để đảm bảo tính độc lập và khả năng mở rộng.

## Danh sách Database

| Service | Database Name | Port | Mô tả |
|---------|---------------|------|-------|
| User Service | `ev_coownership_user_db` | 3306 | Quản lý người dùng, xác thực, tỷ lệ sở hữu |
| Reservation Service | `ev_coownership_reservation_db` | 3307 | Đặt lịch, lịch sử sử dụng, phân tích AI |
| Cost Payment Service | `ev_coownership_payment_db` | 3308 | Chi phí, phân chia, thanh toán |
| Financial Reporting Service | `ev_coownership_financial_reporting_db` | 3309 | Báo cáo tài chính chi tiết |
| Group Management Service | `ev_coownership_group_db` | 3310 | Nhóm đồng sở hữu, bỏ phiếu |
| Vehicle Management Service | `ev_coownership_vehicle_db` | 3311 | Quản lý xe, dịch vụ, bảo dưỡng |
| Dispute Management Service | `ev_coownership_dispute_db` | 3312 | Tranh chấp, giám sát |
| Legal Contract Service | `ev_coownership_legal_db` | 3313 | Hợp đồng pháp lý, check-in/out |

## Cách Setup Database

### Phương pháp 1: Sử dụng Script tự động

#### Trên Linux/Mac:
```bash
chmod +x setup_database.sh
./setup_database.sh
```

#### Trên Windows:
```powershell
.\setup_database.ps1
```

### Phương pháp 2: Chạy thủ công

```bash
mysql -u root -p < database_setup.sql
```

### Phương pháp 3: Sử dụng Docker Compose

```bash
docker-compose up -d
```

Docker Compose sẽ tự động tạo tất cả database khi khởi động.

## Cấu trúc Database Chi tiết

### 1. User Service Database (`ev_coownership_user_db`)

**Tables:**
- `users` - Thông tin người dùng
- `user_authentication` - Xác thực và bảo mật
- `ownership_percentages` - Tỷ lệ sở hữu xe

**Key Features:**
- Quản lý thông tin cá nhân (CMND/CCCD, giấy phép lái xe)
- Xác thực và phân quyền
- Theo dõi tỷ lệ sở hữu theo thời gian

### 2. Reservation Service Database (`ev_coownership_reservation_db`)

**Tables:**
- `reservations` - Đặt lịch sử dụng xe
- `usage_history` - Lịch sử sử dụng chi tiết
- `usage_analytics` - Phân tích AI và gợi ý

**Key Features:**
- Hệ thống đặt lịch với ưu tiên công bằng
- Theo dõi quãng đường, năng lượng tiêu thụ
- AI phân tích mẫu sử dụng

### 3. Cost Payment Service Database (`ev_coownership_payment_db`)

**Tables:**
- `cost_categories` - Danh mục chi phí
- `cost_items` - Chi phí cụ thể
- `cost_splits` - Phân chia chi phí
- `payments` - Thanh toán
- `financial_reports` - Báo cáo tài chính

**Key Features:**
- Tự động phân chia chi phí theo tỷ lệ sở hữu/sử dụng
- Hỗ trợ nhiều phương thức thanh toán
- Báo cáo tài chính minh bạch

### 4. Financial Reporting Service Database (`ev_coownership_financial_reporting_db`)

**Tables:**
- `detailed_reports` - Báo cáo chi tiết
- `report_cost_breakdown` - Phân tích chi phí
- `report_exports` - Xuất báo cáo

**Key Features:**
- Báo cáo tài chính định kỳ (tháng/quý/năm)
- Phân tích chi phí theo danh mục
- Xuất báo cáo nhiều định dạng

### 5. Group Management Service Database (`ev_coownership_group_db`)

**Tables:**
- `co_ownership_groups` - Nhóm đồng sở hữu
- `group_members` - Thành viên nhóm
- `group_votes` - Cuộc bỏ phiếu
- `vote_options` - Lựa chọn phiếu bầu
- `vote_responses` - Phản hồi bỏ phiếu
- `group_funds` - Quỹ chung
- `fund_transactions` - Giao dịch quỹ

**Key Features:**
- Quản lý nhóm và thành viên
- Hệ thống bỏ phiếu và quyết định chung
- Quản lý quỹ chung minh bạch

### 6. Vehicle Management Service Database (`ev_coownership_vehicle_db`)

**Tables:**
- `vehicles` - Thông tin xe điện
- `vehicle_services` - Dịch vụ xe
- `maintenance_history` - Lịch sử bảo dưỡng
- `vehicle_status` - Trạng thái xe

**Key Features:**
- Quản lý thông tin xe điện chi tiết
- Lịch sử bảo dưỡng và dịch vụ
- Theo dõi trạng thái real-time

### 7. Dispute Management Service Database (`ev_coownership_dispute_db`)

**Tables:**
- `disputes` - Tranh chấp
- `dispute_tracking` - Theo dõi tranh chấp
- `dispute_monitoring` - Giám sát tự động

**Key Features:**
- Quản lý và theo dõi tranh chấp
- Giám sát tự động và cảnh báo
- Lịch sử xử lý tranh chấp

### 8. Legal Contract Service Database (`ev_coownership_legal_db`)

**Tables:**
- `legal_contracts` - Hợp đồng pháp lý
- `electronic_signatures` - Chữ ký điện tử
- `vehicle_check_in_out` - Check-in/out xe
- `contract_history` - Lịch sử hợp đồng

**Key Features:**
- Quản lý hợp đồng điện tử
- Chữ ký số và xác thực
- Check-in/out với QR code

## Sample Data

Script setup đã bao gồm dữ liệu mẫu:

### Users
- `user-001`: Nguyễn Văn Admin (Admin)
- `user-002`: Trần Thị Member (Member)
- `user-003`: Lê Văn Member (Member)

### Groups
- `EV Group Tesla Model 3`: Nhóm sở hữu Tesla Model 3
- `EV Group BMW i3`: Nhóm sở hữu BMW i3

### Vehicles
- `vehicle-001`: Tesla Model 3 (2023)
- `vehicle-002`: BMW i3 (2022)

### Cost Categories
- Phí sạc điện
- Bảo dưỡng định kỳ
- Bảo hiểm xe
- Đăng kiểm
- Vệ sinh xe

## Kết nối Database

### Từ Application
```properties
# Group Management Service
spring.datasource.url=jdbc:mysql://group-mysql:3306/ev_coownership_group_db?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=password

# Cost Payment Service
spring.datasource.url=jdbc:mysql://payment-mysql:3306/ev_coownership_payment_db?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=password
```

### Từ MySQL Client
```bash
# Kết nối đến từng database
mysql -h localhost -P 3306 -u root -p ev_coownership_user_db
mysql -h localhost -P 3307 -u root -p ev_coownership_reservation_db
mysql -h localhost -P 3308 -u root -p ev_coownership_payment_db
mysql -h localhost -P 3309 -u root -p ev_coownership_financial_reporting_db
mysql -h localhost -P 3310 -u root -p ev_coownership_group_db
mysql -h localhost -P 3311 -u root -p ev_coownership_vehicle_db
mysql -h localhost -P 3312 -u root -p ev_coownership_dispute_db
mysql -h localhost -P 3313 -u root -p ev_coownership_legal_db
```

## Service Users

Mỗi service có user riêng để bảo mật:

| Service | Username | Password |
|---------|----------|----------|
| User Service | `user_service` | `user_service_password` |
| Reservation Service | `reservation_service` | `reservation_service_password` |
| Cost Payment Service | `payment_service` | `payment_service_password` |
| Financial Reporting Service | `financial_service` | `financial_service_password` |
| Group Management Service | `group_service` | `group_service_password` |
| Vehicle Management Service | `vehicle_service` | `vehicle_service_password` |
| Dispute Management Service | `dispute_service` | `dispute_service_password` |
| Legal Contract Service | `legal_service` | `legal_service_password` |

## Backup và Restore

### Backup tất cả database
```bash
mysqldump -u root -p --all-databases > ev_coownership_backup.sql
```

### Backup database cụ thể
```bash
mysqldump -u root -p ev_coownership_group_db > group_db_backup.sql
mysqldump -u root -p ev_coownership_payment_db > payment_db_backup.sql
```

### Restore database
```bash
mysql -u root -p ev_coownership_group_db < group_db_backup.sql
```

## Monitoring và Maintenance

### Kiểm tra trạng thái database
```sql
SHOW DATABASES;
SHOW PROCESSLIST;
SHOW STATUS;
```

### Tối ưu hiệu suất
- Tất cả database đã có indexes cần thiết
- Sử dụng connection pooling trong ứng dụng
- Monitor slow queries và optimize

### Scaling
- Có thể tách database thành các instance riêng
- Sử dụng read replicas cho reporting
- Implement database sharding nếu cần

## Troubleshooting

### Lỗi kết nối
1. Kiểm tra MySQL service đang chạy
2. Kiểm tra port không bị conflict
3. Kiểm tra credentials đúng

### Lỗi permission
1. Kiểm tra user có quyền truy cập database
2. Grant lại permissions nếu cần

### Lỗi schema
1. Chạy lại script setup
2. Kiểm tra foreign key constraints
3. Verify table structures

## Next Steps

1. ✅ Database setup hoàn tất
2. 🔄 Cập nhật application.properties
3. 🚀 Chạy docker-compose up --build
4. 🧪 Test APIs và database connections
5. 📊 Monitor performance và optimize
