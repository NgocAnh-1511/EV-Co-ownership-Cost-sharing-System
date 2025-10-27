-- ================================
-- SAMPLE DATA FOR EV CO-OWNERSHIP SYSTEM
-- ================================

USE Cost_Payment_DB;

-- Clear existing data
DELETE FROM Payment WHERE costId IS NOT NULL;
DELETE FROM CostShare;
DELETE FROM Cost;

-- Insert sample costs
INSERT INTO Cost (vehicleId, costType, amount, description, createdAt) VALUES 
(1, 'ElectricCharge', 500000, 'Chi phí sạc điện tháng 1/2024', '2024-01-15 10:30:00'),
(1, 'Maintenance', 2000000, 'Bảo dưỡng định kỳ 6 tháng', '2024-01-10 14:20:00'),
(1, 'ElectricCharge', 450000, 'Chi phí sạc điện tháng 2/2024', '2024-02-15 09:15:00'),
(1, 'Cleaning', 200000, 'Vệ sinh xe nội thất', '2024-02-20 16:45:00'),
(1, 'Insurance', 3000000, 'Bảo hiểm xe năm 2024', '2024-01-05 11:00:00'),
(1, 'Inspection', 500000, 'Kiểm định xe định kỳ', '2024-01-25 13:30:00'),
(1, 'ElectricCharge', 480000, 'Chi phí sạc điện tháng 3/2024', '2024-03-15 08:45:00'),
(1, 'Maintenance', 1500000, 'Thay lốp xe mới', '2024-03-10 15:20:00'),
(1, 'Other', 300000, 'Mua phụ kiện cho xe', '2024-03-25 12:10:00'),

(2, 'ElectricCharge', 600000, 'Chi phí sạc điện tháng 1/2024', '2024-01-16 11:30:00'),
(2, 'Maintenance', 2500000, 'Bảo dưỡng định kỳ BMW i3', '2024-01-12 15:45:00'),
(2, 'Insurance', 3500000, 'Bảo hiểm xe BMW i3 năm 2024', '2024-01-08 10:15:00'),
(2, 'ElectricCharge', 550000, 'Chi phí sạc điện tháng 2/2024', '2024-02-16 09:30:00'),
(2, 'Cleaning', 250000, 'Vệ sinh xe BMW i3', '2024-02-22 17:00:00'),
(2, 'Inspection', 600000, 'Kiểm định xe BMW i3', '2024-02-28 14:15:00'),
(2, 'ElectricCharge', 580000, 'Chi phí sạc điện tháng 3/2024', '2024-03-16 10:00:00'),
(2, 'Maintenance', 1800000, 'Sửa chữa hệ thống điện', '2024-03-12 16:30:00'),

(3, 'ElectricCharge', 700000, 'Chi phí sạc điện tháng 1/2024', '2024-01-17 12:00:00'),
(3, 'Maintenance', 3000000, 'Bảo dưỡng định kỳ Audi e-tron', '2024-01-14 16:00:00'),
(3, 'Insurance', 4000000, 'Bảo hiểm xe Audi e-tron năm 2024', '2024-01-10 09:30:00'),
(3, 'ElectricCharge', 650000, 'Chi phí sạc điện tháng 2/2024', '2024-02-17 10:45:00'),
(3, 'Cleaning', 300000, 'Vệ sinh xe Audi e-tron', '2024-02-25 18:15:00'),
(3, 'Inspection', 700000, 'Kiểm định xe Audi e-tron', '2024-03-05 15:45:00'),
(3, 'ElectricCharge', 680000, 'Chi phí sạc điện tháng 3/2024', '2024-03-17 11:15:00'),
(3, 'Maintenance', 2200000, 'Thay pin xe Audi e-tron', '2024-03-15 14:30:00'),
(3, 'Other', 500000, 'Nâng cấp hệ thống âm thanh', '2024-03-30 13:20:00');

-- Insert sample cost shares
INSERT INTO CostShare (costId, userId, percent, amountShare, calculatedAt) VALUES 
-- Cost 1 (ElectricCharge 500k) - 3 users
(1, 1, 40.0, 200000, '2024-01-15 10:35:00'),
(1, 2, 35.0, 175000, '2024-01-15 10:35:00'),
(1, 3, 25.0, 125000, '2024-01-15 10:35:00'),

-- Cost 2 (Maintenance 2M) - 3 users
(2, 1, 40.0, 800000, '2024-01-10 14:25:00'),
(2, 2, 35.0, 700000, '2024-01-10 14:25:00'),
(2, 3, 25.0, 500000, '2024-01-10 14:25:00'),

