# Script to fix the notification service database schema issue
# This script updates the notifications table to match the JPA entity

Write-Host "=== Fixing Notification Service Database Schema ===" -ForegroundColor Green

# Database connection parameters
$DB_HOST = "localhost"
$DB_PORT = "3306"
$DB_NAME = "eduschedule"
$DB_USER = "root"
$DB_PASSWORD = "root123"

Write-Host "Updating notifications table schema..." -ForegroundColor Cyan

# Execute the update script using mysql command
try {
    $mysqlArgs = @("-h$DB_HOST", "-P$DB_PORT", "-u$DB_USER", "-p$DB_PASSWORD", $DB_NAME, "-e", "source update-notifications-table.sql")
    $result = Start-Process -FilePath "mysql" -ArgumentList $mysqlArgs -Wait -PassThru -NoNewWindow
    
    if ($result.ExitCode -eq 0) {
        Write-Host "Database schema updated successfully!" -ForegroundColor Green
        Write-Host "The notifications table now includes all required columns:" -ForegroundColor Green
        Write-Host "  - scheduled_for" -ForegroundColor Gray
        Write-Host "  - event_type" -ForegroundColor Gray
        Write-Host "  - event_id" -ForegroundColor Gray
        Write-Host "  - priority" -ForegroundColor Gray
        Write-Host "  - retry_count" -ForegroundColor Gray
        Write-Host "  - max_retries" -ForegroundColor Gray
        Write-Host "  - template_name" -ForegroundColor Gray
        Write-Host "  - metadata" -ForegroundColor Gray
    } else {
        Write-Host "Failed to update database schema" -ForegroundColor Red
        exit 1
    }
} catch {
    Write-Host "Error executing database update: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host "Make sure MySQL is running and credentials are correct." -ForegroundColor Yellow
    exit 1
}

Write-Host ""
Write-Host "=== Next Steps ===" -ForegroundColor Yellow
Write-Host "1. The database schema has been updated" -ForegroundColor White
Write-Host "2. You can now restart the notification service" -ForegroundColor White
Write-Host "3. Run: cd notification-service; mvn spring-boot:run" -ForegroundColor Cyan

Write-Host ""
Write-Host "Database fix completed!" -ForegroundColor Green