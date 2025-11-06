-- AI Service Database Schema
-- Database: ev_ai

-- Table: ownership_info
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

-- Table: usage_analysis
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

-- Table: fairness_score
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

-- Table: ai_recommendations
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
    period_start TIMESTAMP,
    period_end TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    read_at TIMESTAMP NULL,
    INDEX idx_group (group_id),
    INDEX idx_vehicle (vehicle_id),
    INDEX idx_target_user (target_user_id),
    INDEX idx_status (status),
    INDEX idx_severity (severity),
    INDEX idx_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Sample data for testing
-- Note: Adjust user_id and vehicle_id based on your ReservationService database

-- Example ownership data (3 co-owners with 40%, 30%, 30%)
INSERT INTO ownership_info (user_id, vehicle_id, group_id, ownership_percentage, role) VALUES
(1, 1, 1, 40.0, 'ADMIN'),
(2, 1, 1, 30.0, 'MEMBER'),
(3, 1, 1, 30.0, 'MEMBER')
ON DUPLICATE KEY UPDATE ownership_percentage = VALUES(ownership_percentage);


