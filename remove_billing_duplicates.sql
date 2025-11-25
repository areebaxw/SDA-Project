-- Recreate billing_records table with unique constraint
USE aws_governance_db;

-- Drop the existing table
DROP TABLE IF EXISTS billing_records;

-- Create new table with unique constraint
CREATE TABLE billing_records (
    record_id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    service_name VARCHAR(100) NOT NULL,
    cost_amount DECIMAL(10, 4) DEFAULT 0.0,
    currency VARCHAR(10) DEFAULT 'USD',
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    record_type VARCHAR(50) DEFAULT 'monthly',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    UNIQUE KEY unique_billing_record (user_id, service_name, start_date, end_date)
);

SELECT 'Table billing_records created successfully' as status;
