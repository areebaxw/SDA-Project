-- AWS Cloud Governance & Resource Monitoring Tool - Database Schema
-- Drop existing databases and create fresh
DROP DATABASE IF EXISTS aws_governance_db;
DROP DATABASE IF EXISTS aws_governance;
CREATE DATABASE aws_governance_db;
USE aws_governance_db;

-- Users table
CREATE TABLE users (
    user_id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100),
    full_name VARCHAR(100),
    role VARCHAR(20) DEFAULT 'user',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_login TIMESTAMP NULL
);

-- AWS Credentials table
CREATE TABLE aws_credentials (
    credential_id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    access_key VARCHAR(255) UNIQUE NOT NULL,
    secret_key VARCHAR(255) NOT NULL,
    region VARCHAR(50) NOT NULL,
    remaining_credits DOUBLE DEFAULT 0.0,
    is_active BOOLEAN DEFAULT TRUE,
    validated BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- Governance Rules table
CREATE TABLE rules (
    rule_id INT PRIMARY KEY AUTO_INCREMENT,
    rule_name VARCHAR(100) NOT NULL,
    rule_type VARCHAR(50) NOT NULL,
    resource_type VARCHAR(50) NOT NULL,
    condition_metric VARCHAR(50),
    condition_operator VARCHAR(20),
    condition_value DOUBLE,
    condition_duration INT,
    duration_unit VARCHAR(20) DEFAULT 'hours',
    action_type VARCHAR(50),
    is_active BOOLEAN DEFAULT TRUE,
    created_by INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (created_by) REFERENCES users(user_id)
);

-- Alerts table
CREATE TABLE alerts (
    alert_id INT PRIMARY KEY AUTO_INCREMENT,
    resource_id VARCHAR(100) NOT NULL,
    resource_type VARCHAR(50) NOT NULL,
    alert_type VARCHAR(50) NOT NULL,
    severity VARCHAR(20) DEFAULT 'medium',
    message TEXT NOT NULL,
    rule_id INT,
    is_resolved BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    resolved_at TIMESTAMP NULL,
    FOREIGN KEY (rule_id) REFERENCES rules(rule_id) ON DELETE SET NULL
);

-- EC2 Instances table
CREATE TABLE ec2_instances (
    record_id INT PRIMARY KEY AUTO_INCREMENT,
    instance_id VARCHAR(50) NOT NULL,
    instance_type VARCHAR(50),
    instance_state VARCHAR(20),
    availability_zone VARCHAR(50),
    launch_time TIMESTAMP NULL,
    cpu_utilization DOUBLE,
    network_in DOUBLE,
    network_out DOUBLE,
    is_idle BOOLEAN DEFAULT FALSE,
    last_checked TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    user_id INT,
    FOREIGN KEY (user_id) REFERENCES users(user_id)
);

-- RDS Instances table
CREATE TABLE rds_instances (
    record_id INT PRIMARY KEY AUTO_INCREMENT,
    db_instance_identifier VARCHAR(100) NOT NULL,
    db_instance_class VARCHAR(50),
    engine VARCHAR(50),
    engine_version VARCHAR(50),
    db_instance_status VARCHAR(50),
    allocated_storage INT,
    availability_zone VARCHAR(50),
    cpu_utilization DOUBLE,
    database_connections INT,
    is_idle BOOLEAN DEFAULT FALSE,
    last_checked TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    user_id INT,
    FOREIGN KEY (user_id) REFERENCES users(user_id)
);

-- ECS Services table
CREATE TABLE ecs_services (
    record_id INT PRIMARY KEY AUTO_INCREMENT,
    cluster_name VARCHAR(100) NOT NULL,
    service_name VARCHAR(100) NOT NULL,
    service_arn TEXT,
    status VARCHAR(50),
    desired_count INT,
    running_count INT,
    pending_count INT,
    task_definition VARCHAR(255),
    cpu_utilization DOUBLE,
    memory_utilization DOUBLE,
    is_idle BOOLEAN DEFAULT FALSE,
    last_checked TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    user_id INT,
    FOREIGN KEY (user_id) REFERENCES users(user_id)
);

-- SageMaker Endpoints table
CREATE TABLE sagemaker_endpoints (
    record_id INT PRIMARY KEY AUTO_INCREMENT,
    endpoint_name VARCHAR(100) NOT NULL,
    endpoint_arn TEXT,
    endpoint_status VARCHAR(50),
    model_name VARCHAR(100),
    instance_type VARCHAR(50),
    instance_count INT,
    invocations INT DEFAULT 0,
    model_latency DOUBLE,
    is_idle BOOLEAN DEFAULT FALSE,
    creation_time TIMESTAMP NULL,
    last_checked TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    resource_type VARCHAR(20) DEFAULT 'endpoint',
    user_id INT,
    FOREIGN KEY (user_id) REFERENCES users(user_id)
);

-- Billing Records table
CREATE TABLE billing_records (
    record_id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    service_name VARCHAR(100),
    cost_amount DECIMAL(10, 2),
    currency VARCHAR(10) DEFAULT 'USD',
    start_date DATE,
    end_date DATE,
    record_type VARCHAR(50) DEFAULT 'monthly',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id)
);

