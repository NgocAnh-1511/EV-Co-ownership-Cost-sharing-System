-- Script để tạo database ai_ev (mới)
-- Chạy script này trước khi start AIService

-- Xóa database cũ nếu tồn tại
DROP DATABASE IF EXISTS ev_ai;

-- Tạo database mới
CREATE DATABASE IF NOT EXISTS ai_ev 
CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

USE ai_ev;

-- The tables will be created automatically by Hibernate (ddl-auto=update)
-- But you can also run the schema.sql file manually if needed

SHOW TABLES;


