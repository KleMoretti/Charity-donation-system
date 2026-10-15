-- MySQL 8.4；仅创建不存在的表，不删除、不覆盖已有数据。
-- 数据库本身须由使用者提前创建，应用连接账号需要 CREATE / SELECT / INSERT 权限。
CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT NOT NULL AUTO_INCREMENT,
    username VARCHAR(50) COLLATE utf8mb4_bin NOT NULL,
    password_hash VARCHAR(100) NOT NULL,
    role VARCHAR(10) NOT NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT uk_user_username UNIQUE (username),
    CONSTRAINT ck_user_role CHECK (role IN ('USER', 'ADMIN'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS charity_project (
    id BIGINT NOT NULL AUTO_INCREMENT,
    title VARCHAR(120) NOT NULL,
    description TEXT NOT NULL,
    target_amount DECIMAL(12,2) NOT NULL,
    status VARCHAR(10) NOT NULL DEFAULT 'DRAFT',
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    INDEX idx_project_status (status),
    CONSTRAINT ck_project_target CHECK (target_amount > 0),
    CONSTRAINT ck_project_status CHECK (status IN ('DRAFT', 'ACTIVE', 'CLOSED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS donation (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    project_id BIGINT NOT NULL,
    amount DECIMAL(12,2) NOT NULL,
    request_id VARCHAR(64) COLLATE utf8mb4_bin NOT NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT uk_donation_user_request UNIQUE (user_id, request_id),
    INDEX idx_donation_project (project_id),
    CONSTRAINT fk_donation_user FOREIGN KEY (user_id) REFERENCES sys_user(id) ON DELETE RESTRICT,
    CONSTRAINT fk_donation_project FOREIGN KEY (project_id) REFERENCES charity_project(id) ON DELETE RESTRICT,
    CONSTRAINT ck_donation_amount CHECK (amount > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
