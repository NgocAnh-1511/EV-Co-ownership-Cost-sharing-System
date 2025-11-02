-- ========================================
-- AI Service - Sample Data
-- Database: ev_ai
-- ========================================

-- Clear existing data (for testing)
SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE ai_recommendations;
TRUNCATE TABLE fairness_score;
TRUNCATE TABLE usage_analysis;
TRUNCATE TABLE ownership_info;
SET FOREIGN_KEY_CHECKS = 1;

-- ========================================
-- 1. OWNERSHIP_INFO - Thông tin sở hữu
-- ========================================
-- Group 1: Tesla Model 3 (3 co-owners)
INSERT INTO ownership_info (user_id, vehicle_id, group_id, ownership_percentage, role, joined_date) VALUES
(1, 1, 1, 40.0, 'ADMIN', '2024-01-01 08:00:00'),
(2, 1, 1, 35.0, 'MEMBER', '2024-01-01 08:00:00'),
(3, 1, 1, 25.0, 'MEMBER', '2024-01-15 09:00:00');

-- Group 2: VinFast VF8 (2 co-owners)
INSERT INTO ownership_info (user_id, vehicle_id, group_id, ownership_percentage, role, joined_date) VALUES
(4, 2, 2, 60.0, 'ADMIN', '2024-02-01 10:00:00'),
(5, 2, 2, 40.0, 'MEMBER', '2024-02-01 10:00:00');

-- Group 3: BMW i4 (4 co-owners)
INSERT INTO ownership_info (user_id, vehicle_id, group_id, ownership_percentage, role, joined_date) VALUES
(6, 3, 3, 30.0, 'ADMIN', '2024-03-01 11:00:00'),
(7, 3, 3, 25.0, 'MEMBER', '2024-03-01 11:00:00'),
(8, 3, 3, 25.0, 'MEMBER', '2024-03-05 12:00:00'),
(9, 3, 3, 20.0, 'MEMBER', '2024-03-10 13:00:00');

-- ========================================
-- 2. USAGE_ANALYSIS - Phân tích sử dụng
-- ========================================
-- Group 1 - Tesla Model 3 (Tháng 10/2024)
INSERT INTO usage_analysis (user_id, vehicle_id, group_id, total_hours_used, total_kilometers, 
    booking_count, cancellation_count, usage_percentage, cost_incurred, period_start, period_end, analyzed_at) VALUES
-- User 1: Sử dụng nhiều hơn ownership (40% ownership, 45% usage)
(1, 1, 1, 36.5, 450.0, 12, 1, 45.0, 2250000, '2024-10-01 00:00:00', '2024-10-31 23:59:59', '2024-11-01 01:00:00'),
-- User 2: Sử dụng ít hơn ownership (35% ownership, 30% usage)
(2, 1, 1, 24.3, 300.0, 8, 0, 30.0, 1500000, '2024-10-01 00:00:00', '2024-10-31 23:59:59', '2024-11-01 01:00:00'),
-- User 3: Sử dụng đúng ownership (25% ownership, 25% usage)
(3, 1, 1, 20.2, 250.0, 7, 2, 25.0, 1250000, '2024-10-01 00:00:00', '2024-10-31 23:59:59', '2024-11-01 01:00:00');

-- Group 1 - Tesla Model 3 (Tháng 9/2024)
INSERT INTO usage_analysis (user_id, vehicle_id, group_id, total_hours_used, total_kilometers, 
    booking_count, cancellation_count, usage_percentage, cost_incurred, period_start, period_end, analyzed_at) VALUES
(1, 1, 1, 32.0, 400.0, 10, 0, 42.0, 2000000, '2024-09-01 00:00:00', '2024-09-30 23:59:59', '2024-10-01 01:00:00'),
(2, 1, 1, 28.0, 350.0, 9, 1, 35.0, 1750000, '2024-09-01 00:00:00', '2024-09-30 23:59:59', '2024-10-01 01:00:00'),
(3, 1, 1, 18.0, 220.0, 6, 0, 23.0, 1100000, '2024-09-01 00:00:00', '2024-09-30 23:59:59', '2024-10-01 01:00:00');

