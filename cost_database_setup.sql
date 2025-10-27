-- ===============================
-- DATABASE: Cost_Payment_DB
-- ===============================
CREATE DATABASE IF NOT EXISTS Cost_Payment_DB;
USE Cost_Payment_DB;

-- 1️⃣ Bảng COST (Chi phí)
CREATE TABLE Cost (
    costId INT AUTO_INCREMENT PRIMARY KEY,
    vehicleId INT NOT NULL,
    costType ENUM('ElectricCharge','Maintenance','Insurance','Inspection','Cleaning','Other') DEFAULT 'Other',
    amount DOUBLE NOT NULL,
    description TEXT,
    createdAt DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- 2️⃣ Bảng COST_SHARE (Chia chi phí giữa đồng sở hữu)
CREATE TABLE CostShare (
    shareId INT AUTO_INCREMENT PRIMARY KEY,
    costId INT NOT NULL,
    userId INT NOT NULL,
    percent DOUBLE DEFAULT 0,
    amountShare DOUBLE DEFAULT 0,
    calculatedAt DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (costId) REFERENCES Cost(costId) ON DELETE CASCADE
);

-- 3️⃣ Bảng PAYMENT (Thanh toán)
CREATE TABLE Payment (
    paymentId INT AUTO_INCREMENT PRIMARY KEY,
    userId INT NOT NULL,
    costId INT,
    method ENUM('EWallet','Banking','Cash') DEFAULT 'EWallet',
    amount DOUBLE NOT NULL,
    transactionCode VARCHAR(100),
    paymentDate DATETIME DEFAULT CURRENT_TIMESTAMP,
    status ENUM('Pending','Completed','Failed') DEFAULT 'Pending',
    FOREIGN KEY (costId) REFERENCES Cost(costId) ON DELETE SET NULL
);

-- 4️⃣ Bảng GROUP_FUND (Quỹ chung)
CREATE TABLE GroupFund (
    fundId INT AUTO_INCREMENT PRIMARY KEY,
    groupId INT NOT NULL,
    totalContributed DOUBLE DEFAULT 0,
    currentBalance DOUBLE DEFAULT 0,
    updatedAt DATETIME DEFAULT CURRENT_TIMESTAMP,
    note TEXT
);

-- 5️⃣ Bảng FUND_TRANSACTION (Giao dịch quỹ)
CREATE TABLE FundTransaction (
    transactionId INT AUTO_INCREMENT PRIMARY KEY,
    fundId INT NOT NULL,
    userId INT,
    transactionType ENUM('Deposit','Withdraw') DEFAULT 'Deposit',
    amount DOUBLE NOT NULL,
    purpose VARCHAR(255),
    date DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (fundId) REFERENCES GroupFund(fundId) ON DELETE CASCADE
);

-- Insert sample data
INSERT INTO Cost (vehicleId, costType, amount, description) VALUES 
(1, 'ElectricCharge', 500000, 'Chi phí sạc điện tháng 1'),
(1, 'Maintenance', 2000000, 'Bảo dưỡng định kỳ'),
(2, 'Insurance', 3000000, 'Bảo hiểm xe năm 2024');

INSERT INTO GroupFund (groupId, totalContributed, currentBalance, note) VALUES 
(1, 1000000, 800000, 'Quỹ chung nhóm Tesla Model 3'),
(2, 500000, 300000, 'Quỹ chung nhóm BMW i3');
