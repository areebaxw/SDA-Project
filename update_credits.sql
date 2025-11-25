-- Update remaining credits for a user's AWS credentials
-- Replace USER_ID with the actual user ID and 99.39 with the actual remaining credits amount

USE aws_governance_db;

-- Example: Update credits for user_id = 1
UPDATE aws_credentials 
SET remaining_credits = 99.39 
WHERE user_id = 1 AND is_active = TRUE;

-- Verify the update
SELECT user_id, region, remaining_credits, is_active 
FROM aws_credentials 
WHERE user_id = 1;
