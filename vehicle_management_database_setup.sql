
DROP DATABASE IF EXISTS vehicle_management;
CREATE DATABASE vehicle_management;
USE vehicle_management;

-- ==========================================
-- TABLE DEFINITIONS
-- ==========================================

CREATE TABLE VehicleGroup (
    group_id INT PRIMARY KEY AUTO_INCREMENT,
    group_name VARCHAR(255) NOT NULL,
    description TEXT,
    creation_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE VehicleHistory (
    history_id INT PRIMARY KEY AUTO_INCREMENT,
    group_id INT NOT NULL,
    user_id INT NOT NULL,
    usage_start TIMESTAMP NOT NULL,
    usage_end TIMESTAMP NULL DEFAULT NULL,
    FOREIGN KEY (group_id) REFERENCES VehicleGroup(group_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE Vehicle (
    vehicle_id INT PRIMARY KEY AUTO_INCREMENT,
    group_id INT NOT NULL,
    vehicle_number VARCHAR(20) NOT NULL,
    vehiclename VARCHAR(100) NULL,
    vehicle_type VARCHAR(50) NOT NULL,
    status VARCHAR(50) DEFAULT 'available',
    FOREIGN KEY (group_id) REFERENCES VehicleGroup(group_id),
    UNIQUE KEY uk_vehicle_number (vehicle_number)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE VehicleService (
    service_id INT PRIMARY KEY AUTO_INCREMENT,
    vehicle_id INT NOT NULL,
    service_name VARCHAR(255) NOT NULL,
    service_description TEXT,
    service_type VARCHAR(50) NOT NULL,
    request_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(50) DEFAULT 'pending',
    completion_date TIMESTAMP NULL DEFAULT NULL,
    FOREIGN KEY (vehicle_id) REFERENCES Vehicle(vehicle_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ==========================================
-- SAMPLE DATA
-- ==========================================

INSERT INTO VehicleGroup (group_id, group_name, description, creation_date) VALUES
(1, 'EV Group Tesla Model 3', 'Tesla Model 3 AWD – nhóm test drive tại TP.HCM.', '2024-01-15 09:00:00'),
(2, 'EV Group BMW i3', 'Xe BMW i3 chuyên phục vụ tuyến nội thành.', '2024-02-10 08:30:00'),
(3, 'CarShare Saigon Rivian R1T', 'Dòng bán tải Rivian dùng cho nhóm Saigon.', '2024-03-05 10:45:00'),
(4, 'Đồng Sở Hữu VinFast VF8', 'Nhóm khách hàng trải nghiệm VF8 phiên bản Plus.', '2024-04-18 14:20:00'),
(5, 'EV Adventure Đà Lạt', 'Nhóm roadtrip Đà Lạt với đội xe SUV điện.', '2024-05-01 07:50:00');

INSERT INTO Vehicle (group_id, vehicle_number, vehiclename, vehicle_type, status) VALUES
(1, '51H-30303', 'Tesla Model 3 AWD', 'Sedan', 'available'),
(1, '51K-68686', 'Tesla Model 3 Performance', 'Sedan', 'maintenance'),
(2, '59A-88883', 'BMW i3 Urban Suite', 'Hatchback', 'available'),
(3, '51D-77770', 'Rivian R1T Launch Edition', 'Pickup', 'available'),
(4, '60A-12345', 'VinFast VF8 Plus', 'SUV', 'in_service'),
(5, '49A-56789', 'VinFast VF e34 Adventure', 'SUV', 'available');

INSERT INTO VehicleHistory (group_id, user_id, usage_start, usage_end) VALUES
(1, 101, '2024-10-01 08:00:00', '2024-10-01 18:00:00'),
(1, 102, '2024-10-03 07:30:00', '2024-10-03 19:15:00'),
(2, 201, '2024-10-05 09:00:00', NULL);

INSERT INTO VehicleService (vehicle_id, service_name, service_description, service_type, request_date, status, completion_date) VALUES
(1, 'Bảo dưỡng định kỳ', 'Kiểm tra tổng quát pin và hệ thống điện.', 'maintenance', '2024-10-02 10:30:00', 'completed', '2024-10-03 16:45:00'),
(1, 'Thay lốp trước', 'Lốp trước mòn, cần thay mới.', 'repair', '2024-10-04 11:00:00', 'in_progress', NULL),
(2, 'Kiểm định an toàn', 'Kiểm định an toàn cuối năm.', 'inspection', '2024-09-28 09:15:00', 'pending', NULL),
(3, 'Sửa chữa hệ thống phanh', 'Kiểm tra và sửa chữa hệ thống phanh.', 'repair', '2024-10-06 14:00:00', 'completed', '2024-10-08 10:20:00');

-- ==========================================
-- QUICK VIEWS
-- ==========================================

SELECT '=== VEHICLE GROUPS ===' AS section;
SELECT group_id, group_name, creation_date FROM VehicleGroup;

SELECT '=== VEHICLES ===' AS section;
SELECT vehicle_id, vehicle_number, vehicle_type, status FROM Vehicle;

SELECT '=== VEHICLE SERVICES ===' AS section;
SELECT service_id, vehicle_id, service_name, service_type, status FROM VehicleService;




