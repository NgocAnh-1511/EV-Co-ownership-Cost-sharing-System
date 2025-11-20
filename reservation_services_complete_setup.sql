-- =====================================================
-- SCRIPT TỔNG HỢP: TẠO DATABASE + CHÈN DỮ LIỆU MẪU
-- Hệ Thống EV Co-ownership - Reservation Services
-- Chạy script này MỘT LẦN trên MySQL Workbench
-- =====================================================

-- =====================================================
-- PHẦN 1: TẠO DATABASE
-- =====================================================

-- Xóa database cũ nếu tồn tại (tùy chọn - comment nếu không muốn xóa)
-- DROP DATABASE IF EXISTS co_ownership_booking;
-- DROP DATABASE IF EXISTS co_ownership_booking_admin;
-- DROP DATABASE IF EXISTS ai_ev;

-- Tạo database cho ReservationService
CREATE DATABASE IF NOT EXISTS co_ownership_booking 
CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

-- Tạo database cho ReservationAdminService
CREATE DATABASE IF NOT EXISTS co_ownership_booking_admin 
CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

-- Tạo database cho AIService
CREATE DATABASE IF NOT EXISTS ai_ev 
CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

-- Cấp quyền
GRANT ALL PRIVILEGES ON co_ownership_booking.* TO 'root'@'%';
GRANT ALL PRIVILEGES ON co_ownership_booking_admin.* TO 'root'@'%';
GRANT ALL PRIVILEGES ON ai_ev.* TO 'root'@'%';
FLUSH PRIVILEGES;

-- =====================================================
-- PHẦN 2: TẠO BẢNG - DATABASE co_ownership_booking (ReservationService)
-- =====================================================

USE co_ownership_booking;

