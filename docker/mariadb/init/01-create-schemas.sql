-- 01-create-schemas.sql
-- For Option A: Relying on MariaDB entrypoint for main DB and User creation.

-- The main database (e.g., 'billing_system_e2e') and the primary application user
-- (e.g., 'app_user_e2e') are expected to be created automatically by the MariaDB
-- Docker image's entrypoint script. This happens when MYSQL_DATABASE, MYSQL_USER,
-- and MYSQL_PASSWORD environment variables are provided to the MariaDB container.

-- Flyway migrations, executed by each microservice upon their startup,
-- will be responsible for creating all necessary tables within that database.

-- This script is now only for settings or actions not handled by the entrypoint
-- or individual service Flyway migrations.

-- Example: Enable the event scheduler if your application logic requires it.
-- This is a global setting and needs to be done once.
SET GLOBAL event_scheduler = ON;

-- You could add other global MySQL/MariaDB settings here if needed, for example:
-- SET GLOBAL max_connections = 500;
-- (Use with caution and understanding of the implications)

-- If you have any truly global, minimal seed data that is not tied to a specific
-- service's schema and must exist before any service starts, you *could* put it here.
-- However, it's generally cleaner to manage seed data via Flyway repeatable migrations (R__*.sql)
-- or versioned migrations (V__*.sql) within the relevant service(s) if the data pertains
-- to their tables.
-- Example (if you had a global 'settings' table, which you don't seem to):
-- USE ${MYSQL_DATABASE}; -- The env var MYSQL_DATABASE will be substituted by the shell if used in an .sh script,
                          -- but in a .sql script, you'd hardcode or know the name.
                          -- However, the entrypoint typically executes these scripts *after* creating MYSQL_DATABASE
                          -- and implicitly making it the default, or by connecting as root to it.
-- INSERT INTO global_settings (setting_key, setting_value) VALUES ('system_version', '1.0.0');

-- For most common scenarios with Option A, just setting event_scheduler (if needed)
-- might be all that's required in this init script.