-- Cost 3 (ElectricCharge 450k) - 3 users
(3, 1, 40.0, 180000, '2024-02-15 09:20:00'),
(3, 2, 35.0, 157500, '2024-02-15 09:20:00'),
(3, 3, 25.0, 112500, '2024-02-15 09:20:00'),

-- Cost 4 (Cleaning 200k) - 3 users
(4, 1, 40.0, 80000, '2024-02-20 16:50:00'),
(4, 2, 35.0, 70000, '2024-02-20 16:50:00'),
(4, 3, 25.0, 50000, '2024-02-20 16:50:00'),

-- Cost 5 (Insurance 3M) - 3 users
(5, 1, 40.0, 1200000, '2024-01-05 11:05:00'),
(5, 2, 35.0, 1050000, '2024-01-05 11:05:00'),
(5, 3, 25.0, 750000, '2024-01-05 11:05:00'),

-- Cost 6 (Inspection 500k) - 3 users
(6, 1, 40.0, 200000, '2024-01-25 13:35:00'),
(6, 2, 35.0, 175000, '2024-01-25 13:35:00'),
(6, 3, 25.0, 125000, '2024-01-25 13:35:00'),

-- Cost 7 (ElectricCharge 480k) - 3 users
(7, 1, 40.0, 192000, '2024-03-15 08:50:00'),
(7, 2, 35.0, 168000, '2024-03-15 08:50:00'),
(7, 3, 25.0, 120000, '2024-03-15 08:50:00'),

-- Cost 8 (Maintenance 1.5M) - 3 users
(8, 1, 40.0, 600000, '2024-03-10 15:25:00'),
(8, 2, 35.0, 525000, '2024-03-10 15:25:00'),
(8, 3, 25.0, 375000, '2024-03-10 15:25:00'),

-- Cost 9 (Other 300k) - 3 users
(9, 1, 40.0, 120000, '2024-03-25 12:15:00'),
(9, 2, 35.0, 105000, '2024-03-25 12:15:00'),
(9, 3, 25.0, 75000, '2024-03-25 12:15:00');

-- Insert sample payments
INSERT INTO Payment (userId, costId, method, amount, transactionCode, paymentDate, status) VALUES 
-- User 1 payments
(1, 1, 'EWallet', 200000, 'EW001', '2024-01-15 11:00:00', 'Completed'),
(1, 2, 'Banking', 800000, 'BK001', '2024-01-10 15:00:00', 'Completed'),
(1, 3, 'EWallet', 180000, 'EW002', '2024-02-15 10:00:00', 'Completed'),
(1, 4, 'EWallet', 80000, 'EW003', '2024-02-20 17:00:00', 'Completed'),
(1, 5, 'Banking', 1200000, 'BK002', '2024-01-05 12:00:00', 'Completed'),
(1, 6, 'EWallet', 200000, 'EW004', '2024-01-25 14:00:00', 'Completed'),
(1, 7, 'EWallet', 192000, 'EW005', '2024-03-15 09:00:00', 'Completed'),
(1, 8, 'Banking', 600000, 'BK003', '2024-03-10 16:00:00', 'Completed'),
(1, 9, 'EWallet', 120000, 'EW006', '2024-03-25 13:00:00', 'Completed'),

-- User 2 payments
(2, 1, 'EWallet', 175000, 'EW007', '2024-01-15 11:30:00', 'Completed'),
(2, 2, 'Banking', 700000, 'BK004', '2024-01-10 15:30:00', 'Completed'),
(2, 3, 'EWallet', 157500, 'EW008', '2024-02-15 10:30:00', 'Completed'),
(2, 4, 'EWallet', 70000, 'EW009', '2024-02-20 17:30:00', 'Completed'),
(2, 5, 'Banking', 1050000, 'BK005', '2024-01-05 12:30:00', 'Completed'),
(2, 6, 'EWallet', 175000, 'EW010', '2024-01-25 14:30:00', 'Completed'),
(2, 7, 'EWallet', 168000, 'EW011', '2024-03-15 09:30:00', 'Completed'),
(2, 8, 'Banking', 525000, 'BK006', '2024-03-10 16:30:00', 'Completed'),
(2, 9, 'EWallet', 105000, 'EW012', '2024-03-25 13:30:00', 'Completed'),

-- User 3 payments
(3, 1, 'EWallet', 125000, 'EW013', '2024-01-15 12:00:00', 'Completed'),
(3, 2, 'Banking', 500000, 'BK007', '2024-01-10 16:00:00', 'Completed'),
(3, 3, 'EWallet', 112500, 'EW014', '2024-02-15 11:00:00', 'Completed'),
(3, 4, 'EWallet', 50000, 'EW015', '2024-02-20 18:00:00', 'Completed'),
(3, 5, 'Banking', 750000, 'BK008', '2024-01-05 13:00:00', 'Completed'),
(3, 6, 'EWallet', 125000, 'EW016', '2024-01-25 15:00:00', 'Completed'),
(3, 7, 'EWallet', 120000, 'EW017', '2024-03-15 10:00:00', 'Completed'),
(3, 8, 'Banking', 375000, 'BK009', '2024-03-10 17:00:00', 'Completed'),
(3, 9, 'EWallet', 75000, 'EW018', '2024-03-25 14:00:00', 'Completed'),

