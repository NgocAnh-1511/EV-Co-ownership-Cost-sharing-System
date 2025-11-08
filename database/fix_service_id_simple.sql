-- =====================================================
-- FIX SERVICE_ID - SCRIPT ĐƠN GIẢN
-- Sửa service_id từ số sang format SRV001, SRV002, ...
-- =====================================================

USE vehicle_management;

-- Bước 1: Kiểm tra dữ liệu hiện tại
SELECT '=== KIỂM TRA DỮ LIỆU ===' AS step;

SELECT 'Service table:' AS info;
SELECT service_id, service_name, service_type FROM service ORDER BY service_id;

SELECT 'Vehicleservice table:' AS info;
SELECT service_id, vehicle_id, service_name, status FROM vehicleservice ORDER BY service_id;

-- Bước 2: Tạo bảng backup
SELECT '=== BACKUP ===' AS step;
CREATE TABLE IF NOT EXISTS service_backup AS SELECT * FROM service;
CREATE TABLE IF NOT EXISTS vehicleservice_backup AS SELECT * FROM vehicleservice;
SELECT 'Đã backup!' AS result;

-- Bước 3: Tìm service_id lớn nhất có format SRV
SELECT '=== TÌM SERVICE_ID LỚN NHẤT ===' AS step;
SET @max_srv = (SELECT MAX(CAST(SUBSTRING(service_id, 4) AS UNSIGNED)) 
                FROM service 
                WHERE service_id LIKE 'SRV%' 
                AND LENGTH(service_id) = 7);
SET @next_number = IFNULL(@max_srv, 0) + 1;
SELECT CONCAT('Service ID tiếp theo: SRV', LPAD(@next_number, 3, '0')) AS info;

-- Bước 4: Tạo service_id mới cho các service chưa có format SRV
SELECT '=== TẠO SERVICE_ID MỚI ===' AS step;

SET FOREIGN_KEY_CHECKS = 0;

-- Tạo bảng tạm để mapping
DROP TEMPORARY TABLE IF EXISTS temp_mapping;
CREATE TEMPORARY TABLE temp_mapping (
    old_id VARCHAR(20),
    new_id VARCHAR(20),
    row_num INT AUTO_INCREMENT PRIMARY KEY
);

-- Insert mapping cho các service không có format SRV
INSERT INTO temp_mapping (old_id)
SELECT service_id
FROM service
WHERE service_id NOT LIKE 'SRV%' 
   OR service_id NOT REGEXP '^SRV[0-9]{3}$'
ORDER BY 
    CASE 
        WHEN service_id REGEXP '^[0-9]+$' THEN CAST(service_id AS UNSIGNED)
        ELSE 999999
    END;

-- Cập nhật new_id
UPDATE temp_mapping
SET new_id = CONCAT('SRV', LPAD(@next_number + row_num - 1, 3, '0'));

-- Hiển thị mapping
SELECT 'Mapping:' AS info;
SELECT * FROM temp_mapping;

-- Cập nhật service table
UPDATE service s
INNER JOIN temp_mapping m ON s.service_id = m.old_id
SET s.service_id = m.new_id;

-- Cập nhật vehicleservice table
UPDATE vehicleservice vs
INNER JOIN temp_mapping m ON vs.service_id = m.old_id
SET vs.service_id = m.new_id;

SET FOREIGN_KEY_CHECKS = 1;

-- Bước 5: Kiểm tra kết quả
SELECT '=== KẾT QUẢ ===' AS step;

SELECT 'Service table sau khi fix:' AS info;
SELECT service_id, service_name, service_type FROM service ORDER BY service_id;

SELECT 'Vehicleservice table sau khi fix:' AS info;
SELECT service_id, vehicle_id, service_name, status FROM vehicleservice ORDER BY service_id;

-- Kiểm tra foreign key
SELECT 
    CASE 
        WHEN COUNT(*) = 0 THEN '✅ Tất cả foreign keys đều hợp lệ!'
        ELSE CONCAT('⚠️ Có ', COUNT(*), ' records không khớp!')
    END AS result
FROM vehicleservice vs
LEFT JOIN service s ON vs.service_id = s.service_id
WHERE s.service_id IS NULL;

SELECT '=== HOÀN TẤT ===' AS step;

