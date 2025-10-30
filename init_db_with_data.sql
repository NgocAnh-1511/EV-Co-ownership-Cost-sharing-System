-- ĐẢM BẢO BỘ KÝ TỰ HỖ TRỢ TIẾNG VIỆT (UTF-8)
DROP DATABASE IF EXISTS CoOwnershipDB;
CREATE DATABASE IF NOT EXISTS CoOwnershipDB
CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE CoOwnershipDB;

-- Bảng 1: Người dùng (Users)
CREATE TABLE Users (
    user_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL, -- Mật khẩu đã được mã hóa
    full_name VARCHAR(255),
    phone_number VARCHAR(20) UNIQUE,
    is_verified BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    role VARCHAR(50) NOT NULL DEFAULT 'ROLE_USER',
    date_of_birth DATE,
    id_card_number VARCHAR(20) UNIQUE,
    id_card_issue_date DATE,
    id_card_issue_place VARCHAR(255),
    license_number VARCHAR(20) UNIQUE,
    license_class VARCHAR(10),
    license_issue_date DATE,
    license_expiry_date DATE,
    id_card_front_url VARCHAR(512),
    id_card_back_url VARCHAR(512),
    license_image_url VARCHAR(512),
    portrait_image_url VARCHAR(512)
);

-- Bảng 2: Tài sản (Assets)
CREATE TABLE Assets (
    asset_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    asset_name VARCHAR(255) NOT NULL,
    identifier VARCHAR(100) UNIQUE, -- Biển số xe
    description TEXT,
    total_value DECIMAL(12, 2),
    image_url VARCHAR(512)
);

-- Bảng 3: Hợp đồng (Contracts)
CREATE TABLE Contracts (
    contract_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    asset_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    document_url VARCHAR(512),
    status VARCHAR(50) DEFAULT 'pending', -- Trạng thái: active, pending, finished
    start_date DATE NOT NULL,
    end_date DATE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (asset_id) REFERENCES Assets(asset_id)
);

-- Bảng 4: Quyền sở hữu (Ownership)
CREATE TABLE Ownership (
    ownership_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    asset_id BIGINT NOT NULL,
    contract_id BIGINT NOT NULL,
    ownership_percentage DECIMAL(5, 2) NOT NULL,
    CHECK (ownership_percentage > 0 AND ownership_percentage <= 100),
    FOREIGN KEY (user_id) REFERENCES Users(user_id),
    FOREIGN KEY (asset_id) REFERENCES Assets(asset_id),
    FOREIGN KEY (contract_id) REFERENCES Contracts(contract_id)
);

-- Bảng 5, 6, 7, 8 (Expenses, Transactions, Disputes, DisputeMessages)
-- Giữ nguyên cấu trúc đã tạo của bạn cho các bảng này (chúng được chuẩn hóa)
-- ... (Các bảng còn lại)