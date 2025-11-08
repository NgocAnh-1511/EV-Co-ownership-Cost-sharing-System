-- =====================================================
-- KIỂM TRA DỮ LIỆU SERVICE HIỆN TẠI
-- =====================================================

USE vehicle_management;

-- Kiểm tra bảng service
SELECT '=== BẢNG SERVICE ===' AS info;
SELECT * FROM service ORDER BY service_id;

-- Kiểm tra bảng vehicleservice
SELECT '=== BẢNG VEHICLESERVICE ===' AS info;
SELECT 
    service_id,
    vehicle_id,
    service_name,
    service_type,
    status,
    request_date
FROM vehicleservice 
ORDER BY service_id;

-- Kiểm tra foreign key relationships
SELECT '=== KIỂM TRA FOREIGN KEY ===' AS info;
SELECT 
    vs.service_id AS vehicleservice_service_id,
    vs.vehicle_id,
    s.service_id AS service_service_id,
    s.service_name,
    CASE 
        WHEN s.service_id IS NULL THEN '❌ KHÔNG KHỚP'
        ELSE '✅ OK'
    END AS status
FROM vehicleservice vs
LEFT JOIN service s ON vs.service_id = s.service_id
ORDER BY vs.service_id;

-- Kiểm tra service_id không có format SRV
SELECT '=== SERVICE_ID KHÔNG CÓ FORMAT SRV ===' AS info;
SELECT * FROM service WHERE service_id NOT LIKE 'SRV%';

-- Đếm số lượng
SELECT '=== THỐNG KÊ ===' AS info;
SELECT 
    COUNT(*) AS total_services,
    COUNT(CASE WHEN service_id LIKE 'SRV%' THEN 1 END) AS srv_format_count,
    COUNT(CASE WHEN service_id NOT LIKE 'SRV%' THEN 1 END) AS non_srv_format_count
FROM service;

