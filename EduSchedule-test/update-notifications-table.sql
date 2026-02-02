-- Update notifications table to match the current entity structure
-- Add missing columns to the notifications table

USE iusjcdb;

ALTER TABLE notifications 
ADD COLUMN IF NOT EXISTS scheduled_for TIMESTAMP NULL,
ADD COLUMN IF NOT EXISTS event_type VARCHAR(50) NULL,
ADD COLUMN IF NOT EXISTS event_id BIGINT NULL,
ADD COLUMN IF NOT EXISTS priority VARCHAR(10) DEFAULT 'NORMAL',
ADD COLUMN IF NOT EXISTS retry_count INT DEFAULT 0,
ADD COLUMN IF NOT EXISTS max_retries INT DEFAULT 3,
ADD COLUMN IF NOT EXISTS template_name VARCHAR(100) NULL,
ADD COLUMN IF NOT EXISTS metadata TEXT NULL;

-- Add indexes for the new columns
CREATE INDEX IF NOT EXISTS idx_notifications_event_id ON notifications(event_id);
CREATE INDEX IF NOT EXISTS idx_notifications_event_type ON notifications(event_type);
CREATE INDEX IF NOT EXISTS idx_notifications_scheduled_for ON notifications(scheduled_for);
CREATE INDEX IF NOT EXISTS idx_notifications_priority ON notifications(priority);

-- Show the updated table structure
DESCRIBE notifications;