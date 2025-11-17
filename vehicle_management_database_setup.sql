
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

INSERT INTO VehicleGroup (group_name, description, creation_date) VALUES
('EV Fleet Alpha', 'Nhóm xe điện phục vụ nội thành và khách hàng doanh nghiệp.', '2024-01-15 09:00:00'),
('EV Fleet Beta', 'Nhóm xe điện phục vụ khách hàng khu vực ngoại thành.', '2024-02-10 08:30:00');

INSERT INTO Vehicle (group_id, vehicle_number, vehicle_type, status) VALUES
(1, 'EV-ALPHA-01', 'SUV', 'available'),
(1, 'EV-ALPHA-02', 'Sedan', 'in_service'),
(2, 'EV-BETA-01', 'Hatchback', 'maintenance');

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




