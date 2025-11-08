-- =====================================================
-- ALTER BẢNG VEHICLESERVICE ĐỂ PHÙ HỢP VỚI CẤU TRÚC MỚI
-- =====================================================

USE vehicle_management;

-- Nếu bảng vehicleservice đã tồn tại, cần alter
-- Lưu ý: Chỉ chạy script này nếu bảng đã có dữ liệu và cần migrate

-- Bước 1: Thêm cột registration_id nếu chưa có
ALTER TABLE vehicleservice 
ADD COLUMN IF NOT EXISTS registration_id INT AUTO_INCREMENT PRIMARY KEY FIRST;

-- Bước 2: Đổi tên cột service_id cũ thành service_id_new (nếu cần)
-- ALTER TABLE vehicleservice 
-- CHANGE COLUMN service_id old_service_id INT;

-- Bước 3: Thêm cột service_id mới (VARCHAR) để reference đến bảng service
ALTER TABLE vehicleservice 
ADD COLUMN IF NOT EXISTS service_id VARCHAR(20) AFTER registration_id;

-- Bước 4: Thêm foreign key constraint
ALTER TABLE vehicleservice
ADD CONSTRAINT fk_vehicleservice_service 
FOREIGN KEY (service_id) REFERENCES service(service_id);

-- Bước 5: Thêm foreign key constraint cho vehicle_id (nếu chưa có)
ALTER TABLE vehicleservice
ADD CONSTRAINT fk_vehicleservice_vehicle 
FOREIGN KEY (vehicle_id) REFERENCES vehicle(vehicle_id);

-- Nếu muốn xóa cột cũ (sau khi migrate dữ liệu xong):
-- ALTER TABLE vehicleservice DROP COLUMN old_service_id;

SELECT 'Bảng vehicleservice đã được cập nhật thành công!' AS result;



