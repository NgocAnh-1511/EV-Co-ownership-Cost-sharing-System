-- Thêm cột vehiclename vào bảng vehicle
-- Migration script để thêm cột tên xe vào bảng vehicle

USE vehicle_management;

-- Kiểm tra xem cột đã tồn tại chưa trước khi thêm
SET @dbname = DATABASE();
SET @tablename = "vehicle";
SET @columnname = "vehiclename";
SET @preparedStatement = (SELECT IF(
  (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE
      (TABLE_SCHEMA = @dbname)
      AND (TABLE_NAME = @tablename)
      AND (COLUMN_NAME = @columnname)
  ) > 0,
  "SELECT 'Column already exists.' AS result;",
  CONCAT("ALTER TABLE ", @tablename, " ADD COLUMN ", @columnname, " VARCHAR(100) NULL AFTER vehicle_number;")
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

-- Kiểm tra kết quả
SELECT 
    COLUMN_NAME,
    DATA_TYPE,
    CHARACTER_MAXIMUM_LENGTH,
    IS_NULLABLE,
    COLUMN_DEFAULT
FROM INFORMATION_SCHEMA.COLUMNS
WHERE 
    TABLE_SCHEMA = 'vehicle_management'
    AND TABLE_NAME = 'vehicle'
    AND COLUMN_NAME = 'vehiclename';