-- Group 2 - VinFast VF8 (Tháng 10/2024)
INSERT INTO usage_analysis (user_id, vehicle_id, group_id, total_hours_used, total_kilometers, 
    booking_count, cancellation_count, usage_percentage, cost_incurred, period_start, period_end, analyzed_at) VALUES
-- User 4: Sử dụng đúng ownership (60% ownership, 58% usage)
(4, 2, 2, 46.4, 580.0, 15, 1, 58.0, 2900000, '2024-10-01 00:00:00', '2024-10-31 23:59:59', '2024-11-01 02:00:00'),
-- User 5: Sử dụng nhiều hơn ownership (40% ownership, 42% usage)
(5, 2, 2, 33.6, 420.0, 11, 0, 42.0, 2100000, '2024-10-01 00:00:00', '2024-10-31 23:59:59', '2024-11-01 02:00:00');

-- Group 3 - BMW i4 (Tháng 10/2024)
INSERT INTO usage_analysis (user_id, vehicle_id, group_id, total_hours_used, total_kilometers, 
    booking_count, cancellation_count, usage_percentage, cost_incurred, period_start, period_end, analyzed_at) VALUES
-- User 6: Sử dụng ít (30% ownership, 20% usage)
(6, 3, 3, 16.0, 200.0, 5, 0, 20.0, 1000000, '2024-10-01 00:00:00', '2024-10-31 23:59:59', '2024-11-01 03:00:00'),
-- User 7: Sử dụng nhiều (25% ownership, 35% usage)
(7, 3, 3, 28.0, 350.0, 10, 1, 35.0, 1750000, '2024-10-01 00:00:00', '2024-10-31 23:59:59', '2024-11-01 03:00:00'),
-- User 8: Sử dụng đúng (25% ownership, 25% usage)
(8, 3, 3, 20.0, 250.0, 7, 0, 25.0, 1250000, '2024-10-01 00:00:00', '2024-10-31 23:59:59', '2024-11-01 03:00:00'),
-- User 9: Sử dụng đúng (20% ownership, 20% usage)
(9, 3, 3, 16.0, 200.0, 6, 2, 20.0, 1000000, '2024-10-01 00:00:00', '2024-10-31 23:59:59', '2024-11-01 03:00:00');

-- ========================================
-- 3. FAIRNESS_SCORE - Điểm công bằng
-- ========================================
-- Group 1 - Tesla Model 3 (Tháng 10/2024)
INSERT INTO fairness_score (user_id, vehicle_id, group_id, ownership_percentage, usage_percentage, 
    difference, fairness_score, priority, period_start, period_end, calculated_at) VALUES
-- User 1: Sử dụng nhiều hơn 5% -> Priority LOW
(1, 1, 1, 40.0, 45.0, 5.0, 75.0, 'LOW', '2024-10-01 00:00:00', '2024-10-31 23:59:59', '2024-11-01 01:30:00'),
-- User 2: Sử dụng ít hơn 5% -> Priority HIGH
(2, 1, 1, 35.0, 30.0, -5.0, 85.0, 'HIGH', '2024-10-01 00:00:00', '2024-10-31 23:59:59', '2024-11-01 01:30:00'),
-- User 3: Sử dụng đúng -> Priority NORMAL
(3, 1, 1, 25.0, 25.0, 0.0, 100.0, 'NORMAL', '2024-10-01 00:00:00', '2024-10-31 23:59:59', '2024-11-01 01:30:00');

-- Group 1 - Tesla Model 3 (Tháng 9/2024)
INSERT INTO fairness_score (user_id, vehicle_id, group_id, ownership_percentage, usage_percentage, 
    difference, fairness_score, priority, period_start, period_end, calculated_at) VALUES