-- Bảng users
CREATE TABLE IF NOT EXISTS users (
    user_id BIGINT NOT NULL AUTO_INCREMENT,
    full_name VARCHAR(255),
    email VARCHAR(255),
    phone VARCHAR(50),
    username VARCHAR(255),
    password VARCHAR(255),
    PRIMARY KEY (user_id),
    INDEX idx_username (username),
    INDEX idx_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Bảng vehicle_groups
CREATE TABLE IF NOT EXISTS vehicle_groups (
    group_id VARCHAR(20) NOT NULL,
    group_name VARCHAR(255),
    description TEXT,
    creation_date DATETIME(6),
    active VARCHAR(50),
    PRIMARY KEY (group_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Bảng vehicles
CREATE TABLE IF NOT EXISTS vehicles (
    vehicle_id BIGINT NOT NULL AUTO_INCREMENT,
    vehicle_name VARCHAR(255),
    license_plate VARCHAR(255),
    vehicle_type VARCHAR(255),
    group_id VARCHAR(20),
    status VARCHAR(50) DEFAULT 'AVAILABLE',
    PRIMARY KEY (vehicle_id),
    INDEX idx_group_id (group_id),
    INDEX idx_status (status),
    FOREIGN KEY (group_id) REFERENCES vehicle_groups(group_id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Bảng group_members
CREATE TABLE IF NOT EXISTS group_members (
    member_id BIGINT NOT NULL AUTO_INCREMENT,
    group_id VARCHAR(20) NOT NULL,
    user_id BIGINT NOT NULL,
    ownership_percentage DOUBLE,
    PRIMARY KEY (member_id),
    UNIQUE KEY uk_group_user (group_id, user_id),
    INDEX idx_group_id (group_id),
    INDEX idx_user_id (user_id),
    FOREIGN KEY (group_id) REFERENCES vehicle_groups(group_id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Bảng reservations
CREATE TABLE IF NOT EXISTS reservations (
    reservation_id BIGINT NOT NULL AUTO_INCREMENT,
    vehicle_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    start_datetime DATETIME(6),
    end_datetime DATETIME(6),
    purpose TEXT,
    status VARCHAR(50) DEFAULT 'BOOKED',
    PRIMARY KEY (reservation_id),
    INDEX idx_vehicle_id (vehicle_id),
    INDEX idx_user_id (user_id),
    INDEX idx_start_datetime (start_datetime),
    INDEX idx_end_datetime (end_datetime),
    INDEX idx_status (status),
    FOREIGN KEY (vehicle_id) REFERENCES vehicles(vehicle_id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Bảng reservation_checkpoints
CREATE TABLE IF NOT EXISTS reservation_checkpoints (
    checkpoint_id BIGINT NOT NULL AUTO_INCREMENT,
    reservation_id BIGINT NOT NULL,
    checkpoint_type VARCHAR(20) NOT NULL,
    status VARCHAR(20) DEFAULT 'PENDING',
    qr_token VARCHAR(128) NOT NULL,
    issued_by VARCHAR(30),
    issued_at DATETIME(6) DEFAULT CURRENT_TIMESTAMP,
    expires_at DATETIME(6),
    scanned_at DATETIME(6),
    signed_at DATETIME(6),
    signer_name VARCHAR(255),
    signer_id_number VARCHAR(50),
    signature_data LONGTEXT,
    latitude DECIMAL(10,7),
    longitude DECIMAL(10,7),
    notes TEXT,
    PRIMARY KEY (checkpoint_id),
    UNIQUE KEY uk_qr_token (qr_token),
    INDEX idx_reservation (reservation_id),
    INDEX idx_type (checkpoint_type),
    INDEX idx_status (status),
    FOREIGN KEY (reservation_id) REFERENCES reservations(reservation_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- PHẦN 3: TẠO BẢNG - DATABASE co_ownership_booking_admin (ReservationAdminService)
-- =====================================================

USE co_ownership_booking_admin;

-- Bảng groups
CREATE TABLE IF NOT EXISTS `groups` (
    id BIGINT NOT NULL AUTO_INCREMENT,
    created_at DATETIME(6),
    description TEXT,
    name VARCHAR(255),
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Bảng vehicles
CREATE TABLE IF NOT EXISTS vehicles (
    id BIGINT NOT NULL AUTO_INCREMENT,
    vehicle_name VARCHAR(255),
    vehicle_type VARCHAR(255),
    license_plate VARCHAR(255),
    group_id BIGINT,
    status VARCHAR(50),
    PRIMARY KEY (id),
    INDEX idx_group_id (group_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Bảng reservations
CREATE TABLE IF NOT EXISTS reservations (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT,
    vehicle_id BIGINT,
    start_datetime DATETIME(6),
    end_datetime DATETIME(6),
    purpose TEXT,
    status VARCHAR(50),
    created_at DATETIME(6),
    PRIMARY KEY (id),
    INDEX idx_user_id (user_id),
    INDEX idx_vehicle_id (vehicle_id),
    INDEX idx_start_datetime (start_datetime),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Bảng admin_users (đổi tên từ users để tránh nhầm lẫn)
CREATE TABLE IF NOT EXISTS admin_users (
    id BIGINT NOT NULL AUTO_INCREMENT,
    username VARCHAR(255) UNIQUE,
    email VARCHAR(255),
    password VARCHAR(255),
    role VARCHAR(50),
    created_at DATETIME(6),
    PRIMARY KEY (id),
    INDEX idx_username (username),
    INDEX idx_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Bảng users (cho đồng bộ từ booking database)
CREATE TABLE IF NOT EXISTS users (
    id BIGINT NOT NULL AUTO_INCREMENT,
    username VARCHAR(255),
    email VARCHAR(255),
    password VARCHAR(255),
    role VARCHAR(50),
    created_at DATETIME(6),
    PRIMARY KEY (id),
    INDEX idx_username (username),
    INDEX idx_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- PHẦN 4: TẠO BẢNG - DATABASE ai_ev (AIService)
-- =====================================================

USE ai_ev;

-- Bảng ownership_info
CREATE TABLE IF NOT EXISTS ownership_info (
    ownership_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    vehicle_id BIGINT NOT NULL,
    group_id BIGINT NOT NULL,
    ownership_percentage DOUBLE NOT NULL,
    role VARCHAR(20) DEFAULT 'MEMBER',
    joined_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_vehicle (user_id, vehicle_id),
    INDEX idx_vehicle (vehicle_id),
    INDEX idx_group (group_id),
    INDEX idx_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Bảng usage_analysis
CREATE TABLE IF NOT EXISTS usage_analysis (
    analysis_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    vehicle_id BIGINT NOT NULL,
    group_id BIGINT NOT NULL,
    total_hours_used DOUBLE DEFAULT 0,
    total_kilometers DOUBLE DEFAULT 0,
    booking_count INT DEFAULT 0,
    cancellation_count INT DEFAULT 0,
    usage_percentage DOUBLE DEFAULT 0,
    cost_incurred DOUBLE DEFAULT 0,
    period_start TIMESTAMP NULL,
    period_end TIMESTAMP NULL,
    analyzed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user (user_id),
    INDEX idx_vehicle (vehicle_id),
    INDEX idx_group (group_id),
    INDEX idx_period (period_start, period_end),
    INDEX idx_analyzed (analyzed_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Bảng fairness_score
CREATE TABLE IF NOT EXISTS fairness_score (
    score_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    vehicle_id BIGINT NOT NULL,
    group_id BIGINT NOT NULL,
    ownership_percentage DOUBLE NOT NULL,
    usage_percentage DOUBLE NOT NULL,
    difference DOUBLE NOT NULL,
    fairness_score DOUBLE NOT NULL,
    priority VARCHAR(20) DEFAULT 'NORMAL',
    period_start TIMESTAMP NULL,
    period_end TIMESTAMP NULL,
    calculated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user (user_id),
    INDEX idx_vehicle (vehicle_id),
    INDEX idx_group (group_id),
    INDEX idx_priority (priority),
    INDEX idx_calculated (calculated_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Bảng ai_recommendations
CREATE TABLE IF NOT EXISTS ai_recommendations (
   recommendation_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    group_id BIGINT NOT NULL,
    vehicle_id BIGINT NOT NULL,
    type VARCHAR(50) NOT NULL,
    title VARCHAR(500),
    description TEXT,
    severity VARCHAR(20) DEFAULT 'INFO',
    target_user_id BIGINT,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    period_start TIMESTAMP NULL,
    period_end TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    read_at TIMESTAMP NULL,
    INDEX idx_group (group_id),
    INDEX idx_vehicle (vehicle_id),
    INDEX idx_target_user (target_user_id),
    INDEX idx_status (status),
    INDEX idx_severity (severity),
    INDEX idx_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- PHẦN 5: CHÈN DỮ LIỆU MẪU - co_ownership_booking
-- =====================================================

USE co_ownership_booking;

-- Xóa dữ liệu cũ (nếu có)
SET SQL_SAFE_UPDATES = 0;
SET FOREIGN_KEY_CHECKS = 0;
DELETE FROM reservation_checkpoints;
DELETE FROM reservations;
DELETE FROM group_members;
DELETE FROM vehicles;
DELETE FROM vehicle_groups;
DELETE FROM users;
SET FOREIGN_KEY_CHECKS = 1;
SET SQL_SAFE_UPDATES = 1;

-- Reset AUTO_INCREMENT
ALTER TABLE reservations AUTO_INCREMENT = 1;
ALTER TABLE reservation_checkpoints AUTO_INCREMENT = 1;
ALTER TABLE group_members AUTO_INCREMENT = 1;
ALTER TABLE vehicles AUTO_INCREMENT = 1;
ALTER TABLE users AUTO_INCREMENT = 1;

-- Chèn users
INSERT INTO users (user_id, full_name, email, phone, username, password) VALUES
(1, 'Nguyễn Văn An', 'nguyenvanan@example.com', '0901234567', 'nguyenvanan', 'password123'),
(2, 'Trần Thị Bình', 'tranthibinh@example.com', '0902345678', 'tranthibinh', 'password123'),
(3, 'Lê Văn Cường', 'levancuong@example.com', '0903456789', 'levancuong', 'password123'),
(4, 'Phạm Thị Dung', 'phamthidung@example.com', '0904567890', 'phamthidung', 'password123'),
(5, 'Hoàng Văn Em', 'hoangvanem@example.com', '0905678901', 'hoangvanem', 'password123');

-- Chèn vehicle_groups
INSERT INTO vehicle_groups (group_id, group_name, description, creation_date, active) VALUES
('GRP001', 'Nhóm Xe Điện Tesla', 'Nhóm sở hữu chung xe Tesla Model 3', '2024-01-15 10:00:00', 'ACTIVE'),
('GRP002', 'Nhóm Xe Điện VinFast', 'Nhóm sở hữu chung xe VinFast VF8', '2024-02-01 09:00:00', 'ACTIVE'),
('GRP003', 'Nhóm Xe Hybrid', 'Nhóm sở hữu chung xe Hybrid Toyota', '2024-02-20 14:30:00', 'ACTIVE');

-- Chèn vehicles
INSERT INTO vehicles (vehicle_id, vehicle_name, license_plate, vehicle_type, group_id, status) VALUES
(1, 'Tesla Model 3', '30A-12345', 'ELECTRIC', 'GRP001', 'AVAILABLE'),
(2, 'VinFast VF8', '30B-67890', 'ELECTRIC', 'GRP002', 'AVAILABLE'),
(3, 'Toyota Prius Hybrid', '30C-11111', 'HYBRID', 'GRP003', 'AVAILABLE'),
(4, 'Tesla Model Y', '30A-22222', 'ELECTRIC', 'GRP001', 'AVAILABLE'),
(5, 'VinFast VF9', '30B-33333', 'ELECTRIC', 'GRP002', 'MAINTENANCE');

-- Chèn group_members
INSERT INTO group_members (member_id, group_id, user_id, ownership_percentage) VALUES
(1, 'GRP001', 1, 40.0),
(2, 'GRP001', 2, 35.0),
(3, 'GRP001', 3, 25.0),
(4, 'GRP002', 2, 50.0),
(5, 'GRP002', 4, 50.0),
(6, 'GRP003', 1, 33.33),
(7, 'GRP003', 3, 33.33),
(8, 'GRP003', 5, 33.34);

-- Chèn reservations
INSERT INTO reservations (reservation_id, vehicle_id, user_id, start_datetime, end_datetime, purpose, status) VALUES
(1, 1, 1, '2024-11-15 08:00:00', '2024-11-15 12:00:00', 'Đi công tác Hà Nội', 'BOOKED'),
(2, 1, 2, '2024-11-15 14:00:00', '2024-11-15 18:00:00', 'Đi chơi cuối tuần', 'BOOKED'),
(3, 2, 4, '2024-11-16 09:00:00', '2024-11-16 17:00:00', 'Đi du lịch Đà Lạt', 'BOOKED'),
(4, 3, 1, '2024-11-17 07:00:00', '2024-11-17 19:00:00', 'Đi thăm gia đình', 'BOOKED'),
(5, 2, 2, '2024-11-18 10:00:00', '2024-11-18 15:00:00', 'Đi mua sắm', 'CANCELLED'),
(6, 4, 3, '2024-11-19 08:00:00', '2024-11-19 16:00:00', 'Đi họp công ty', 'BOOKED');

-- =====================================================
-- PHẦN 6: CHÈN DỮ LIỆU MẪU - co_ownership_booking_admin
-- =====================================================

USE co_ownership_booking_admin;

-- Xóa dữ liệu cũ (nếu có)
SET SQL_SAFE_UPDATES = 0;
SET FOREIGN_KEY_CHECKS = 0;
DELETE FROM reservations;
DELETE FROM vehicles;
DELETE FROM `groups`;
DELETE FROM admin_users;
DELETE FROM users;
SET FOREIGN_KEY_CHECKS = 1;
SET SQL_SAFE_UPDATES = 1;

-- Reset AUTO_INCREMENT
ALTER TABLE reservations AUTO_INCREMENT = 1;
ALTER TABLE vehicles AUTO_INCREMENT = 1;
ALTER TABLE `groups` AUTO_INCREMENT = 1;
ALTER TABLE admin_users AUTO_INCREMENT = 1;
ALTER TABLE users AUTO_INCREMENT = 1;

-- Chèn admin_users (tài khoản admin)
INSERT INTO admin_users (id, username, email, password, role, created_at) VALUES
(1, 'admin', 'admin@example.com', 'admin123', 'ADMIN', NOW()),
(2, 'manager', 'manager@example.com', 'manager123', 'MANAGER', NOW()),
(3, 'staff', 'staff@example.com', 'staff123', 'STAFF', NOW());

-- Chèn groups
INSERT INTO `groups` (id, name, description, created_at) VALUES
(1, 'Nhóm Xe Điện Tesla', 'Nhóm sở hữu chung xe Tesla Model 3', NOW()),
(2, 'Nhóm Xe Điện VinFast', 'Nhóm sở hữu chung xe VinFast VF8', NOW()),
(3, 'Nhóm Xe Hybrid', 'Nhóm sở hữu chung xe Hybrid Toyota', NOW());

-- Chèn vehicles
INSERT INTO vehicles (id, vehicle_name, vehicle_type, license_plate, group_id, status) VALUES
(1, 'Tesla Model 3', 'ELECTRIC', '30A-12345', 1, 'AVAILABLE'),
(2, 'VinFast VF8', 'ELECTRIC', '30B-67890', 2, 'AVAILABLE'),
(3, 'Toyota Prius Hybrid', 'HYBRID', '30C-11111', 3, 'AVAILABLE'),
(4, 'Tesla Model Y', 'ELECTRIC', '30A-22222', 1, 'AVAILABLE'),
(5, 'VinFast VF9', 'ELECTRIC', '30B-33333', 2, 'MAINTENANCE');

-- Chèn users (đồng bộ từ booking database)
INSERT INTO users (id, username, email, password, role, created_at) VALUES
(1, 'nguyenvanan', 'nguyenvanan@example.com', 'password123', 'USER', NOW()),
(2, 'tranthibinh', 'tranthibinh@example.com', 'password123', 'USER', NOW()),
(3, 'levancuong', 'levancuong@example.com', 'password123', 'USER', NOW()),
(4, 'phamthidung', 'phamthidung@example.com', 'password123', 'USER', NOW()),
(5, 'hoangvanem', 'hoangvanem@example.com', 'password123', 'USER', NOW());

-- Chèn reservations
INSERT INTO reservations (id, user_id, vehicle_id, start_datetime, end_datetime, purpose, status, created_at) VALUES
(1, 1, 1, '2024-11-15 08:00:00', '2024-11-15 12:00:00', 'Đi công tác Hà Nội', 'BOOKED', NOW()),
(2, 2, 1, '2024-11-15 14:00:00', '2024-11-15 18:00:00', 'Đi chơi cuối tuần', 'BOOKED', NOW()),
(3, 4, 2, '2024-11-16 09:00:00', '2024-11-16 17:00:00', 'Đi du lịch Đà Lạt', 'BOOKED', NOW()),
(4, 1, 3, '2024-11-17 07:00:00', '2024-11-17 19:00:00', 'Đi thăm gia đình', 'BOOKED', NOW()),
(5, 2, 2, '2024-11-18 10:00:00', '2024-11-18 15:00:00', 'Đi mua sắm', 'CANCELLED', NOW()),
(6, 3, 4, '2024-11-19 08:00:00', '2024-11-19 16:00:00', 'Đi họp công ty', 'BOOKED', NOW());

-- =====================================================
-- PHẦN 7: CHÈN DỮ LIỆU MẪU - ai_ev
-- =====================================================

USE ai_ev;

-- Xóa dữ liệu cũ (nếu có)
SET SQL_SAFE_UPDATES = 0;
SET FOREIGN_KEY_CHECKS = 0;
DELETE FROM ai_recommendations;
DELETE FROM fairness_score;
DELETE FROM usage_analysis;
DELETE FROM ownership_info;
SET FOREIGN_KEY_CHECKS = 1;
SET SQL_SAFE_UPDATES = 1;

-- Reset AUTO_INCREMENT
ALTER TABLE ai_recommendations AUTO_INCREMENT = 1;
ALTER TABLE fairness_score AUTO_INCREMENT = 1;
ALTER TABLE usage_analysis AUTO_INCREMENT = 1;
ALTER TABLE ownership_info AUTO_INCREMENT = 1;

-- Chèn ownership_info
INSERT INTO ownership_info (ownership_id, user_id, vehicle_id, group_id, ownership_percentage, role, joined_date) VALUES
(1, 1, 1, 1, 40.0, 'OWNER', '2024-01-15 10:00:00'),
(2, 2, 1, 1, 35.0, 'MEMBER', '2024-01-15 10:00:00'),
(3, 3, 1, 1, 25.0, 'MEMBER', '2024-01-15 10:00:00'),
(4, 2, 2, 2, 50.0, 'OWNER', '2024-02-01 09:00:00'),
(5, 4, 2, 2, 50.0, 'OWNER', '2024-02-01 09:00:00'),
(6, 1, 3, 3, 33.33, 'MEMBER', '2024-02-20 14:30:00'),
(7, 3, 3, 3, 33.33, 'MEMBER', '2024-02-20 14:30:00'),
(8, 5, 3, 3, 33.34, 'MEMBER', '2024-02-20 14:30:00');

-- Chèn usage_analysis
INSERT INTO usage_analysis (analysis_id, user_id, vehicle_id, group_id, total_hours_used, total_kilometers, booking_count, cancellation_count, usage_percentage, cost_incurred, period_start, period_end) VALUES
(1, 1, 1, 1, 120.5, 1500.0, 15, 2, 45.0, 5000000, '2024-01-01 00:00:00', '2024-11-14 23:59:59'),
(2, 2, 1, 1, 80.0, 1000.0, 10, 1, 30.0, 3500000, '2024-01-01 00:00:00', '2024-11-14 23:59:59'),
(3, 3, 1, 1, 50.0, 600.0, 8, 0, 25.0, 2000000, '2024-01-01 00:00:00', '2024-11-14 23:59:59'),
(4, 2, 2, 2, 100.0, 1200.0, 12, 1, 48.0, 6000000, '2024-02-01 00:00:00', '2024-11-14 23:59:59'),
(5, 4, 2, 2, 110.0, 1300.0, 13, 0, 52.0, 6500000, '2024-02-01 00:00:00', '2024-11-14 23:59:59'),
(6, 1, 3, 3, 60.0, 800.0, 9, 1, 32.0, 3000000, '2024-02-20 00:00:00', '2024-11-14 23:59:59'),
(7, 3, 3, 3, 55.0, 700.0, 8, 0, 30.0, 2800000, '2024-02-20 00:00:00', '2024-11-14 23:59:59'),
(8, 5, 3, 3, 65.0, 850.0, 10, 0, 38.0, 3200000, '2024-02-20 00:00:00', '2024-11-14 23:59:59');

-- Chèn fairness_score
INSERT INTO fairness_score (score_id, user_id, vehicle_id, group_id, ownership_percentage, usage_percentage, difference, fairness_score, priority, period_start, period_end) VALUES
(1, 1, 1, 1, 40.0, 45.0, 5.0, 85.0, 'NORMAL', '2024-01-01 00:00:00', '2024-11-14 23:59:59'),
(2, 2, 1, 1, 35.0, 30.0, -5.0, 90.0, 'HIGH', '2024-01-01 00:00:00', '2024-11-14 23:59:59'),
(3, 3, 1, 1, 25.0, 25.0, 0.0, 100.0, 'NORMAL', '2024-01-01 00:00:00', '2024-11-14 23:59:59'),
(4, 2, 2, 2, 50.0, 48.0, -2.0, 95.0, 'NORMAL', '2024-02-01 00:00:00', '2024-11-14 23:59:59'),
(5, 4, 2, 2, 50.0, 52.0, 2.0, 95.0, 'NORMAL', '2024-02-01 00:00:00', '2024-11-14 23:59:59'),
(6, 1, 3, 3, 33.33, 32.0, -1.33, 98.0, 'NORMAL', '2024-02-20 00:00:00', '2024-11-14 23:59:59'),
(7, 3, 3, 3, 33.33, 30.0, -3.33, 92.0, 'NORMAL', '2024-02-20 00:00:00', '2024-11-14 23:59:59'),
(8, 5, 3, 3, 33.34, 38.0, 4.66, 88.0, 'NORMAL', '2024-02-20 00:00:00', '2024-11-14 23:59:59');

-- Chèn ai_recommendations
INSERT INTO ai_recommendations (recommendation_id, group_id, vehicle_id, type, title, description, severity, target_user_id, status, period_start, period_end, created_at) VALUES
(1, 1, 1, 'USAGE_FAIRNESS', 'Khuyến nghị tăng thời gian sử dụng cho thành viên Trần Thị Bình', 'Người dùng Trần Thị Bình có tỷ lệ sở hữu 35% nhưng chỉ sử dụng 30%. Nên ưu tiên cho người này đặt xe trong thời gian tới.', 'INFO', 2, 'ACTIVE', '2024-11-15 00:00:00', '2024-12-15 23:59:59', NOW()),
(2, 2, 2, 'BALANCE_USAGE', 'Cân bằng sử dụng giữa các thành viên', 'Cả hai thành viên đều sử dụng gần bằng nhau, tình trạng tốt. Tiếp tục duy trì.', 'INFO', NULL, 'ACTIVE', '2024-11-15 00:00:00', '2024-12-15 23:59:59', NOW()),
(3, 3, 3, 'OVERUSAGE_WARNING', 'Cảnh báo sử dụng quá mức', 'Người dùng Hoàng Văn Em có tỷ lệ sử dụng 38% trong khi sở hữu 33.34%. Nên giảm tần suất đặt xe.', 'WARNING', 5, 'ACTIVE', '2024-11-15 00:00:00', '2024-12-15 23:59:59', NOW()),
(4, 1, 1, 'MAINTENANCE_REMINDER', 'Nhắc nhở bảo dưỡng định kỳ', 'Xe Tesla Model 3 đã chạy được 1500km, nên đưa đi bảo dưỡng định kỳ.', 'INFO', NULL, 'ACTIVE', '2024-11-15 00:00:00', '2024-12-15 23:59:59', NOW()),
(5, 2, 5, 'VEHICLE_STATUS', 'Xe đang trong trạng thái bảo trì', 'Xe VinFast VF9 đang trong trạng thái bảo trì, không thể đặt xe cho đến khi hoàn tất.', 'WARNING', NULL, 'ACTIVE', '2024-11-15 00:00:00', '2024-12-15 23:59:59', NOW());
-- Chạy script này trong MySQL
USE co_ownership_booking_admin;

-- Thêm cột created_at nếu chưa có
ALTER TABLE reservations
    ADD COLUMN IF NOT EXISTS created_at DATETIME(6) DEFAULT CURRENT_TIMESTAMP;

-- Cập nhật giá trị cho các bản ghi cũ
UPDATE reservations
SET created_at = start_datetime
WHERE created_at IS NULL AND start_datetime IS NOT NULL;
-- =====================================================
-- PHẦN 8: XÁC MINH VÀ KIỂM TRA
-- =====================================================

-- Hiển thị tất cả database đã tạo
SHOW DATABASES LIKE 'co_ownership%';
SHOW DATABASES LIKE 'ai_ev';

-- Kiểm tra các bảng trong từng database
SELECT 'co_ownership_booking' AS database_name, TABLE_NAME 
FROM information_schema.TABLES 
WHERE TABLE_SCHEMA = 'co_ownership_booking'
ORDER BY TABLE_NAME;

SELECT 'co_ownership_booking_admin' AS database_name, TABLE_NAME 
FROM information_schema.TABLES 
WHERE TABLE_SCHEMA = 'co_ownership_booking_admin'
ORDER BY TABLE_NAME;

SELECT 'ai_ev' AS database_name, TABLE_NAME 
FROM information_schema.TABLES 
WHERE TABLE_SCHEMA = 'ai_ev'
ORDER BY TABLE_NAME;

-- Kiểm tra số lượng bản ghi trong từng bảng
SELECT 'co_ownership_booking' AS database_name, 'users' AS table_name, COUNT(*) AS record_count FROM co_ownership_booking.users
UNION ALL
SELECT 'co_ownership_booking', 'vehicle_groups', COUNT(*) FROM co_ownership_booking.vehicle_groups
UNION ALL
SELECT 'co_ownership_booking', 'vehicles', COUNT(*) FROM co_ownership_booking.vehicles
UNION ALL
SELECT 'co_ownership_booking', 'group_members', COUNT(*) FROM co_ownership_booking.group_members
UNION ALL
SELECT 'co_ownership_booking', 'reservations', COUNT(*) FROM co_ownership_booking.reservations
UNION ALL
SELECT 'co_ownership_booking_admin', 'admin_users', COUNT(*) FROM co_ownership_booking_admin.admin_users
UNION ALL
SELECT 'co_ownership_booking_admin', 'groups', COUNT(*) FROM co_ownership_booking_admin.groups
UNION ALL
SELECT 'co_ownership_booking_admin', 'vehicles', COUNT(*) FROM co_ownership_booking_admin.vehicles
UNION ALL
SELECT 'co_ownership_booking_admin', 'users', COUNT(*) FROM co_ownership_booking_admin.users
UNION ALL
SELECT 'co_ownership_booking_admin', 'reservations', COUNT(*) FROM co_ownership_booking_admin.reservations
UNION ALL
SELECT 'ai_ev', 'ownership_info', COUNT(*) FROM ai_ev.ownership_info
UNION ALL
SELECT 'ai_ev', 'usage_analysis', COUNT(*) FROM ai_ev.usage_analysis
UNION ALL
SELECT 'ai_ev', 'fairness_score', COUNT(*) FROM ai_ev.fairness_score
UNION ALL
SELECT 'ai_ev', 'ai_recommendations', COUNT(*) FROM ai_ev.ai_recommendations;

-- =====================================================
-- HOÀN TẤT
-- =====================================================
-- ✅ Script đã hoàn tất!
-- 
-- Đã tạo và chèn dữ liệu mẫu vào:
-- 
-- 1. Database: co_ownership_booking (ReservationService)
--    - 5 users
--    - 3 vehicle_groups
--    - 5 vehicles
--    - 8 group_members
--    - 6 reservations
-- 
-- 2. Database: co_ownership_booking_admin (ReservationAdminService)
--    - 3 admin_users (admin/admin123, manager/manager123, staff/staff123)
--    - 3 groups
--    - 5 vehicles
--    - 5 users
--    - 6 reservations
-- 
-- 3. Database: ai_ev (AIService)
--    - 8 ownership_info
--    - 8 usage_analysis
--    - 8 fairness_score
--    - 5 ai_recommendations
-- 
-- =====================================================
-- LƯU Ý:
-- - Tài khoản admin: username='admin', password='admin123'
-- - Tài khoản manager: username='manager', password='manager123'
-- - Tài khoản staff: username='staff', password='staff123'
-- =====================================================

