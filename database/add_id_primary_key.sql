-- Script để thêm cột id AUTO_INCREMENT làm primary key
-- Giải quyết vấn đề: Cho phép đăng ký cùng một dịch vụ (service_id) cho cùng một xe (vehicle_id) nhiều lần

USE vehicle_management;

-- ============================================
-- BƯỚC 1: KIỂM TRA CẤU TRÚC BẢNG HIỆN TẠI
-- ============================================
SELECT '=== CẤU TRÚC BẢNG VEHICLESERVICE TRƯỚC KHI SỬA ===' AS Info;
SELECT 
    COLUMN_NAME,
    COLUMN_TYPE,
    IS_NULLABLE,
    COLUMN_KEY,
    EXTRA
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = 'vehicle_management' 
  AND TABLE_NAME = 'vehicleservice'
ORDER BY ORDINAL_POSITION;

-- ============================================
-- BƯỚC 2: KIỂM TRA PRIMARY KEY HIỆN TẠI
-- ============================================
SELECT '=== PRIMARY KEY HIỆN TẠI ===' AS Info;
SELECT 
    CONSTRAINT_NAME,
    COLUMN_NAME,
    ORDINAL_POSITION
FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE
WHERE TABLE_SCHEMA = 'vehicle_management' 
  AND TABLE_NAME = 'vehicleservice'
  AND CONSTRAINT_NAME = 'PRIMARY'
ORDER BY ORDINAL_POSITION;

-- ============================================
-- BƯỚC 3: TẮT FOREIGN KEY CHECKS
-- ============================================
SET FOREIGN_KEY_CHECKS = 0;
SET SQL_SAFE_UPDATES = 0;

-- ============================================
-- BƯỚC 4: XÓA PRIMARY KEY CŨ (COMPOSITE KEY hoặc registration_id)
-- ============================================
SELECT '=== XÓA PRIMARY KEY CŨ ===' AS Info;
ALTER TABLE vehicleservice DROP PRIMARY KEY;

-- ============================================
-- BƯỚC 5: KIỂM TRA XEM CỘT id ĐÃ TỒN TẠI CHƯA
-- ============================================
SELECT '=== KIỂM TRA CỘT id ===' AS Info;
SET @col_exists = 0;
SELECT COUNT(*) INTO @col_exists 
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_SCHEMA = 'vehicle_management' 
  AND TABLE_NAME = 'vehicleservice' 
  AND COLUMN_NAME = 'id';

-- ============================================
-- BƯỚC 6: XÓA CỘT registration_id NẾU TỒN TẠI
-- ============================================
SELECT '=== XÓA CỘT registration_id NẾU TỒN TẠI ===' AS Info;
SET @reg_col_exists = 0;
SELECT COUNT(*) INTO @reg_col_exists 
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_SCHEMA = 'vehicle_management' 
  AND TABLE_NAME = 'vehicleservice' 
  AND COLUMN_NAME = 'registration_id';

SET @sql = IF(@reg_col_exists > 0, 
    'ALTER TABLE vehicleservice DROP COLUMN registration_id', 
    'SELECT "Cột registration_id không tồn tại" AS Message');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ============================================
-- BƯỚC 7: THÊM CỘT id NẾU CHƯA TỒN TẠI
-- ============================================
SELECT '=== THÊM CỘT id ===' AS Info;
SET @sql = IF(@col_exists > 0, 
    'SELECT "Cột id đã tồn tại, sẽ cập nhật..." AS Message', 
    'ALTER TABLE vehicleservice ADD COLUMN id INT NOT NULL AUTO_INCREMENT FIRST');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Nếu cột đã tồn tại, đảm bảo nó là AUTO_INCREMENT
-- Nếu cột chưa tồn tại, nó đã được tạo với AUTO_INCREMENT ở trên
SET @sql = IF(@col_exists > 0,
    'ALTER TABLE vehicleservice MODIFY COLUMN id INT NOT NULL AUTO_INCREMENT',
    'SELECT "Cột id đã được tạo mới với AUTO_INCREMENT" AS Message');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ============================================
-- BƯỚC 8: SET AUTO_INCREMENT START VALUE
-- ============================================
-- Lấy giá trị lớn nhất hiện tại (nếu có dữ liệu)
SET @max_id = (SELECT COALESCE(MAX(id), 0) FROM vehicleservice);
SET @next_id = @max_id + 1;
SET @sql = CONCAT('ALTER TABLE vehicleservice AUTO_INCREMENT = ', @next_id);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SELECT CONCAT('✅ Đã set AUTO_INCREMENT bắt đầu từ ', @next_id) AS Message;

-- ============================================
-- BƯỚC 9: TẠO PRIMARY KEY MỚI (id)
-- ============================================
SELECT '=== TẠO PRIMARY KEY MỚI (id) ===' AS Info;
ALTER TABLE vehicleservice ADD PRIMARY KEY (id);

-- ============================================
-- BƯỚC 10: TẠO INDEX CHO (service_id, vehicle_id) ĐỂ TỐI ƯU QUERY
-- ============================================
SELECT '=== TẠO INDEX CHO (service_id, vehicle_id) ===' AS Info;
-- Xóa index cũ nếu tồn tại
DROP INDEX IF EXISTS idx_service_vehicle ON vehicleservice;
-- Tạo index mới
CREATE INDEX idx_service_vehicle ON vehicleservice(service_id, vehicle_id);

-- ============================================
-- BƯỚC 11: BẬT LẠI FOREIGN KEY CHECKS
-- ============================================
SET FOREIGN_KEY_CHECKS = 1;
SET SQL_SAFE_UPDATES = 1;

-- ============================================
-- BƯỚC 12: KIỂM TRA KẾT QUẢ
-- ============================================
SELECT '=== KIỂM TRA CẤU TRÚC SAU KHI SỬA ===' AS Info;
SELECT 
    COLUMN_NAME,
    COLUMN_TYPE,
    IS_NULLABLE,
    COLUMN_KEY,
    EXTRA
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = 'vehicle_management' 
  AND TABLE_NAME = 'vehicleservice'
ORDER BY ORDINAL_POSITION;

SELECT '=== KIỂM TRA PRIMARY KEY SAU KHI SỬA ===' AS Info;
SELECT 
    CONSTRAINT_NAME,
    COLUMN_NAME,
    ORDINAL_POSITION
FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE
WHERE TABLE_SCHEMA = 'vehicle_management' 
  AND TABLE_NAME = 'vehicleservice'
  AND CONSTRAINT_NAME = 'PRIMARY'
ORDER BY ORDINAL_POSITION;

SELECT '=== KIỂM TRA INDEX ===' AS Info;
SHOW INDEX FROM vehicleservice;

SELECT '=== KIỂM TRA DỮ LIỆU ===' AS Info;
SELECT 
    id,
    service_id,
    vehicle_id,
    service_name,
    status,
    request_date
FROM vehicleservice
ORDER BY id DESC
LIMIT 10;

SELECT '✅ HOÀN TẤT! Đã thêm cột id làm primary key' AS Result;
SELECT '✅ Bây giờ có thể đăng ký cùng một dịch vụ (service_id) cho cùng một xe (vehicle_id) nhiều lần' AS Result;

