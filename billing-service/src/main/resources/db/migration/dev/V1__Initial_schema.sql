CREATE TABLE billing_events
(
    id          VARCHAR(255) NOT NULL,
    billing_id  VARCHAR(255) NOT NULL,
    policy_id   VARCHAR(255) NOT NULL,
    event_type  enum ('billing_created','payment_due','payment_success','payment_failed','retry_scheduled','grace_period_started','delinquent','reminder_sent') NOT NULL,
    occurred_at TIMESTAMP    NOT NULL,
    payload     VARCHAR,
    metadata    VARCHAR,
    CONSTRAINT pk_billing_events PRIMARY KEY (id)
);

CREATE TABLE billings
(
    id             VARCHAR(255) NOT NULL,
    policy_id      VARCHAR(255),
    customer_id    VARCHAR(255),
    amount         DECIMAL,
    due_date       date,
    status         enum ('pending','paid','overdue','grace_period','delinquent'),
    billing_date   TIMESTAMP,
    payment_status VARCHAR(255),
    created_at     TIMESTAMP,
    updated_at     TIMESTAMP,
    CONSTRAINT pk_billings PRIMARY KEY (id)
);

CREATE TABLE grace_period_configs
(
    id                VARCHAR(255) NOT NULL,
    policy_type       VARCHAR(255),
    payment_frequency enum ('monthly','quarterly','semi_annual','annual'),
    grace_period_days INT,
    created_at        TIMESTAMP,
    updated_at        TIMESTAMP,
    CONSTRAINT pk_grace_period_configs PRIMARY KEY (id)
);

CREATE TABLE payment_retries
(
    id             VARCHAR(255) NOT NULL,
    payment_id     VARCHAR(255) NOT NULL,
    billing_id     VARCHAR(255) NOT NULL,
    retry_attempt  INT          NOT NULL,
    scheduled_at   TIMESTAMP    NOT NULL,
    attempted_at   TIMESTAMP,
    status         enum ('scheduled','in_progress','success','failed','skipped','exhausted') NOT NULL,
    failure_reason VARCHAR(255),
    created_at     TIMESTAMP,
    updated_at     TIMESTAMP,
    CONSTRAINT pk_payment_retries PRIMARY KEY (id)
);

CREATE TABLE payments
(
    id               VARCHAR(255)   NOT NULL,
    billing_id       VARCHAR(255)   NOT NULL,
    policy_id        VARCHAR(255)   NOT NULL,
    customer_id      VARCHAR(255)   NOT NULL,
    amount           DECIMAL(10, 2) NOT NULL,
    status           enum ('scheduled','in_progress','success','failed','skipped','exhausted')   NOT NULL,
    method           enum ('credit_card','debit_card','bank_transfer','ach','paypal','stripe')   NOT NULL,
    attempted_at     TIMESTAMP      NOT NULL,
    processed_at     TIMESTAMP,
    transaction_id   VARCHAR(255),
    gateway_response VARCHAR(255),
    failure_reason   VARCHAR(255),
    created_at       TIMESTAMP,
    updated_at       TIMESTAMP,
    CONSTRAINT pk_payments PRIMARY KEY (id)
);

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