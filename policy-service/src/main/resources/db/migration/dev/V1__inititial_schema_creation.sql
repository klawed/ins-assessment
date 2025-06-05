CREATE TABLE policies
(
    id                VARCHAR(255) NOT NULL,
    policy_number     VARCHAR(255),
    customer_id       VARCHAR(255),
    policy_type       VARCHAR(255),
    premium_amount    DECIMAL,
    next_due_date     date,
    grace_period_days INT,
    status            enum ('pending','paid','overdue','grace_period','delinquent','active'),
    payment_frequency enum ('monthly','quarterly','semi_annual','annual'),
    effective_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_policies PRIMARY KEY (id)
);