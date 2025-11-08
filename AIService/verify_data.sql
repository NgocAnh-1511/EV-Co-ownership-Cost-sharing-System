-- ========================================
-- Verify Sample Data Import
-- Database: ev_ai
-- ========================================

USE ev_ai;

SELECT '========================================' AS '';
SELECT '  DATA VERIFICATION REPORT' AS '';
SELECT '========================================' AS '';
SELECT '' AS '';

-- 1. Table Record Counts
SELECT '1. TABLE RECORD COUNTS' AS '';
SELECT '------------------------' AS '';
SELECT 
    'ownership_info' as 'Table', 
    COUNT(*) as 'Records',
    CASE 
        WHEN COUNT(*) = 9 THEN '✓ OK'
        ELSE '✗ FAIL (Expected: 9)'
    END as 'Status'
FROM ownership_info
UNION ALL
SELECT 
    'usage_analysis', 
    COUNT(*),
    CASE 
        WHEN COUNT(*) = 13 THEN '✓ OK'
        ELSE '✗ FAIL (Expected: 13)'
    END
FROM usage_analysis
UNION ALL
SELECT 
    'fairness_score', 
    COUNT(*),
    CASE 
        WHEN COUNT(*) = 13 THEN '✓ OK'
        ELSE '✗ FAIL (Expected: 13)'
    END
FROM fairness_score
UNION ALL
SELECT 
    'ai_recommendations', 
    COUNT(*),
    CASE 
        WHEN COUNT(*) = 9 THEN '✓ OK'
        ELSE '✗ FAIL (Expected: 9)'
    END
FROM ai_recommendations;

SELECT '' AS '';

-- 2. Ownership Distribution by Group
SELECT '2. OWNERSHIP DISTRIBUTION' AS '';
SELECT '------------------------' AS '';
SELECT 
    group_id as 'Group',
    COUNT(*) as 'Co-owners',
    CONCAT(SUM(ownership_percentage), '%') as 'Total %',
    CASE 
        WHEN SUM(ownership_percentage) = 100 THEN '✓ OK'
        ELSE '✗ FAIL (Should be 100%)'
    END as 'Status'
FROM ownership_info
GROUP BY group_id
ORDER BY group_id;

SELECT '' AS '';

-- 3. Recommendations by Severity
SELECT '3. RECOMMENDATIONS BY SEVERITY' AS '';
SELECT '------------------------' AS '';
SELECT 
    severity as 'Severity',
    COUNT(*) as 'Count',
    CONCAT(ROUND(COUNT(*) * 100.0 / (SELECT COUNT(*) FROM ai_recommendations), 1), '%') as 'Percentage'
FROM ai_recommendations
GROUP BY severity
ORDER BY 
    CASE severity
        WHEN 'CRITICAL' THEN 1
        WHEN 'WARNING' THEN 2
        WHEN 'INFO' THEN 3
    END;

SELECT '' AS '';

-- 4. Recommendations by Type
SELECT '4. RECOMMENDATIONS BY TYPE' AS '';
SELECT '------------------------' AS '';
SELECT 
    type as 'Type',
    COUNT(*) as 'Count'
FROM ai_recommendations
GROUP BY type
ORDER BY COUNT(*) DESC;

SELECT '' AS '';

-- 5. Fairness Score Summary
SELECT '5. FAIRNESS SCORE SUMMARY' AS '';
SELECT '------------------------' AS '';
SELECT 
    priority as 'Priority',
    COUNT(*) as 'Users',
    CONCAT(ROUND(AVG(fairness_score), 1), ' pts') as 'Avg Score',
    CONCAT(ROUND(AVG(ABS(difference)), 1), '%') as 'Avg Diff'
FROM fairness_score
WHERE period_start = '2024-10-01 00:00:00'
GROUP BY priority
ORDER BY 
    CASE priority
        WHEN 'CRITICAL' THEN 1
        WHEN 'HIGH' THEN 2
        WHEN 'NORMAL' THEN 3
        WHEN 'LOW' THEN 4
    END;

SELECT '' AS '';

-- 6. Usage Analysis Summary (October 2024)
SELECT '6. USAGE ANALYSIS (October 2024)' AS '';
SELECT '------------------------' AS '';
SELECT 
    group_id as 'Group',
    COUNT(DISTINCT user_id) as 'Users',
    CONCAT(ROUND(SUM(total_hours_used), 1), ' hrs') as 'Total Hours',
    CONCAT(ROUND(SUM(total_kilometers), 0), ' km') as 'Total KM',
    SUM(booking_count) as 'Bookings',
    CONCAT(FORMAT(SUM(cost_incurred), 0), ' VND') as 'Total Cost'
FROM usage_analysis
WHERE period_start = '2024-10-01 00:00:00'
GROUP BY group_id
ORDER BY group_id;

SELECT '' AS '';

-- 7. Critical Issues (Group 3)
SELECT '7. CRITICAL ISSUES DETECTED' AS '';
SELECT '------------------------' AS '';
SELECT 
    CONCAT('User #', user_id) as 'User',
    CONCAT(ownership_percentage, '%') as 'Ownership',
    CONCAT(usage_percentage, '%') as 'Usage',
    CONCAT(difference, '%') as 'Difference',
    fairness_score as 'Score',
    priority as 'Priority'
FROM fairness_score
WHERE priority IN ('CRITICAL', 'HIGH')
    AND period_start = '2024-10-01 00:00:00'
ORDER BY 
    CASE priority
        WHEN 'CRITICAL' THEN 1
        WHEN 'HIGH' THEN 2
    END,
    ABS(difference) DESC;

SELECT '' AS '';

-- 8. Recent Recommendations (Last 5)
SELECT '8. RECENT RECOMMENDATIONS' AS '';
SELECT '------------------------' AS '';
SELECT 
    CONCAT('Group ', group_id) as 'Group',
    type as 'Type',
    severity as 'Severity',
    LEFT(title, 50) as 'Title',
    DATE_FORMAT(created_at, '%Y-%m-%d %H:%i') as 'Created'
FROM ai_recommendations
ORDER BY created_at DESC
LIMIT 5;

SELECT '' AS '';
SELECT '========================================' AS '';
SELECT '  VERIFICATION COMPLETE!' AS '';
SELECT '========================================' AS '';
SELECT '' AS '';
SELECT 'If all statuses show ✓ OK, data import was successful!' AS '';
SELECT 'You can now start AIService: mvn spring-boot:run' AS '';
SELECT '' AS '';












