-- Cleanup Test/Mock Data from Database
-- Run this to remove all test data and start fresh with real AWS data

USE aws_governance_db;

-- Clear all EC2 test instances
DELETE FROM ec2_instances;

-- Clear all RDS test instances
DELETE FROM rds_instances;

-- Clear all ECS test services
DELETE FROM ecs_services;

-- Clear all SageMaker test endpoints
DELETE FROM sagemaker_endpoints;

-- Clear all test billing records
DELETE FROM billing_records;

-- Clear all test alerts
DELETE FROM alerts;

-- Clear all test rules
DELETE FROM rules;

-- Reset auto-increment counters
ALTER TABLE ec2_instances AUTO_INCREMENT = 1;
ALTER TABLE rds_instances AUTO_INCREMENT = 1;
ALTER TABLE ecs_services AUTO_INCREMENT = 1;
ALTER TABLE sagemaker_endpoints AUTO_INCREMENT = 1;
ALTER TABLE billing_records AUTO_INCREMENT = 1;
ALTER TABLE alerts AUTO_INCREMENT = 1;
ALTER TABLE rules AUTO_INCREMENT = 1;

SELECT 'Database cleaned successfully! Now sync from AWS to populate with real data.' AS Status;
