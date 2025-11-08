-- =====================================================
-- HOÀN THIỆN BẢNG SERVICE - CHẠY FILE NÀY ĐỂ SETUP ĐẦY ĐỦ
-- =====================================================

USE vehicle_management;

-- =====================================================
-- BƯỚC 1: TẠO BẢNG SERVICE
-- =====================================================
CREATE TABLE IF NOT EXISTS service (
    service_id VARCHAR(20) PRIMARY KEY,
    service_name VARCHAR(255) NOT NULL,
    service_type VARCHAR(50) NOT NULL,
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_service_name (service_name),
    INDEX idx_service_type (service_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- BƯỚC 2: CHÈN DỮ LIỆU MẪU
-- =====================================================
INSERT IGNORE INTO service (service_id, service_name, service_type) VALUES
-- Bảo dưỡng
('SRV001', 'Bảo dưỡng định kỳ', 'Bảo dưỡng'),
('SRV002', 'Thay dầu động cơ', 'Bảo dưỡng'),
('SRV003', 'Thay lọc gió', 'Bảo dưỡng'),
('SRV004', 'Thay lọc nhiên liệu', 'Bảo dưỡng'),
('SRV017', 'Cân bằng và sửa lốp', 'Bảo dưỡng'),
('SRV020', 'Bảo dưỡng pin EV', 'Bảo dưỡng'),
('SRV023', 'Thay dầu hộp số', 'Bảo dưỡng'),
('SRV025', 'Thay bugi', 'Bảo dưỡng'),
-- Sửa chữa
('SRV005', 'Kiểm tra phanh', 'Sửa chữa'),
('SRV006', 'Thay má phanh', 'Sửa chữa'),
('SRV007', 'Kiểm tra hệ thống điện', 'Sửa chữa'),
('SRV008', 'Sửa chữa động cơ', 'Sửa chữa'),
('SRV009', 'Thay ắc quy', 'Sửa chữa'),
('SRV010', 'Sửa chữa hệ thống làm mát', 'Sửa chữa'),
('SRV016', 'Sửa chữa hệ thống treo', 'Sửa chữa'),
('SRV018', 'Thay lốp xe', 'Sửa chữa'),
('SRV022', 'Kiểm tra động cơ', 'Sửa chữa'),
('SRV024', 'Kiểm tra hệ thống phanh', 'Sửa chữa'),
-- Vệ sinh
('SRV011', 'Vệ sinh nội thất', 'Vệ sinh'),
('SRV012', 'Vệ sinh ngoại thất', 'Vệ sinh'),
('SRV013', 'Đánh bóng xe', 'Vệ sinh'),
('SRV021', 'Rửa xe', 'Vệ sinh'),
-- Kiểm tra
('SRV014', 'Kiểm tra điều hòa', 'Kiểm tra'),
('SRV015', 'Nạp gas điều hòa', 'Kiểm tra'),
('SRV019', 'Kiểm tra an toàn tổng thể', 'Kiểm tra');

-- =====================================================
-- BƯỚC 3: KIỂM TRA KẾT QUẢ
-- =====================================================
SELECT '✅ Bảng service đã được tạo và có dữ liệu!' AS status;
SELECT COUNT(*) AS total_services FROM service;
SELECT * FROM service ORDER BY service_id LIMIT 10;


