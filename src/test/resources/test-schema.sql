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

-- 产品表 (用于 ProductServiceTest)
CREATE TABLE IF NOT EXISTS products (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_name VARCHAR(500),
    kpi VARCHAR(500),
    region VARCHAR(200),
    status VARCHAR(100) DEFAULT 'active',
    customer VARCHAR(500),
    sales_person_id BIGINT NULL,
    mcc_id BIGINT NULL,
    agency_ratio DOUBLE NULL,
    owner_id BIGINT NULL,
    runner_ids VARCHAR(1000) DEFAULT '[]',
    is_archived BIGINT DEFAULT 0,
    deleted_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 测试账户 (BCrypt for '1976xiaobai')
INSERT INTO users (id, username, password, role, display_name, platform)
VALUES (1, 'carl567', '$2a$10$HZ1fRn4UryGjpT2rPTqVx.N4hYFv9vx3LXwQWZfrYT7I2DZd93lru', 'developer', '系统管理员', 'gg');
