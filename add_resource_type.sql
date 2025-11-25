-- Add resource_type column to sagemaker_endpoints table
ALTER TABLE sagemaker_endpoints 
ADD COLUMN resource_type VARCHAR(20) DEFAULT 'endpoint' AFTER last_checked;

-- Update existing records to be 'endpoint' type
UPDATE sagemaker_endpoints SET resource_type = 'endpoint' WHERE resource_type IS NULL;
