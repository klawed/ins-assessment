CREATE TABLE policies
(
    id                VARCHAR(255) NOT NULL,
    policy_number     VARCHAR(255) NOT NULL,
    customer_id       VARCHAR(255) NOT NULL,
    policy_type       VARCHAR(255) NOT NULL,
    effective_date    TIMESTAMP    NOT NULL,
    expiration_date   TIMESTAMP         NOT NULL,
    premium_amount    DECIMAL      NOT NULL,
    status            enum ('pending','paid','overdue','grace_period','delinquent','active'),
    payment_frequency enum ('monthly','quarterly','semi_annual','annual'),
    grace_period_days INT,
    next_due_date     date,
    created_at        TIMESTAMP,
    updated_at        TIMESTAMP,
    CONSTRAINT pk_policies PRIMARY KEY (id)
);