-- Insert sample users
INSERT INTO users (username, password, email, full_name, role) VALUES
('admin', 'admin123', 'admin@awsgovernance.com', 'System Administrator', 'admin'),
('john.doe', 'password123', 'john.doe@company.com', 'John Doe', 'user'),
('jane.smith', 'password123', 'jane.smith@company.com', 'Jane Smith', 'user');

-- Insert sample AWS credentials (placeholder values)
INSERT INTO aws_credentials (user_id, access_key, secret_key, region, validated) VALUES
(1, 'AKIAIOSFODNN7EXAMPLE', 'wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY', 'us-east-1', FALSE),
(2, 'AKIAIOSFODNN7EXAMPLE2', 'wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY2', 'us-west-2', FALSE);

-- Insert sample governance rules
INSERT INTO rules (rule_name, rule_type, resource_type, condition_metric, condition_operator, condition_value, condition_duration, action_type, is_active, created_by) VALUES
('Idle EC2 Detection', 'resource_optimization', 'EC2', 'CPU', '<', 5.0, 7, 'ALERT', TRUE, 1),
('High CPU Alert', 'performance', 'EC2', 'CPU', '>', 90.0, 1, 'ALERT', TRUE, 1),
('Unused RDS Detection', 'cost_optimization', 'RDS', 'Connections', '<', 2, 7, 'ALERT', TRUE, 1),
('Idle SageMaker Endpoint', 'cost_optimization', 'SageMaker', 'Invocations', '<', 10, 7, 'STOP', TRUE, 1),
('ECS Service Underutilization', 'resource_optimization', 'ECS', 'CPU', '<', 10.0, 5, 'ALERT', TRUE, 1);

-- Insert sample EC2 instances
INSERT INTO ec2_instances (instance_id, instance_type, instance_state, availability_zone, launch_time, cpu_utilization, network_in, network_out, is_idle, user_id) VALUES
('i-0123456789abcdef0', 't2.micro', 'running', 'us-east-1a', '2025-11-01 10:00:00', 2.5, 1024.0, 512.0, TRUE, 1),
('i-0123456789abcdef1', 't3.medium', 'running', 'us-east-1b', '2025-11-10 14:30:00', 45.8, 5120.0, 2048.0, FALSE, 1),
('i-0123456789abcdef2', 'm5.large', 'stopped', 'us-east-1a', '2025-10-15 08:00:00', 0.0, 0.0, 0.0, TRUE, 2);

-- Insert sample RDS instances
INSERT INTO rds_instances (db_instance_identifier, db_instance_class, engine, engine_version, db_instance_status, allocated_storage, availability_zone, cpu_utilization, database_connections, is_idle, user_id) VALUES
('mydb-instance-1', 'db.t3.micro', 'mysql', '8.0.35', 'available', 20, 'us-east-1a', 15.5, 5, FALSE, 1),
('mydb-instance-2', 'db.t3.small', 'postgres', '15.3', 'available', 50, 'us-east-1b', 1.2, 0, TRUE, 1);

