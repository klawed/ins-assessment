-- Initialize billing system database
-- This script runs when MariaDB container starts for the first time

USE billing_system;

-- Create schemas for each microservice
-- Tables will be created by Flyway migrations in each service

-- Policy Service tables will include:
-- - policies
-- - policy_schedules

-- Billing Service tables will include:
-- - billing_cycles 
-- - premium_calculations
-- - delinquency_tracking

-- Payment Service tables will include:
-- - payments
-- - payment_attempts
-- - retry_schedules

-- Notification Service tables will include:
-- - notifications
-- - notification_templates

-- Create a test user for development/testing
-- Note: In production, each service should have its own database user with limited permissions
INSERT IGNORE INTO mysql.user (Host, User, Password, ssl_cipher, x509_issuer, x509_subject)
VALUES ('localhost', 'test_user', PASSWORD('test_password'), '', '', '');

-- Grant basic permissions for testing
GRANT SELECT, INSERT, UPDATE, DELETE ON billing_system.* TO 'test_user'@'localhost';

-- Ensure privileges are applied
FLUSH PRIVILEGES;

-- Enable event scheduler for background tasks (if needed)
SET GLOBAL event_scheduler = ON;