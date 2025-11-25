-- Add remaining_credits column to aws_credentials table
USE aws_governance_db;

ALTER TABLE aws_credentials 
ADD COLUMN remaining_credits DOUBLE DEFAULT 0.0 AFTER region;

-- Show the updated table structure
DESCRIBE aws_credentials;
