-- Complete fix for billing_records duplicates
-- This will recreate the table with a unique constraint

USE aws_governance_db;

-- Step 1: Backup existing data (optional but recommended)
-- CREATE TABLE billing_records_backup AS SELECT * FROM billing_records;

-- Step 2: Drop the existing table
DROP TABLE IF EXISTS billing_records;

-- Step 3: Recreate the table with unique constraint
CREATE TABLE billing_records (
    record_id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    service_name VARCHAR(100) NOT NULL,
    cost_amount DECIMAL(10, 4) DEFAULT 0.0,  -- Changed from DECIMAL(10,2) to DECIMAL(10,4) for precision
    currency VARCHAR(10) DEFAULT 'USD',
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    record_type VARCHAR(50) DEFAULT 'monthly',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    -- Add unique constraint to prevent duplicates
    UNIQUE KEY unique_billing_record (user_id, service_name, start_date, end_date)
);

-- Step 4: If you had backup data and want to restore unique records only:
-- INSERT INTO billing_records (user_id, service_name, cost_amount, currency, start_date, end_date, record_type, created_at)
-- SELECT user_id, service_name, cost_amount, currency, start_date, end_date, record_type, created_at
-- FROM billing_records_backup
-- WHERE record_id IN (
--     SELECT MAX(record_id) 
--     FROM billing_records_backup 
--     GROUP BY user_id, service_name, start_date, end_date
-- );

-- Verify the constraint was added
SHOW CREATE TABLE billing_records;
