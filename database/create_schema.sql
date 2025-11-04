-- =====================================================
-- SCHEMA CREATION - CarRental MicroServices System
-- =====================================================

-- ==============================
-- 1️⃣ CƠ SỞ DỮ LIỆU VEHICLE_MANAGEMENT
-- ==============================
CREATE DATABASE IF NOT EXISTS vehicle_management;
USE vehicle_management;

-- 🔹 Bảng VehicleGroup
CREATE TABLE vehiclegroup (
    group_id INT AUTO_INCREMENT PRIMARY KEY,
    group_name VARCHAR(255),
    description TEXT,
    creation_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 🔹 Bảng Vehicle
CREATE TABLE vehicle (
    vehicle_id INT AUTO_INCREMENT PRIMARY KEY,
    group_id INT,
    vehicle_number VARCHAR(20),
    vehicle_type VARCHAR(50),
    status VARCHAR(50),
    FOREIGN KEY (group_id) REFERENCES vehiclegroup(group_id)
);

-- 🔹 Bảng VehicleService
CREATE TABLE vehicleservice (
    service_id INT AUTO_INCREMENT PRIMARY KEY,
    vehicle_id INT,
    service_name VARCHAR(255),
    service_description TEXT,
    service_type VARCHAR(50),
    request_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(50),
    completion_date TIMESTAMP,
    FOREIGN KEY (vehicle_id) REFERENCES vehicle(vehicle_id)
);

-- 🔹 Bảng VehicleHistory
CREATE TABLE vehiclehistory (
    history_id INT AUTO_INCREMENT PRIMARY KEY,
    group_id INT,
    user_id INT,
    usage_start TIMESTAMP,
    usage_end TIMESTAMP
    -- Không có foreign key constraint để tránh lỗi khi xóa nhóm xe
);

-- ==============================
-- 2️⃣ CƠ SỞ DỮ LIỆU LEGAL_CONTRACT
-- ==============================
CREATE DATABASE IF NOT EXISTS legal_contract;
USE legal_contract;

-- 🔹 Bảng LegalContract
CREATE TABLE legalcontract (
    contract_id INT AUTO_INCREMENT PRIMARY KEY,
    contract_code VARCHAR(100),
    contract_status VARCHAR(50),
    creation_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    signed_date TIMESTAMP
);

-- 🔹 Bảng ContractHistory
CREATE TABLE contracthistory (
    history_id INT AUTO_INCREMENT PRIMARY KEY,
    contract_id INT,
    action VARCHAR(255),
    action_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (contract_id) REFERENCES legalcontract(contract_id)
);

-- 🔹 Bảng ContractSignatures
CREATE TABLE contractsignatures (
    signature_id INT AUTO_INCREMENT PRIMARY KEY,
    contract_id INT,
    signer_id INT,
    signature_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (contract_id) REFERENCES legalcontract(contract_id)
);





