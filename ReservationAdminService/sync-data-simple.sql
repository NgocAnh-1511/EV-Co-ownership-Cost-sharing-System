-- Script đồng bộ dữ liệu từ co_ownership_booking sang co_ownership_booking_admin

USE co_ownership_booking_admin;

-- Tắt foreign key checks tạm thời
SET FOREIGN_KEY_CHECKS = 0;

-- 1. Xóa dữ liệu cũ trong admin DB
DELETE FROM reservations;
DELETE FROM vehicles;

-- 2. Đồng bộ vehicles
INSERT INTO vehicles (id, vehicle_name, vehicle_type, license_plate, year, group_id, status)
SELECT vehicle_id as id, vehicle_name, vehicle_type, license_plate, year, group_id, status
FROM co_ownership_booking.vehicles;

-- 3. Đồng bộ reservations
INSERT INTO reservations (id, vehicle_id, user_id, start_time, end_time, purpose, status, created_at, start_datetime, end_datetime)
SELECT 
    reservation_id as id,
    vehicle_id, 
    user_id, 
    start_datetime as start_time, 
    end_datetime as end_time,
    purpose,
    status,
    created_at,
    start_datetime,
    end_datetime
FROM co_ownership_booking.reservations;

-- 4. Kiểm tra kết quả
SELECT '=== VEHICLES ===' as Info;
SELECT COUNT(*) as total_vehicles FROM vehicles;

SELECT '=== RESERVATIONS ===' as Info;
SELECT COUNT(*) as total_reservations FROM reservations;

SELECT '=== LATEST 3 RESERVATIONS ===' as Info;
SELECT id, vehicle_id, user_id, status, start_time, end_time 
FROM reservations 
ORDER BY id DESC 
LIMIT 3;

-- Bật lại foreign key checks
SET FOREIGN_KEY_CHECKS = 1;

