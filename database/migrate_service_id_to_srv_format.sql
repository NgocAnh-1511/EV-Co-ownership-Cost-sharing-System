-- =====================================================
-- MIGRATE SERVICE_ID SANG FORMAT SRV001, SRV002, ...
-- Script này sẽ:
-- 1. Kiểm tra dữ liệu hiện tại
-- 2. Tạo service_id mới theo format SRV001, SRV002, ...
-- 3. Cập nhật foreign key trong vehicleservice
-- =====================================================

USE vehicle_management;

-- Bước 1: Kiểm tra dữ liệu hiện tại
SELECT '=== KIỂM TRA DỮ LIỆU HIỆN TẠI ===' AS step;

SELECT 'Service table:' AS info;
SELECT * FROM service ORDER BY service_id;

SELECT 'Vehicleservice table:' AS info;
SELECT * FROM vehicleservice ORDER BY service_id;

-- Bước 2: Backup dữ liệu
SELECT '=== BACKUP DỮ LIỆU ===' AS step;

CREATE TABLE IF NOT EXISTS service_backup AS SELECT * FROM service;
CREATE TABLE IF NOT EXISTS vehicleservice_backup AS SELECT * FROM vehicleservice;

SELECT 'Đã backup dữ liệu!' AS result;

-- Bước 3: Tạo bảng tạm để mapping service_id cũ -> mới
SELECT '=== TẠO MAPPING TABLE ===' AS step;

CREATE TEMPORARY TABLE service_id_mapping (
    old_service_id VARCHAR(20),
    new_service_id VARCHAR(20),
    PRIMARY KEY (old_service_id)
);

-- Bước 4: Tạo service_id mới cho các service hiện tại
SELECT '=== TẠO SERVICE_ID MỚI ===' AS step;

-- Đếm số service hiện tại
SET @service_count = (SELECT COUNT(*) FROM service);
SELECT CONCAT('Tổng số service: ', @service_count) AS info;

-- Lấy service_id lớn nhất có prefix SRV (nếu có)
SET @max_srv_id = (SELECT MAX(service_id) FROM service WHERE service_id LIKE 'SRV%' AND LENGTH(service_id) = 7);

-- Xác định số bắt đầu
SET @start_number = IF(@max_srv_id IS NULL, 1, 
    CAST(SUBSTRING(@max_srv_id, 4) AS UNSIGNED) + 1);

SELECT CONCAT('Bắt đầu từ SRV', LPAD(@start_number, 3, '0')) AS info;

-- Tạo mapping: service_id cũ -> service_id mới
-- Sử dụng biến để đếm
SET @row_number = 0;

INSERT INTO service_id_mapping (old_service_id, new_service_id)
SELECT 
    service_id AS old_service_id,
    CONCAT('SRV', LPAD(@start_number + (@row_number := @row_number + 1) - 1, 3, '0')) AS new_service_id
FROM service
WHERE service_id NOT LIKE 'SRV%' OR service_id NOT REGEXP '^SRV[0-9]{3}$'
ORDER BY 
    CASE 
        WHEN service_id REGEXP '^[0-9]+$' THEN CAST(service_id AS UNSIGNED)
        ELSE 999999
    END,
    service_id;

-- Hiển thị mapping
SELECT 'Mapping service_id cũ -> mới:' AS info;
SELECT * FROM service_id_mapping;

-- Bước 5: Cập nhật service_id trong bảng service
SELECT '=== CẬP NHẬT SERVICE_ID TRONG BẢNG SERVICE ===' AS step;

-- Tắt foreign key checks tạm thời
SET FOREIGN_KEY_CHECKS = 0;

-- Cập nhật service_id trong bảng service
UPDATE service s
INNER JOIN service_id_mapping m ON s.service_id = m.old_service_id
SET s.service_id = m.new_service_id;

-- Bật lại foreign key checks
SET FOREIGN_KEY_CHECKS = 1;

-- Bước 6: Cập nhật service_id trong bảng vehicleservice
SELECT '=== CẬP NHẬT SERVICE_ID TRONG BẢNG VEHICLESERVICE ===' AS step;

SET FOREIGN_KEY_CHECKS = 0;

-- Cập nhật service_id trong bảng vehicleservice
UPDATE vehicleservice vs
INNER JOIN service_id_mapping m ON vs.service_id = m.old_service_id
SET vs.service_id = m.new_service_id;

SET FOREIGN_KEY_CHECKS = 1;

-- Bước 7: Kiểm tra kết quả
SELECT '=== KIỂM TRA KẾT QUẢ ===' AS step;

SELECT 'Service table sau khi migrate:' AS info;
SELECT * FROM service ORDER BY service_id;

SELECT 'Vehicleservice table sau khi migrate:' AS info;
SELECT service_id, vehicle_id, service_name, status 
FROM vehicleservice 
ORDER BY service_id;

-- Bước 8: Kiểm tra foreign key constraints
SELECT '=== KIỂM TRA FOREIGN KEY ===' AS step;

SELECT 
    'Foreign key check:' AS info,
    COUNT(*) AS orphaned_records
FROM vehicleservice vs
LEFT JOIN service s ON vs.service_id = s.service_id
WHERE s.service_id IS NULL;

-- Nếu có orphaned records, hiển thị cảnh báo
SELECT 
    CASE 
        WHEN (SELECT COUNT(*) FROM vehicleservice vs LEFT JOIN service s ON vs.service_id = s.service_id WHERE s.service_id IS NULL) > 0
        THEN '⚠️ CẢNH BÁO: Có records trong vehicleservice không khớp với service!'
        ELSE '✅ Tất cả foreign keys đều hợp lệ!'
    END AS result;

SELECT '=== HOÀN TẤT ===' AS step;
SELECT 'Đã migrate service_id sang format SRV001, SRV002, ...!' AS result;

