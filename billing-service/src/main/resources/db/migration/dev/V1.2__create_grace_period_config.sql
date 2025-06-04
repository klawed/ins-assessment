CREATE TABLE grace_period_configs (
    id VARCHAR(36) PRIMARY KEY,
    policy_type VARCHAR(50) NOT NULL,
    payment_frequency VARCHAR(20) NOT NULL,
    grace_period_days INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_policy_frequency (policy_type, payment_frequency)
);

-- Default configurations
INSERT INTO grace_period_configs (id, policy_type, payment_frequency, grace_period_days) VALUES
('default-monthly', 'DEFAULT', 'MONTHLY', 10),
('auto-monthly', 'AUTO', 'MONTHLY', 15),
('home-monthly', 'HOME', 'MONTHLY', 10);