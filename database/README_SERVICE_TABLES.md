# 📋 HƯỚNG DẪN BẢNG SERVICE VÀ VEHICLESERVICE

## 🗄️ Cấu Trúc Bảng

### 1. Bảng `service` (Danh mục dịch vụ)

Bảng này lưu danh sách các dịch vụ có sẵn trong hệ thống.

```sql
CREATE TABLE service (
    service_id VARCHAR(20) PRIMARY KEY,      -- ID dịch vụ (VD: SRV001, SRV002)
    service_name VARCHAR(255) NOT NULL       -- Tên dịch vụ (VD: "Bảo dưỡng định kỳ")
);
```

**Ví dụ dữ liệu:**
- `SRV001` - "Bảo dưỡng định kỳ"
- `SRV002` - "Thay dầu động cơ"
- `SRV003` - "Sửa chữa động cơ"

### 2. Bảng `vehicleservice` (Đăng ký dịch vụ cho xe)

Bảng này lưu thông tin các đăng ký dịch vụ của người dùng cho xe.

```sql
CREATE TABLE vehicleservice (
    registration_id INT AUTO_INCREMENT PRIMARY KEY,  -- ID đăng ký (tự động tăng)
    service_id VARCHAR(20) NOT NULL,                 -- FK đến bảng service
    vehicle_id VARCHAR(20) NOT NULL,                 -- FK đến bảng vehicle
    service_name VARCHAR(255),                       -- Tên dịch vụ (lưu từ service)
    service_description TEXT,                        -- Mô tả chi tiết
    service_type VARCHAR(50),                        -- Loại dịch vụ (Bảo dưỡng, Sửa chữa, ...)
    request_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP, -- Ngày đăng ký
    status VARCHAR(50) DEFAULT 'pending',            -- Trạng thái (pending, in_progress, completed)
    completion_date TIMESTAMP NULL,                  -- Ngày hoàn thành
    FOREIGN KEY (service_id) REFERENCES service(service_id),
    FOREIGN KEY (vehicle_id) REFERENCES vehicle(vehicle_id)
);
```

## 🔗 Quan Hệ

- `vehicleservice.service_id` → `service.service_id` (Many-to-One)
- `vehicleservice.vehicle_id` → `vehicle.vehicle_id` (Many-to-One)

## 📝 Quy Trình Đăng Ký Dịch Vụ

1. **Người dùng chọn xe**: Chọn `vehicle_id` từ danh sách xe
2. **Người dùng chọn dịch vụ**: Chọn `service_id` từ danh sách dịch vụ (bảng `service`)
3. **Nhập thông tin bổ sung**:
   - `service_description`: Mô tả chi tiết (tùy chọn)
   - `service_type`: Loại dịch vụ (Bảo dưỡng, Sửa chữa, Vệ sinh, ...)
4. **Hệ thống tự động**:
   - Lấy `service_name` từ bảng `service` dựa trên `service_id`
   - Set `request_date` = thời gian hiện tại
   - Set `status` = "pending" (mặc định)

## 🚀 Cách Sử Dụng

### Chèn dữ liệu vào bảng service:
```sql
INSERT INTO service (service_id, service_name) VALUES
('SRV001', 'Bảo dưỡng định kỳ'),
('SRV002', 'Thay dầu động cơ'),
('SRV003', 'Sửa chữa động cơ');
```

### Đăng ký dịch vụ (tự động qua API):
```json
POST /api/vehicleservices
{
    "serviceId": "SRV001",
    "vehicleId": "VEH001",
    "serviceDescription": "Thay dầu và kiểm tra động cơ",
    "serviceType": "Bảo dưỡng"
}
```

## ⚠️ Lưu Ý

1. **service_id** trong bảng `service` phải là VARCHAR(20) và là PRIMARY KEY
2. **service_id** trong bảng `vehicleservice` là FOREIGN KEY, phải tồn tại trong bảng `service`
3. **vehicle_id** trong bảng `vehicleservice` là FOREIGN KEY, phải tồn tại trong bảng `vehicle`
4. **registration_id** là PRIMARY KEY tự động tăng, không cần nhập
5. **request_date** tự động set = CURRENT_TIMESTAMP khi tạo mới
6. **status** mặc định = "pending" nếu không chỉ định

## 🔧 Sửa Lỗi Schema

Nếu bảng đã tồn tại và cần sửa lại, chạy file:
```bash
mysql -u root -p < database/fix_vehicleservice_schema.sql
```

Hoặc trong MySQL Workbench, mở file `database/fix_vehicleservice_schema.sql` và chạy.



