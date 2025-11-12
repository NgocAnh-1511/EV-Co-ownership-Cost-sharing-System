-- Migration script để cập nhật constraints cho bảng vehicle
-- 1. Bỏ unique constraint trên group_id (cho phép nhiều xe không có nhóm)
-- 2. Cho phép group_id nullable
-- 3. Thêm unique constraint cho vehicle_number (không cho phép trùng biển số)

USE vehicle_management;

-- Bước 1: Xóa unique constraint cũ trên group_id (nếu tồn tại)
SET @constraint_name = 'uk_vehicle_group_id';
SET @table_name = 'vehicle';
SET @dbname = DATABASE();

-- Kiểm tra và xóa constraint nếu tồn tại
SET @sql = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS
     WHERE TABLE_SCHEMA = @dbname
       AND TABLE_NAME = @table_name
       AND CONSTRAINT_NAME = @constraint_name) > 0,
    CONCAT('ALTER TABLE ', @table_name, ' DROP INDEX ', @constraint_name, ';'),
    'SELECT "Constraint không tồn tại, bỏ qua." AS result;'
));
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Bước 2: Thay đổi group_id để cho phép NULL
ALTER TABLE vehicle_management.vehicle
MODIFY COLUMN group_id VARCHAR(20) NULL;

-- Bước 3: Kiểm tra và thêm unique constraint cho vehicle_number
-- Lưu ý: MySQL cho phép nhiều NULL trong unique column, nhưng không cho phép giá trị trùng lặp
SET @column_name = 'vehicle_number';
SET @constraint_name_vehicle_number = 'uk_vehicle_number';

-- Kiểm tra xem constraint đã tồn tại chưa
SET @sql2 = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS
     WHERE TABLE_SCHEMA = @dbname
       AND TABLE_NAME = @table_name
       AND CONSTRAINT_NAME = @constraint_name_vehicle_number) > 0,
    'SELECT "Unique constraint trên vehicle_number đã tồn tại, bỏ qua." AS result;',
    CONCAT('ALTER TABLE ', @table_name, ' ADD CONSTRAINT ', @constraint_name_vehicle_number, ' UNIQUE (vehicle_number);')
));
PREPARE stmt2 FROM @sql2;
EXECUTE stmt2;
DEALLOCATE PREPARE stmt2;

-- Bước 4: Kiểm tra dữ liệu trùng lặp trước khi thêm constraint
-- Nếu có dữ liệu trùng lặp, sẽ cần xử lý trước
SELECT 
    vehicle_number,
    COUNT(*) as count
FROM vehicle_management.vehicle
WHERE vehicle_number IS NOT NULL 
  AND vehicle_number != ''
GROUP BY vehicle_number
HAVING COUNT(*) > 1;

-- Nếu query trên trả về kết quả, có dữ liệu trùng lặp
-- Cần xử lý dữ liệu trùng lặp trước khi thêm unique constraint
-- Ví dụ: UPDATE vehicle SET vehicle_number = CONCAT(vehicle_number, '_', vehicle_id) WHERE vehicle_id IN (...);

-- Bước 5: Kiểm tra kết quả
SELECT 
    CONSTRAINT_NAME,
    CONSTRAINT_TYPE,
    TABLE_NAME
FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS
WHERE TABLE_SCHEMA = 'vehicle_management'
  AND TABLE_NAME = 'vehicle'
  AND CONSTRAINT_TYPE = 'UNIQUE';

-- Kiểm tra cấu trúc cột group_id
SELECT 
    COLUMN_NAME,
    IS_NULLABLE,
    DATA_TYPE,
    COLUMN_DEFAULT
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = 'vehicle_management'
  AND TABLE_NAME = 'vehicle'
  AND COLUMN_NAME IN ('group_id', 'vehicle_number');





