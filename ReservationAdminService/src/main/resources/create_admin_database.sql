-- Tạo database mới cho Admin Service
CREATE DATABASE IF NOT EXISTS co_ownership_booking_admin;

USE co_ownership_booking_admin;

-- Tạo các bảng giống như booking database
-- Bảng groups
CREATE TABLE IF NOT EXISTS `groups` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `created_at` DATETIME(6),
  `description` TEXT,
  `name` VARCHAR(255),
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Bảng vehicles (khớp với booking DB)
CREATE TABLE IF NOT EXISTS vehicles (
  id BIGINT NOT NULL AUTO_INCREMENT,
  vehicle_name VARCHAR(255),
  vehicle_type VARCHAR(255),
  license_plate VARCHAR(255),
  group_id BIGINT,
  status VARCHAR(50),
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Bảng reservations (khớp với booking DB)
CREATE TABLE IF NOT EXISTS reservations (
  id BIGINT NOT NULL AUTO_INCREMENT,
  user_id BIGINT,
  vehicle_id BIGINT,
  start_datetime DATETIME(6),
  end_datetime DATETIME(6),
  purpose TEXT,
  status VARCHAR(50),
  created_at DATETIME(6),
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Bảng users (nếu cần)
CREATE TABLE IF NOT EXISTS users (
  id BIGINT NOT NULL AUTO_INCREMENT,
  username VARCHAR(255) UNIQUE,
  email VARCHAR(255),
  password VARCHAR(255),
  role VARCHAR(50),
  created_at DATETIME(6),
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

