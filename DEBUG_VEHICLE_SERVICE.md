# 🔍 Debug Vehicle Service Manager

## Vấn Đề
Stats cards không hiển thị dữ liệu (hiển thị 0 hoặc không hiển thị).

## Cách Kiểm Tra

### 1. Kiểm Tra Logs
Khi truy cập `/admin/vehicle-manager`, kiểm tra console logs để xem:
- Có bao nhiêu dịch vụ được lấy từ API?
- Cấu trúc dữ liệu như thế nào?
- vehicleId có được lấy đúng không?
- serviceType có giá trị gì?

### 2. Kiểm Tra Database
```sql
USE vehicle_management;

-- Kiểm tra số lượng records trong vehicleservice
SELECT COUNT(*) as total_services FROM vehicleservice;

-- Kiểm tra dữ liệu
SELECT 
    v.vehicle_id,
    v.service_id,
    v.service_type,
    v.status,
    v.service_name
FROM vehicleservice v
LIMIT 10;

-- Đếm số xe distinct
SELECT COUNT(DISTINCT vehicle_id) as total_vehicles FROM vehicleservice;

-- Đếm số xe theo serviceType
SELECT 
    service_type,
    COUNT(DISTINCT vehicle_id) as vehicle_count
FROM vehicleservice
GROUP BY service_type;
```

### 3. Kiểm Tra API Response
```bash
# Test API trực tiếp
curl http://localhost:8083/api/vehicleservices

# Kiểm tra response structure
```

### 4. Các Vấn Đề Có Thể Xảy Ra

#### a. Không có dữ liệu trong bảng vehicleservice
- **Nguyên nhân**: Bảng vehicleservice trống
- **Giải pháp**: Thêm dữ liệu test hoặc đăng ký dịch vụ mới

#### b. vehicleId không được lấy đúng
- **Nguyên nhân**: Cấu trúc JSON response khác với expected
- **Giải pháp**: Kiểm tra logs để xem cấu trúc thực tế

#### c. serviceType không khớp
- **Nguyên nhân**: serviceType trong DB khác với điều kiện filter
- **Giải pháp**: Kiểm tra giá trị serviceType thực tế trong DB

#### d. API không trả về dữ liệu
- **Nguyên nhân**: Lỗi kết nối hoặc API không hoạt động
- **Giải pháp**: Kiểm tra service VehicleServiceManagementService có đang chạy không

## Logs Cần Kiểm Tra

Khi load trang, kiểm tra các log sau:
```
✅ Đã lấy X xe từ API
✅ Đã lấy Y dịch vụ từ API
🔍 Debug - Cấu trúc service đầu tiên:
   - Keys: ...
   - id: ...
   - vehicle: ...
   - serviceType: ...
📊 Tổng số xe (distinct): ...
📊 Bảo dưỡng: ... xe
📊 Kiểm tra: ... xe
📊 Sửa chữa: ... xe
```

## Giải Pháp

### Nếu không có dữ liệu:
1. Thêm dữ liệu test vào bảng vehicleservice
2. Đăng ký dịch vụ mới qua UI

### Nếu có dữ liệu nhưng không hiển thị:
1. Kiểm tra logs để xem cấu trúc dữ liệu
2. Sửa lại logic lấy vehicleId nếu cần
3. Kiểm tra serviceType có đúng format không

### Nếu API không trả về dữ liệu:
1. Kiểm tra VehicleServiceManagementService có đang chạy không
2. Kiểm tra URL API: `http://localhost:8083/api/vehicleservices`
3. Kiểm tra CORS configuration

