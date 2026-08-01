-- 测试用 schema — 仅创建认证测试需要的 users 表
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(100) NOT NULL,
    password VARCHAR(255),
    role VARCHAR(50) DEFAULT 'user',
    display_name VARCHAR(200),
    platform VARCHAR(20) DEFAULT 'gg',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_login TIMESTAMP NULL,
    created_by BIGINT NULL,
    config VARCHAR(2000) DEFAULT '{}',
    custom_name VARCHAR(200) DEFAULT '',
    email VARCHAR(200) DEFAULT '',
    telegram_username VARCHAR(200) DEFAULT '',
    token_version INT DEFAULT 0
);

-- 测试账户 (BCrypt for '1976xiaobai')
INSERT INTO users (id, username, password, role, display_name, platform)
VALUES (1, 'carl567', '$2a$10$HZ1fRn4UryGjpT2rPTqVx.N4hYFv9vx3LXwQWZfrYT7I2DZd93lru', 'developer', '系统管理员', 'gg');
