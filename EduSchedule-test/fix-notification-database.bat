@echo off
echo ========================================
echo Fixing Notification Service Database Schema
echo ========================================

echo Updating notifications table to add missing columns...
echo.

echo Please run the following commands in MySQL:
echo.
echo mysql -h localhost -P 3306 -u iusjc -p
echo USE iusjcdb;
echo.
echo ALTER TABLE notifications 
echo ADD COLUMN IF NOT EXISTS scheduled_for TIMESTAMP NULL,
echo ADD COLUMN IF NOT EXISTS event_type VARCHAR(50) NULL,
echo ADD COLUMN IF NOT EXISTS event_id BIGINT NULL,
echo ADD COLUMN IF NOT EXISTS priority VARCHAR(10) DEFAULT 'NORMAL',
echo ADD COLUMN IF NOT EXISTS retry_count INT DEFAULT 0,
echo ADD COLUMN IF NOT EXISTS max_retries INT DEFAULT 3,
echo ADD COLUMN IF NOT EXISTS template_name VARCHAR(100) NULL,
echo ADD COLUMN IF NOT EXISTS metadata TEXT NULL;
echo.
echo CREATE INDEX IF NOT EXISTS idx_notifications_event_id ON notifications(event_id);
echo CREATE INDEX IF NOT EXISTS idx_notifications_event_type ON notifications(event_type);
echo CREATE INDEX IF NOT EXISTS idx_notifications_scheduled_for ON notifications(scheduled_for);
echo CREATE INDEX IF NOT EXISTS idx_notifications_priority ON notifications(priority);
echo.
echo EXIT;
echo.
echo ========================================
echo Or use the SQL file: update-notifications-table.sql
echo ========================================
pause