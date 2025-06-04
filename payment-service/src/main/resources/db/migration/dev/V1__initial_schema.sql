CREATE TABLE payment_entity
(
    id             VARCHAR(255) NOT NULL,
    policy_id      VARCHAR(255),
    amount         DECIMAL,
    status         enum ('monthly','quarterly','semi_annual','annual'),
    timestamp      TIMESTAMP,
    payment_method enum ('credit_card','debit_card','bank_transfer','ach','paypal','stripe'),
    CONSTRAINT pk_paymententity PRIMARY KEY (id)
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
    status            enum ('pending','success','failed','cancelled','refunded','completed','processing'),
    payment_frequency enum ('monthly','quarterly','semi_annual','annual'),
    CONSTRAINT pk_policies PRIMARY KEY (id)
);