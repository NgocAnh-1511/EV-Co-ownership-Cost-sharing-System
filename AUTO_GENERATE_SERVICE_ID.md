# 🔢 Tự Động Generate Service ID - SRV001, SRV002, SRV003, ...

## ✅ Đã Thực Hiện

### 1. Repository Changes
- **File**: `ServiceRepository.java`
- **Thêm method**: `findMaxServiceIdWithPrefix()` - Lấy service_id lớn nhất có prefix "SRV"

### 2. Service Changes
- **File**: `ServiceService.java`
- **Thêm method**: `generateNextServiceId()` - Tự động tạo service_id mới theo format SRV001, SRV002, ...
- **Sửa method**: `addService()` - Tự động generate service_id nếu không được cung cấp

### 3. Controller Changes
- **File**: `ServiceAPI.java`
- **Sửa**: Cho phép không truyền `serviceId` (tự động generate)
- **Thêm endpoint**: `GET /api/services/next-id` - Lấy service_id tiếp theo sẽ được generate

### 4. Model Changes
- **File**: `ServiceType.java`
- **Sửa**: Bỏ `@NotBlank` từ `serviceId` (cho phép null để tự động generate)

## 🎯 Cách Sử Dụng

### 1. Tạo Service Mới (Không Cần Truyền serviceId)

**Request:**
```json
POST /api/services
{
  "serviceName": "Bảo dưỡng định kỳ",
  "serviceType": "maintenance"
}
```

**Response:**
```json
{
  "success": true,
  "message": "Thêm dịch vụ thành công",
  "data": {
    "serviceId": "SRV001",
    "serviceName": "Bảo dưỡng định kỳ",
    "serviceType": "maintenance",
    ...
  }
}
```

### 2. Tạo Service Mới (Với serviceId Tùy Chỉnh)

**Request:**
```json
POST /api/services
{
  "serviceId": "CUSTOM001",
  "serviceName": "Dịch vụ đặc biệt",
  "serviceType": "special"
}
```

**Response:**
```json
{
  "success": true,
  "message": "Thêm dịch vụ thành công",
  "data": {
    "serviceId": "CUSTOM001",
    "serviceName": "Dịch vụ đặc biệt",
    "serviceType": "special",
    ...
  }
}
```

### 3. Lấy Service ID Tiếp Theo

**Request:**
```
GET /api/services/next-id
```

**Response:**
```json
{
  "nextServiceId": "SRV004"
}
```

## 🔢 Logic Generate Service ID

### Format
- **Prefix**: `SRV`
- **Number**: 3 chữ số (001, 002, 003, ...)
- **Ví dụ**: `SRV001`, `SRV002`, `SRV003`, ..., `SRV999`

### Cách Hoạt Động

1. **Lần đầu tiên**: Nếu chưa có service nào, bắt đầu từ `SRV001`
2. **Các lần sau**: 
   - Tìm service_id lớn nhất có prefix "SRV"
   - Tách số từ service_id (ví dụ: "SRV003" -> 3)
   - Tăng lên 1 (3 + 1 = 4)
   - Format thành "SRV004"

### Ví Dụ

```
Database hiện tại:
- SRV001
- SRV002
- SRV003

Lần tạo tiếp theo: SRV004
```

## ⚠️ Lưu Ý

### 1. Service ID Tùy Chỉnh
- Bạn vẫn có thể tạo service với service_id tùy chỉnh (không cần prefix "SRV")
- Tuy nhiên, auto-generate chỉ tìm service_id có prefix "SRV"
- Ví dụ: Nếu có "CUSTOM001", auto-generate vẫn sẽ tìm "SRV003" và tạo "SRV004"

### 2. Xóa Service
- Khi xóa service, service_id không được tái sử dụng
- Ví dụ: Xóa SRV003, lần tạo tiếp theo vẫn là SRV004 (không quay lại SRV003)

### 3. Concurrent Requests
- Nếu có nhiều request đồng thời tạo service, có thể có race condition
- Khuyến nghị: Sử dụng transaction hoặc lock để tránh duplicate service_id

## 🧪 Test

### Test 1: Tạo Service Không Có serviceId
```bash
curl -X POST http://localhost:8083/api/services \
  -H "Content-Type: application/json" \
  -d '{
    "serviceName": "Test Service 1",
    "serviceType": "test"
  }'
```

### Test 2: Lấy Next Service ID
```bash
curl -X GET http://localhost:8083/api/services/next-id
```

### Test 3: Tạo Nhiều Service Liên Tiếp
```bash
# Tạo service 1
curl -X POST http://localhost:8083/api/services \
  -H "Content-Type: application/json" \
  -d '{"serviceName": "Service 1", "serviceType": "test"}'

# Tạo service 2
curl -X POST http://localhost:8083/api/services \
  -H "Content-Type: application/json" \
  -d '{"serviceName": "Service 2", "serviceType": "test"}'

# Tạo service 3
curl -X POST http://localhost:8083/api/services \
  -H "Content-Type: application/json" \
  -d '{"serviceName": "Service 3", "serviceType": "test"}'
```

**Kết quả mong đợi:**
- Service 1: `SRV001`
- Service 2: `SRV002`
- Service 3: `SRV003`

## 📝 Database Migration (Optional)

Nếu bạn muốn migrate các service_id cũ sang format mới:

```sql
-- Ví dụ: Migrate service_id cũ sang SRV format
UPDATE vehicle_management.service 
SET service_id = CONCAT('SRV', LPAD(ROW_NUMBER() OVER (ORDER BY created_date), 3, '0'))
WHERE service_id NOT LIKE 'SRV%';
```

**Lưu ý**: Chỉ chạy script này nếu bạn chắc chắn muốn thay đổi service_id hiện tại!

## ✅ Checklist

- [x] Thêm method `findMaxServiceIdWithPrefix()` vào Repository
- [x] Thêm method `generateNextServiceId()` vào Service
- [x] Sửa method `addService()` để tự động generate service_id
- [x] Sửa Controller để cho phép không truyền service_id
- [x] Sửa Model để bỏ validation @NotBlank cho service_id
- [x] Thêm endpoint `GET /api/services/next-id`
- [ ] Test tạo service không có service_id
- [ ] Test tạo nhiều service liên tiếp
- [ ] Test với service_id tùy chỉnh