(1, 1, 1, 40.0, 42.0, 2.0, 90.0, 'NORMAL', '2024-09-01 00:00:00', '2024-09-30 23:59:59', '2024-10-01 01:30:00'),
(2, 1, 1, 35.0, 35.0, 0.0, 100.0, 'NORMAL', '2024-09-01 00:00:00', '2024-09-30 23:59:59', '2024-10-01 01:30:00'),
(3, 1, 1, 25.0, 23.0, -2.0, 92.0, 'NORMAL', '2024-09-01 00:00:00', '2024-09-30 23:59:59', '2024-10-01 01:30:00');

-- Group 2 - VinFast VF8 (Tháng 10/2024)
INSERT INTO fairness_score (user_id, vehicle_id, group_id, ownership_percentage, usage_percentage, 
    difference, fairness_score, priority, period_start, period_end, calculated_at) VALUES
(4, 2, 2, 60.0, 58.0, -2.0, 95.0, 'NORMAL', '2024-10-01 00:00:00', '2024-10-31 23:59:59', '2024-11-01 02:30:00'),
(5, 2, 2, 40.0, 42.0, 2.0, 93.0, 'NORMAL', '2024-10-01 00:00:00', '2024-10-31 23:59:59', '2024-11-01 02:30:00');

-- Group 3 - BMW i4 (Tháng 10/2024)
INSERT INTO fairness_score (user_id, vehicle_id, group_id, ownership_percentage, usage_percentage, 
    difference, fairness_score, priority, period_start, period_end, calculated_at) VALUES
-- User 6: Sử dụng ít hơn 10% -> Priority CRITICAL
(6, 3, 3, 30.0, 20.0, -10.0, 70.0, 'CRITICAL', '2024-10-01 00:00:00', '2024-10-31 23:59:59', '2024-11-01 03:30:00'),
-- User 7: Sử dụng nhiều hơn 10% -> Priority CRITICAL (ngược lại)
(7, 3, 3, 25.0, 35.0, 10.0, 65.0, 'CRITICAL', '2024-10-01 00:00:00', '2024-10-31 23:59:59', '2024-11-01 03:30:00'),
(8, 3, 3, 25.0, 25.0, 0.0, 100.0, 'NORMAL', '2024-10-01 00:00:00', '2024-10-31 23:59:59', '2024-11-01 03:30:00'),
(9, 3, 3, 20.0, 20.0, 0.0, 100.0, 'NORMAL', '2024-10-01 00:00:00', '2024-10-31 23:59:59', '2024-11-01 03:30:00');

-- ========================================
-- 4. AI_RECOMMENDATIONS - Gợi ý AI
-- ========================================
-- Group 1 - Tesla Model 3
INSERT INTO ai_recommendations (group_id, vehicle_id, type, title, description, severity, 
    target_user_id, status, period_start, period_end, created_at) VALUES
-- Cảnh báo sử dụng không công bằng
(1, 1, 'FAIRNESS_ALERT', 
    'Mất cân bằng sử dụng xe trong tháng 10/2024', 
    'Người dùng #1 đã sử dụng 45% (cao hơn 5% so với quyền sở hữu 40%). Người dùng #2 chỉ sử dụng 30% (thấp hơn 5% so với quyền sở hữu 35%). Đề xuất điều chỉnh lịch đặt xe để đảm bảo công bằng.',
    'WARNING', NULL, 'ACTIVE', '2024-10-01 00:00:00', '2024-10-31 23:59:59', '2024-11-01 01:45:00'),

-- Gợi ý ưu tiên booking
(1, 1, 'PRIORITY_SUGGESTION', 
    'Ưu tiên booking cho User #2', 
    'User #2 (35% ownership) đã sử dụng ít hơn quyền sở hữu trong 2 tháng liên tiếp. Đề xuất ưu tiên booking trong tháng tới để cân bằng.',
    'INFO', 2, 'ACTIVE', '2024-10-01 00:00:00', '2024-10-31 23:59:59', '2024-11-01 01:50:00'),