-- Insert sample ECS services
INSERT INTO ecs_services (cluster_name, service_name, service_arn, status, desired_count, running_count, pending_count, task_definition, cpu_utilization, memory_utilization, is_idle, user_id) VALUES
('production-cluster', 'web-service', 'arn:aws:ecs:us-east-1:123456789012:service/production-cluster/web-service', 'ACTIVE', 3, 3, 0, 'web-task:1', 35.2, 45.8, FALSE, 1),
('staging-cluster', 'api-service', 'arn:aws:ecs:us-east-1:123456789012:service/staging-cluster/api-service', 'ACTIVE', 1, 1, 0, 'api-task:2', 5.1, 12.3, TRUE, 1);

-- Insert sample SageMaker endpoints
INSERT INTO sagemaker_endpoints (endpoint_name, endpoint_arn, endpoint_status, model_name, instance_type, instance_count, invocations, model_latency, is_idle, creation_time, user_id) VALUES
('fraud-detection-endpoint', 'arn:aws:sagemaker:us-east-1:123456789012:endpoint/fraud-detection-endpoint', 'InService', 'fraud-model-v1', 'ml.m5.large', 1, 1523, 45.2, FALSE, '2025-10-20 09:00:00', 1),
('test-ml-endpoint', 'arn:aws:sagemaker:us-east-1:123456789012:endpoint/test-ml-endpoint', 'InService', 'test-model', 'ml.t3.medium', 1, 3, 120.5, TRUE, '2025-09-15 11:30:00', 1);

-- Insert sample alerts
INSERT INTO alerts (resource_id, resource_type, alert_type, severity, message, rule_id, is_resolved) VALUES
('i-0123456789abcdef0', 'EC2', 'IDLE_RESOURCE', 'medium', 'EC2 instance has CPU utilization below 5% for 7 days', 1, FALSE),
('mydb-instance-2', 'RDS', 'IDLE_RESOURCE', 'high', 'RDS instance has no database connections for 7 days', 3, FALSE),
('test-ml-endpoint', 'SageMaker', 'IDLE_RESOURCE', 'high', 'SageMaker endpoint has minimal invocations', 4, FALSE),
('i-0123456789abcdef1', 'EC2', 'HIGH_CPU', 'high', 'EC2 instance CPU utilization exceeded 90%', 2, TRUE);

-- Insert sample billing records
INSERT INTO billing_records (user_id, service_name, cost_amount, start_date, end_date, record_type) VALUES
(1, 'EC2', 145.50, '2025-10-01', '2025-10-31', 'monthly'),
(1, 'RDS', 89.20, '2025-10-01', '2025-10-31', 'monthly'),
(1, 'ECS', 65.00, '2025-10-01', '2025-10-31', 'monthly'),
(1, 'SageMaker', 230.75, '2025-10-01', '2025-10-31', 'monthly'),
(1, 'CloudWatch', 15.30, '2025-10-01', '2025-10-31', 'monthly'),
(1, 'EC2', 152.80, '2025-11-01', '2025-11-20', 'monthly'),
(1, 'RDS', 92.10, '2025-11-01', '2025-11-20', 'monthly');

-- Create indexes for performance
CREATE INDEX idx_ec2_instance_id ON ec2_instances(instance_id);
CREATE INDEX idx_rds_identifier ON rds_instances(db_instance_identifier);
CREATE INDEX idx_alerts_resource ON alerts(resource_id, resource_type);
CREATE INDEX idx_alerts_resolved ON alerts(is_resolved);
CREATE INDEX idx_rules_active ON rules(is_active);
CREATE INDEX idx_billing_dates ON billing_records(start_date, end_date);

-- Display summary
SELECT 'Database schema created successfully!' AS Status;
SELECT COUNT(*) AS total_users FROM users;
SELECT COUNT(*) AS total_rules FROM rules;
SELECT COUNT(*) AS total_alerts FROM alerts;
SELECT COUNT(*) AS total_ec2_instances FROM ec2_instances;
