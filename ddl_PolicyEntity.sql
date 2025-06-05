
CREATE TABLE policies
(
    id                VARCHAR(255) NOT NULL,
    policy_number     VARCHAR(255),
    customer_id       VARCHAR(255),
    policy_type       VARCHAR(255),
    premium_amount    DECIMAL,
    next_due_date     date,
    grace_period_days INT,
    status            VARCHAR(255),
    payment_frequency VARCHAR(255),
    CONSTRAINT pk_policies PRIMARY KEY (id)
);
CREATE TABLE policies
(
    id                VARCHAR(255) NOT NULL,
    policy_number     VARCHAR(255) NOT NULL,
    customer_id       VARCHAR(255) NOT NULL,
    policy_type       VARCHAR(255) NOT NULL,
    status            VARCHAR(255) NOT NULL,
    effective_date    TIMESTAMP    NOT NULL,
    expiration_date   date         NOT NULL,
    premium_amount    DECIMAL      NOT NULL,
    frequency         VARCHAR(255) NOT NULL,
    grace_period_days INT,
    next_due_date     date,
    created_at        TIMESTAMP,
    updated_at        TIMESTAMP,
    CONSTRAINT pk_policies PRIMARY KEY (id)
);