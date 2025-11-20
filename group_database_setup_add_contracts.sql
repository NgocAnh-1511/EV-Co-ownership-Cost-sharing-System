-- ==========================================
-- GROUP_Management_DB - Contract Extensions
-- ==========================================
-- Run this script after the base group_database_setup.sql
-- to add contract management tables that enforce the
-- “sign before join” rule for vehicle co-ownership groups.

USE Group_Management_DB;

-- 1. Nhóm hợp đồng
CREATE TABLE IF NOT EXISTS GroupContract (
    contract_id INT AUTO_INCREMENT PRIMARY KEY,
    group_id INT NOT NULL,
    contract_code VARCHAR(100) NOT NULL UNIQUE,
    contract_content TEXT,
    contract_status ENUM('pending', 'signed', 'archived') DEFAULT 'pending',
    creation_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    signed_date DATETIME NULL,
    created_by INT,
    CONSTRAINT fk_group_contract_group
        FOREIGN KEY (group_id) REFERENCES `Group`(groupId)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_group_contract_group_id ON GroupContract(group_id);

-- 2. Chữ ký hợp đồng
CREATE TABLE IF NOT EXISTS ContractSignature (
    signature_id INT AUTO_INCREMENT PRIMARY KEY,
    contract_id INT NOT NULL,
    user_id INT NOT NULL,
    signed_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    signature_method VARCHAR(50),
    ip_address VARCHAR(45),
    CONSTRAINT fk_contract_signature_contract
        FOREIGN KEY (contract_id) REFERENCES GroupContract(contract_id)
        ON DELETE CASCADE,
    CONSTRAINT uq_contract_signature UNIQUE (contract_id, user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_contract_signature_user ON ContractSignature(user_id);

-- Sample contract for demo groups
INSERT INTO GroupContract (group_id, contract_code, contract_content, contract_status, created_by)
SELECT g.groupId,
       CONCAT('LC-', UPPER(REPLACE(g.groupName, ' ', '')), '-', YEAR(NOW())),
       CONCAT('Hợp đồng sở hữu chung cho nhóm ', g.groupName),
       'pending',
       g.adminId
FROM `Group` g
LEFT JOIN GroupContract c ON c.group_id = g.groupId
WHERE c.contract_id IS NULL;

SELECT '✅ Contract tables created/updated successfully.' AS '';


