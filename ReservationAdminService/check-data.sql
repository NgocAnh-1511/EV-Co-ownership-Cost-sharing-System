-- ============================================
-- Script kiểm tra dữ liệu trong 2 databases
-- ============================================

-- Kiểm tra booking database (nguồn)
USE co_ownership_booking;

SELECT '=== BOOKING DATABASE ===' AS '';
SELECT 'Vehicles:' AS '';
SELECT COUNT(*) AS total_vehicles FROM vehicles;
SELECT * FROM vehicles LIMIT 5;

SELECT 'Reservations:' AS '';
SELECT COUNT(*) AS total_reservations FROM reservations;
SELECT * FROM reservations LIMIT 5;

SELECT 'Groups:' AS '';
SELECT COUNT(*) AS total_groups FROM `groups`;
SELECT * FROM `groups` LIMIT 5;

-- Kiểm tra admin database (đích)
USE co_ownership_booking_admin;

SELECT '=== ADMIN DATABASE ===' AS '';
SELECT 'Vehicles:' AS '';
SELECT COUNT(*) AS total_vehicles FROM vehicles;
SELECT * FROM vehicles LIMIT 5;

SELECT 'Reservations:' AS '';
SELECT COUNT(*) AS total_reservations FROM reservations;
SELECT * FROM reservations LIMIT 5;

SELECT 'Groups:' AS '';
SELECT COUNT(*) AS total_groups FROM `groups`;
SELECT * FROM `groups` LIMIT 5;

-- So sánh số lượng
SELECT '=== COMPARISON ===' AS '';
SELECT 
    (SELECT COUNT(*) FROM co_ownership_booking.vehicles) AS booking_vehicles,
    (SELECT COUNT(*) FROM co_ownership_booking_admin.vehicles) AS admin_vehicles,
    (SELECT COUNT(*) FROM co_ownership_booking.vehicles) - (SELECT COUNT(*) FROM co_ownership_booking_admin.vehicles) AS difference_vehicles;

SELECT 
    (SELECT COUNT(*) FROM co_ownership_booking.reservations) AS booking_reservations,
    (SELECT COUNT(*) FROM co_ownership_booking_admin.reservations) AS admin_reservations,
    (SELECT COUNT(*) FROM co_ownership_booking.reservations) - (SELECT COUNT(*) FROM co_ownership_booking_admin.reservations) AS difference_reservations;