-- Some pending payments
(1, 10, 'EWallet', 240000, 'EW019', '2024-03-16 11:00:00', 'Pending'),
(2, 10, 'EWallet', 210000, 'EW020', '2024-03-16 11:30:00', 'Pending'),
(3, 10, 'EWallet', 150000, 'EW021', '2024-03-16 12:00:00', 'Pending'),

-- Some failed payments
(1, 11, 'Banking', 1000000, 'BK010', '2024-01-12 16:00:00', 'Failed'),
(2, 11, 'Banking', 875000, 'BK011', '2024-01-12 16:30:00', 'Failed'),
(3, 11, 'Banking', 625000, 'BK012', '2024-01-12 17:00:00', 'Failed');

-- Update GroupFund with new data
UPDATE GroupFund SET 
    totalContributed = 15000000,
    currentBalance = 12000000,
    updatedAt = NOW()
WHERE groupId = 1;

UPDATE GroupFund SET 
    totalContributed = 12000000,
    currentBalance = 9000000,
    updatedAt = NOW()
WHERE groupId = 2;

-- Insert new fund transactions
INSERT INTO FundTransaction (fundId, userId, transactionType, amount, purpose, date) VALUES 
(1, 1, 'Deposit', 5000000, 'Đóng góp quỹ chung Tesla Model 3', '2024-01-01 10:00:00'),
(1, 2, 'Deposit', 4000000, 'Đóng góp quỹ chung Tesla Model 3', '2024-01-01 10:30:00'),
(1, 3, 'Deposit', 3000000, 'Đóng góp quỹ chung Tesla Model 3', '2024-01-01 11:00:00'),
(1, 1, 'Deposit', 2000000, 'Bổ sung quỹ chung Tesla Model 3', '2024-02-01 10:00:00'),
(1, 2, 'Deposit', 1500000, 'Bổ sung quỹ chung Tesla Model 3', '2024-02-01 10:30:00'),
(1, 3, 'Deposit', 1000000, 'Bổ sung quỹ chung Tesla Model 3', '2024-02-01 11:00:00'),

(2, 2, 'Deposit', 4000000, 'Đóng góp quỹ chung BMW i3', '2024-01-01 12:00:00'),
(2, 4, 'Deposit', 3000000, 'Đóng góp quỹ chung BMW i3', '2024-01-01 12:30:00'),
(2, 2, 'Deposit', 2000000, 'Bổ sung quỹ chung BMW i3', '2024-02-01 12:00:00'),
(2, 4, 'Deposit', 1500000, 'Bổ sung quỹ chung BMW i3', '2024-02-01 12:30:00');

-- Show summary
SELECT 'Cost_Payment_DB Sample Data Inserted Successfully!' as Status;

-- Show cost summary
SELECT 
    'Cost Summary' as Report,
    COUNT(*) as TotalCosts,
    SUM(amount) as TotalAmount,
    AVG(amount) as AverageAmount
FROM Cost;

-- Show cost type breakdown
SELECT 
    costType as 'Cost Type',
    COUNT(*) as Count,
    SUM(amount) as TotalAmount,
    AVG(amount) as AverageAmount
FROM Cost 
GROUP BY costType 
ORDER BY TotalAmount DESC;

-- Show vehicle breakdown
SELECT 
    vehicleId as 'Vehicle ID',
    COUNT(*) as Count,
    SUM(amount) as TotalAmount,
    AVG(amount) as AverageAmount
FROM Cost 
GROUP BY vehicleId 
ORDER BY TotalAmount DESC;

-- Show payment summary
SELECT 
    status as 'Payment Status',
    COUNT(*) as Count,
    SUM(amount) as TotalAmount
FROM Payment 
GROUP BY status 
ORDER BY TotalAmount DESC;

-- Show cost share summary
SELECT 
    cs.costId as 'Cost ID',
    c.description as 'Cost Description',
    c.amount as 'Total Amount',
    COUNT(cs.shareId) as 'Number of Shares',
    SUM(cs.amountShare) as 'Total Shared Amount'
FROM CostShare cs
JOIN Cost c ON cs.costId = c.costId
GROUP BY cs.costId, c.description, c.amount
ORDER BY cs.costId;