-- Cảnh báo hủy booking nhiều
(1, 1, 'USAGE_PATTERN', 
    'User #3 có tỷ lệ hủy booking cao', 
    'User #3 đã hủy 2/7 booking trong tháng 10 (28.6%). Đề xuất xem xét lý do và điều chỉnh thói quen đặt xe.',
    'INFO', 3, 'ACTIVE', '2024-10-01 00:00:00', '2024-10-31 23:59:59', '2024-11-01 02:00:00'),

-- Group 2 - VinFast VF8
(2, 2, 'FAIRNESS_ALERT', 
    'Sử dụng xe cân bằng tốt', 
    'Tất cả thành viên đang sử dụng xe gần đúng với tỷ lệ sở hữu. Tiếp tục duy trì!',
    'INFO', NULL, 'ACTIVE', '2024-10-01 00:00:00', '2024-10-31 23:59:59', '2024-11-01 02:15:00'),

-- Group 3 - BMW i4
(3, 3, 'FAIRNESS_ALERT', 
    '⚠️ Mất cân bằng nghiêm trọng trong Group 3', 
    'User #6 chỉ sử dụng 20% (thấp hơn 10% so với ownership 30%). User #7 sử dụng 35% (cao hơn 10% so với ownership 25%). Cần điều chỉnh ngay!',
    'CRITICAL', NULL, 'ACTIVE', '2024-10-01 00:00:00', '2024-10-31 23:59:59', '2024-11-01 03:45:00'),

(3, 3, 'PRIORITY_SUGGESTION', 
    'Ưu tiên cao cho User #6', 
    'User #6 cần được ưu tiên booking trong 2 tháng tới để bù đắp mức sử dụng thấp. Đề xuất tự động chấp nhận booking của User #6.',
    'CRITICAL', 6, 'ACTIVE', '2024-10-01 00:00:00', '2024-10-31 23:59:59', '2024-11-01 03:50:00'),

(3, 3, 'USAGE_PATTERN', 
    'User #7 sử dụng quá mức', 
    'User #7 đã sử dụng vượt 10% so với quyền sở hữu. Đề xuất giảm số lượng booking trong tháng tới hoặc xem xét tăng ownership percentage.',
    'WARNING', 7, 'ACTIVE', '2024-10-01 00:00:00', '2024-10-31 23:59:59', '2024-11-01 03:55:00'),

-- Recommendation đã đọc (ví dụ)
(1, 1, 'COST_OPTIMIZATION', 
    'Tối ưu chi phí sạc điện', 
    'Phân tích cho thấy việc sạc vào ban đêm (22h-6h) có thể tiết kiệm 30% chi phí điện. Đề xuất lên lịch sạc tự động.',
    'INFO', NULL, 'ACTIVE', '2024-09-01 00:00:00', '2024-09-30 23:59:59', '2024-10-01 08:00:00'),

-- Recommendation đã đóng
(2, 2, 'MAINTENANCE_ALERT', 
    'Đã hoàn thành bảo dưỡng định kỳ', 
    'Xe đã chạy được 10,000 km. Bảo dưỡng định kỳ đã hoàn thành ngày 25/10/2024.',
    'INFO', NULL, 'CLOSED', '2024-09-01 00:00:00', '2024-10-31 23:59:59', '2024-10-15 10:00:00');

-- Update read_at cho recommendation đã đọc
UPDATE ai_recommendations 
SET read_at = '2024-10-01 09:30:00' 
WHERE type = 'COST_OPTIMIZATION' AND group_id = 1;

-- ========================================
-- Summary Statistics
-- ========================================
-- Total records inserted:
-- - ownership_info: 9 records (3 groups)
-- - usage_analysis: 13 records (multiple periods)
-- - fairness_score: 13 records (matching usage_analysis)
-- - ai_recommendations: 9 records (various types and severities)
-- ========================================

SELECT 'Data insertion completed successfully!' AS status;
SELECT 
    (SELECT COUNT(*) FROM ownership_info) AS ownership_count,
    (SELECT COUNT(*) FROM usage_analysis) AS usage_analysis_count,
    (SELECT COUNT(*) FROM fairness_score) AS fairness_score_count,
    (SELECT COUNT(*) FROM ai_recommendations) AS recommendations_count;



