-- ==========================================================
--  AWS Cloud Governance Tool – Sprint 1 Database Schema
--  Module: User & Cloud Account Onboarding + Basic Cost Dashboard
--
--  Sprint 1 tables required:
--    1. users            (US-01: Register / Login)
--    2. aws_credentials  (US-02: Save credentials)
--    3. billing_records  (US-03: Cost totals – will be empty until Sprint 2 syncs)
--    4. ec2_instances    (US-03: Resource count – 0 until Sprint 2 syncs)
--    5. rds_instances    (US-03: Resource count – 0 until Sprint 2 syncs)
--    6. ecs_services     (US-03: Resource count – 0 until Sprint 2 syncs)
-- ==========================================================

-- Create / select DB
CREATE DATABASE IF NOT EXISTS aws_governance_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE aws_governance_db;

-- ── 1. Users table ───────────────────────────────────────
CREATE TABLE IF NOT EXISTS users (
    user_id    INT          PRIMARY KEY AUTO_INCREMENT,
    username   VARCHAR(50)  UNIQUE NOT NULL,
    password   VARCHAR(255) NOT NULL,
    email      VARCHAR(100),
    full_name  VARCHAR(100),
    role       VARCHAR(20)  DEFAULT 'user',
    created_at TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    last_login TIMESTAMP    NULL
);

-- Default admin account (password: admin123 – change in production!)
INSERT IGNORE INTO users (username, password, email, full_name, role)
VALUES ('admin', 'admin123', 'admin@localhost', 'System Administrator', 'admin');

-- ── 2. AWS Credentials table ─────────────────────────────
--      Linked to users; encrypted/obfuscated storage in Sprint 2
CREATE TABLE IF NOT EXISTS aws_credentials (
    credential_id  INT          PRIMARY KEY AUTO_INCREMENT,
    user_id        INT          NOT NULL,
    access_key     VARCHAR(255) NOT NULL,
    secret_key     VARCHAR(255) NOT NULL,
    region         VARCHAR(50)  NOT NULL,
    remaining_credits DOUBLE   DEFAULT 0.0,
    is_active      BOOLEAN      DEFAULT TRUE,
    validated      BOOLEAN      DEFAULT FALSE,
    created_at     TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    updated_at     TIMESTAMP    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uq_user_active (user_id, is_active),
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- ── 3. Billing records table ─────────────────────────────
--      Empty in Sprint 1; populated by AWS Cost Explorer sync in Sprint 2
CREATE TABLE IF NOT EXISTS billing_records (
    record_id    INT           PRIMARY KEY AUTO_INCREMENT,
    user_id      INT           NOT NULL,
    service_name VARCHAR(100),
    cost_amount  DECIMAL(10,2) DEFAULT 0.00,
    currency     VARCHAR(10)   DEFAULT 'USD',
    start_date   DATE,
    end_date     DATE,
    record_type  VARCHAR(50)   DEFAULT 'monthly',
    created_at   TIMESTAMP     DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- ── 4. EC2 instances stub ────────────────────────────────
--      Empty in Sprint 1; populated by EC2 sync in Sprint 2
CREATE TABLE IF NOT EXISTS ec2_instances (
    record_id         INT         PRIMARY KEY AUTO_INCREMENT,
    instance_id       VARCHAR(50) NOT NULL,
    instance_type     VARCHAR(50),
    instance_state    VARCHAR(20),
    availability_zone VARCHAR(50),
    launch_time       TIMESTAMP   NULL,
    cpu_utilization   DOUBLE,
    is_idle           BOOLEAN     DEFAULT FALSE,
    last_checked      TIMESTAMP   DEFAULT CURRENT_TIMESTAMP,
    user_id           INT,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE SET NULL
);

-- ── 5. RDS instances stub ────────────────────────────────
CREATE TABLE IF NOT EXISTS rds_instances (
    record_id                INT          PRIMARY KEY AUTO_INCREMENT,
    db_instance_identifier   VARCHAR(100) NOT NULL,
    db_instance_class        VARCHAR(50),
    engine                   VARCHAR(50),
    db_instance_status       VARCHAR(50),
    allocated_storage        INT,
    cpu_utilization          DOUBLE,
    is_idle                  BOOLEAN      DEFAULT FALSE,
    last_checked             TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    user_id                  INT,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE SET NULL
);

-- ── 6. ECS services stub ─────────────────────────────────
CREATE TABLE IF NOT EXISTS ecs_services (
    record_id        INT          PRIMARY KEY AUTO_INCREMENT,
    service_name     VARCHAR(100) NOT NULL,
    cluster_name     VARCHAR(100),
    service_status   VARCHAR(50),
    desired_count    INT          DEFAULT 0,
    running_count    INT          DEFAULT 0,
    pending_count    INT          DEFAULT 0,
    is_idle          BOOLEAN      DEFAULT FALSE,
    last_checked     TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    user_id          INT,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE SET NULL
);

-- ==========================================================
--  Quick verification queries (run manually after import)
-- ==========================================================
-- SELECT * FROM users;
-- SELECT COUNT(*) AS total_users FROM users;
-- SHOW TABLES;
