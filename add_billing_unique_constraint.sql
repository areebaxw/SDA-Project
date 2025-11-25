-- Add unique constraint to billing_records table to prevent duplicates
USE aws_governance_db;

-- Add unique constraint on user_id, service_name, start_date, end_date
ALTER TABLE billing_records 
ADD CONSTRAINT unique_billing_record 
UNIQUE (user_id, service_name, start_date, end_date);

-- Optional: Remove any existing duplicates before adding the constraint
-- First, create a temporary table with only unique records (keeping the latest one)
-- CREATE TEMPORARY TABLE billing_records_unique AS
-- SELECT record_id, user_id, service_name, cost_amount, currency, start_date, end_date, record_type, created_at
-- FROM billing_records
-- WHERE record_id IN (
--     SELECT MAX(record_id) 
--     FROM billing_records 
--     GROUP BY user_id, service_name, start_date, end_date
-- );

-- Delete all records from billing_records
-- DELETE FROM billing_records;

-- Insert unique records back
-- INSERT INTO billing_records SELECT * FROM billing_records_unique;

-- Drop temporary table
-- DROP TEMPORARY TABLE billing_records_unique;
