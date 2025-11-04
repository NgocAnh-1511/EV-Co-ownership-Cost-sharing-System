-- ===============================
-- DATABASE: Cost_Payment_DB
-- Version ĐƠN GIẢN - Đủ làm chức năng
-- ===============================

DROP DATABASE IF EXISTS Cost_Payment_DB;
CREATE DATABASE Cost_Payment_DB;
USE Cost_Payment_DB;

-- ==========================================
-- BẢNG CHÍNH
-- ==========================================

-- 1. Chi phí
CREATE TABLE Cost (
    `costId` INT AUTO_INCREMENT PRIMARY KEY,
    `vehicleId` INT NOT NULL,
    `costType` ENUM('ElectricCharge','Maintenance','Insurance','Inspection','Cleaning','Other') DEFAULT 'Other',
    `amount` DOUBLE NOT NULL,
    `description` TEXT,
    `createdAt` DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- 2. Chia sẻ chi phí
CREATE TABLE CostShare (
    `shareId` INT AUTO_INCREMENT PRIMARY KEY,
    `costId` INT NOT NULL,
    `userId` INT NOT NULL,
    `percent` DOUBLE DEFAULT 0,
    `amountShare` DOUBLE DEFAULT 0,
    `calculatedAt` DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (`costId`) REFERENCES Cost(`costId`) ON DELETE CASCADE
);

-- 3. Thanh toán
CREATE TABLE payment (
    `paymentId` INT AUTO_INCREMENT PRIMARY KEY,
    `userId` INT NOT NULL,
    `costId` INT,
    `method` ENUM('EWALLET','BANKING','CASH') DEFAULT 'EWALLET',
    `amount` DOUBLE NOT NULL,
    `transactionCode` VARCHAR(100),
    `paymentDate` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `status` ENUM('PENDING','PAID','OVERDUE','CANCELLED') DEFAULT 'PENDING',
    FOREIGN KEY (`costId`) REFERENCES Cost(`costId`) ON DELETE SET NULL
);

-- 4. Quỹ chung
CREATE TABLE GroupFund (
    `fundId` INT AUTO_INCREMENT PRIMARY KEY,
    `groupId` INT NOT NULL,
    `totalContributed` DOUBLE DEFAULT 0,
    `currentBalance` DOUBLE DEFAULT 0,
    `updatedAt` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `note` TEXT
);

-- 5. Giao dịch quỹ (HỖ TRỢ PHÊ DUYỆT)
CREATE TABLE FundTransaction (
    `transactionId` INT AUTO_INCREMENT PRIMARY KEY,
    `fundId` INT NOT NULL,
    `userId` INT,
    `transactionType` ENUM('Deposit','Withdraw') DEFAULT 'Deposit',
    `amount` DOUBLE NOT NULL,
    `purpose` VARCHAR(255),
    `date` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `status` ENUM('Pending','Approved','Rejected','Completed') DEFAULT 'Completed',
    `approvedBy` INT,
    `approvedAt` DATETIME,
    `voteId` INT,
    `receiptUrl` VARCHAR(500),
    FOREIGN KEY (`fundId`) REFERENCES GroupFund(`fundId`) ON DELETE CASCADE
);

-- 6. Theo dõi km (THÊM MỚI - Cho chức năng chia theo km)
CREATE TABLE UsageTracking (
    `usageId` INT AUTO_INCREMENT PRIMARY KEY,
    `groupId` INT NOT NULL,
    `userId` INT NOT NULL,
    `month` INT NOT NULL,
    `year` INT NOT NULL,
    `kmDriven` DOUBLE NOT NULL DEFAULT 0,
    `recordedAt` DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY (`groupId`, `userId`, `month`, `year`)
);

-- ==========================================
-- DỮ LIỆU MẪU ĐƠN GIẢN
-- ==========================================

-- Km đã chạy tháng 10/2024 (Group 1)
INSERT INTO UsageTracking (`groupId`, `userId`, `month`, `year`, `kmDriven`) VALUES 
(1, 1, 10, 2024, 600),  -- User 1: 600km (60%)
(1, 2, 10, 2024, 300),  -- User 2: 300km (30%)
(1, 3, 10, 2024, 100);  -- User 3: 100km (10%)

-- Chi phí mẫu
INSERT INTO Cost (`vehicleId`, `costType`, `amount`, `description`) VALUES 
-- Chia theo SỞ HỮU (50%, 30%, 20%)
(1, 'Insurance', 6000000, 'Bảo hiểm năm 2024'),
(1, 'Maintenance', 5000000, 'Bảo dưỡng định kỳ'),

-- Chia theo KM (60%, 30%, 10%)
(1, 'ElectricCharge', 500000, 'Sạc điện tháng 10/2024'),

-- Chia ĐỀU (33.33% mỗi người)
(1, 'Cleaning', 150000, 'Rửa xe');

-- Chia chi phí TỰ ĐỘNG
-- Cost 1: Bảo hiểm (theo ownership 50%, 30%, 20%)
INSERT INTO CostShare (`costId`, `userId`, `percent`, `amountShare`) VALUES
(1, 1, 50.0, 3000000),
(1, 2, 30.0, 1800000),
(1, 3, 20.0, 1200000);

-- Cost 2: Bảo dưỡng (theo ownership)
INSERT INTO CostShare (`costId`, `userId`, `percent`, `amountShare`) VALUES
(2, 1, 50.0, 2500000),
(2, 2, 30.0, 1500000),
(2, 3, 20.0, 1000000);

-- Cost 3: Sạc điện (theo km: 60%, 30%, 10%)
INSERT INTO CostShare (`costId`, `userId`, `percent`, `amountShare`) VALUES
(3, 1, 60.0, 300000),
(3, 2, 30.0, 150000),
(3, 3, 10.0, 50000);

-- Cost 4: Rửa xe (chia đều)
INSERT INTO CostShare (`costId`, `userId`, `percent`, `amountShare`) VALUES
(4, 1, 33.33, 50000),
(4, 2, 33.33, 50000),
(4, 3, 33.34, 50000);

-- Thanh toán mẫu
INSERT INTO payment (`userId`, `costId`, `method`, `amount`, `status`) VALUES
(1, 1, 'BANKING', 3000000, 'PAID'),
(2, 1, 'EWALLET', 1800000, 'PAID'),
(3, 1, 'BANKING', 1200000, 'PENDING'),
(1, 3, 'EWALLET', 300000, 'PAID');

-- Quỹ
INSERT INTO GroupFund (`groupId`, `totalContributed`, `currentBalance`) VALUES 
(1, 1000000, 800000);

-- Giao dịch quỹ mẫu
INSERT INTO FundTransaction (`fundId`, `userId`, `transactionType`, `amount`, `purpose`, `status`) VALUES
(1, 1, 'Deposit', 500000, 'Nạp tiền vào quỹ', 'Completed'),
(1, 2, 'Deposit', 300000, 'Đóng góp quỹ', 'Completed'),
(1, 3, 'Deposit', 200000, 'Nạp quỹ tháng 10', 'Completed');

-- ==========================================
-- XEM DỮ LIỆU
-- ==========================================

SELECT '=== 1. CHI PHÍ ===' as '';
SELECT `costId`, `costType`, FORMAT(`amount`,0) as amount, `description` FROM Cost;

SELECT '=== 2. KM THÁNG 10/2024 ===' as '';
SELECT 
    `userId`,
    `kmDriven` as 'KM',
    ROUND(`kmDriven`/(SELECT SUM(`kmDriven`) FROM UsageTracking WHERE `month`=10)*100, 2) as '%'
FROM UsageTracking WHERE `month`=10;

SELECT '=== 3. CHIA CHI PHÍ ===' as '';
SELECT 
    cs.`costId`,
    c.`costType`,
    cs.`userId`,
    CONCAT(cs.`percent`,'%') as '%',
    FORMAT(cs.`amountShare`,0) as 'Số tiền'
FROM CostShare cs JOIN Cost c ON cs.`costId`=c.`costId`;

SELECT '=== 4. NỢ CỦA USER ===' as '';
SELECT 
    cs.`userId`,
    FORMAT(SUM(cs.`amountShare`),0) as 'Phải trả',
    FORMAT(SUM(CASE WHEN p.`status`='PAID' THEN p.`amount` ELSE 0 END),0) as 'Đã trả',
    FORMAT(SUM(cs.`amountShare`)-SUM(CASE WHEN p.`status`='PAID' THEN p.`amount` ELSE 0 END),0) as 'Còn nợ'
FROM CostShare cs LEFT JOIN payment p ON cs.`costId`=p.`costId` AND cs.`userId`=p.`userId`
GROUP BY cs.`userId`;

SELECT '=== 5. QUỸ CHUNG ===' as '';
SELECT 
    `fundId`,
    `groupId`,
    FORMAT(`totalContributed`,0) as 'Tổng đóng góp',
    FORMAT(`currentBalance`,0) as 'Số dư hiện tại'
FROM GroupFund;

SELECT '=== 6. GIAO DỊCH QUỸ ===' as '';
SELECT 
    `transactionId`,
    `fundId`,
    `userId`,
    `transactionType` as 'Loại',
    FORMAT(`amount`,0) as 'Số tiền',
    `purpose` as 'Mục đích',
    `status` as 'Trạng thái',
    DATE_FORMAT(`date`, '%d/%m/%Y %H:%i') as 'Thời gian'
FROM FundTransaction
ORDER BY `date` DESC;

SELECT '✅ DATABASE SETUP HOÀN TẤT!' as '';