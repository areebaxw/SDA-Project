-- AWS Cloud Governance Tool - Fresh Schema (XAMPP MySQL)
-- Run this entire script in phpMyAdmin or MySQL CLI.

DROP DATABASE IF EXISTS aws_governance_db;
CREATE DATABASE aws_governance_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE aws_governance_db;

-- Users
CREATE TABLE users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(120),
    full_name VARCHAR(120),
    role VARCHAR(20) NOT NULL DEFAULT 'user',
    last_login DATETIME NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- AWS Credentials
CREATE TABLE aws_credentials (
    credential_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    access_key VARCHAR(100) NOT NULL,
    secret_key VARCHAR(255) NOT NULL,
    region VARCHAR(50) NOT NULL,
    remaining_credits DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    validated BOOLEAN NOT NULL DEFAULT FALSE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_aws_credentials_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    CONSTRAINT uq_aws_access_key UNIQUE (access_key)
);

-- EC2 Monitoring
CREATE TABLE ec2_instances (
    record_id INT AUTO_INCREMENT PRIMARY KEY,
    instance_id VARCHAR(100) NOT NULL UNIQUE,
    instance_type VARCHAR(50),
    instance_state VARCHAR(40),
    availability_zone VARCHAR(40),
    launch_time DATETIME NULL,
    cpu_utilization DOUBLE DEFAULT 0,
    network_in DOUBLE DEFAULT 0,
    network_out DOUBLE DEFAULT 0,
    is_idle BOOLEAN NULL,
    last_checked TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    user_id INT NOT NULL,
    CONSTRAINT fk_ec2_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- S3 Monitoring
CREATE TABLE s3_buckets (
    record_id INT AUTO_INCREMENT PRIMARY KEY,
    bucket_name VARCHAR(120) NOT NULL UNIQUE,
    bucket_arn VARCHAR(255),
    region VARCHAR(50),
    object_count BIGINT NOT NULL DEFAULT 0,
    total_size_gb DOUBLE NOT NULL DEFAULT 0,
    is_public BOOLEAN NULL,
    is_idle BOOLEAN NULL,
    last_checked TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    user_id INT NOT NULL,
    CONSTRAINT fk_s3_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- SQS Monitoring
CREATE TABLE sqs_queues (
    record_id INT AUTO_INCREMENT PRIMARY KEY,
    queue_name VARCHAR(120) NOT NULL,
    queue_url VARCHAR(400) NOT NULL UNIQUE,
    queue_arn VARCHAR(255),
    message_count BIGINT NOT NULL DEFAULT 0,
    delayed_message_count BIGINT NOT NULL DEFAULT 0,
    is_idle BOOLEAN NULL,
    last_checked TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    user_id INT NOT NULL,
    CONSTRAINT fk_sqs_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- ALB Monitoring
CREATE TABLE alb_resources (
    record_id INT AUTO_INCREMENT PRIMARY KEY,
    load_balancer_name VARCHAR(150) NOT NULL,
    load_balancer_arn VARCHAR(300) NOT NULL UNIQUE,
    dns_name VARCHAR(255),
    scheme VARCHAR(30),
    state VARCHAR(50),
    request_count BIGINT NOT NULL DEFAULT 0,
    is_idle BOOLEAN NULL,
    last_checked TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    user_id INT NOT NULL,
    CONSTRAINT fk_alb_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- Billing Reports
CREATE TABLE billing_records (
    record_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    service_name VARCHAR(150) NOT NULL,
    cost_amount DECIMAL(12,4) NOT NULL DEFAULT 0,
    currency VARCHAR(10) NOT NULL DEFAULT 'USD',
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    record_type VARCHAR(30) NOT NULL DEFAULT 'monthly',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_billing_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    CONSTRAINT uq_billing_record UNIQUE (user_id, service_name, start_date, end_date)
);

-- Rule Engine
CREATE TABLE rules (
    rule_id INT AUTO_INCREMENT PRIMARY KEY,
    rule_name VARCHAR(150) NOT NULL,
    rule_type VARCHAR(50) NOT NULL,
    resource_type VARCHAR(30) NOT NULL,
    condition_metric VARCHAR(80) NOT NULL,
    condition_operator VARCHAR(5) NOT NULL,
    condition_value DOUBLE NOT NULL,
    condition_duration INT NOT NULL DEFAULT 1,
    duration_unit VARCHAR(20) NOT NULL DEFAULT 'hours',
    action_type VARCHAR(30) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_by INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_rules_user FOREIGN KEY (created_by) REFERENCES users(user_id) ON DELETE CASCADE
);

-- Alerts
CREATE TABLE alerts (
    alert_id INT AUTO_INCREMENT PRIMARY KEY,
    resource_id VARCHAR(255) NOT NULL,
    resource_type VARCHAR(30) NOT NULL,
    alert_type VARCHAR(50) NOT NULL,
    severity VARCHAR(20) NOT NULL,
    message TEXT NOT NULL,
    rule_id INT NULL,
    is_resolved BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    resolved_at DATETIME NULL,
    CONSTRAINT fk_alert_rule FOREIGN KEY (rule_id) REFERENCES rules(rule_id) ON DELETE SET NULL
);

-- Helpful indexes
CREATE INDEX idx_alerts_resolved ON alerts(is_resolved);
CREATE INDEX idx_alerts_resource_type ON alerts(resource_type);
CREATE INDEX idx_rules_active ON rules(is_active);
CREATE INDEX idx_billing_user_date ON billing_records(user_id, start_date, end_date);

-- Seed admin user (password: admin123)
INSERT INTO users (username, password, email, full_name, role)
VALUES ('admin', 'admin123', 'admin@local', 'System Admin', 'admin');
