-- V1__Initial_schema.sql (FOR MARIADB DATABASE)

-- 1. Billings Table
CREATE TABLE billings (
                          id VARCHAR(255) PRIMARY KEY,
                          policy_id VARCHAR(255) NOT NULL,
                          customer_id VARCHAR(255) NOT NULL,
                          premium_amount DECIMAL(10, 2) NOT NULL,
                          due_date DATE NOT NULL,
                          billing_period_start DATE NOT NULL,
                          billing_period_end DATE NOT NULL,
                          status VARCHAR(50) NOT NULL COMMENT 'Enum: PENDING, PAID, OVERDUE, GRACE_PERIOD, DELINQUENT, CANCELLED',
                          frequency VARCHAR(50) NOT NULL COMMENT 'Enum: MONTHLY, QUARTERLY, SEMI_ANNUAL, ANNUAL',
                          grace_period_end DATE,
                          retry_count INT,
                          next_retry_date DATE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Indexes for billings (as per plan and repository methods)
CREATE INDEX idx_billings_policy_id ON billings(policy_id);
CREATE INDEX idx_billings_customer_id ON billings(customer_id);
CREATE INDEX idx_billings_status ON billings(status);
CREATE INDEX idx_billings_due_date ON billings(due_date);
-- For findByStatusAndDueDateBefore
CREATE INDEX idx_billings_status_due_date ON billings(status, due_date);
-- For findDelinquentBillings (status = 'OVERDUE' AND gracePeriodEnd < :date)
CREATE INDEX idx_billings_status_grace_period_end ON billings(status, grace_period_end);


-- 2. Payments Table
CREATE TABLE payments (
                          id VARCHAR(255) PRIMARY KEY,
                          billing_id VARCHAR(255) NOT NULL,
                          policy_id VARCHAR(255) NOT NULL,
                          customer_id VARCHAR(255) NOT NULL,
                          amount DECIMAL(10, 2) NOT NULL,
                          status VARCHAR(50) NOT NULL COMMENT 'Enum: PENDING, SUCCESS, FAILED, CANCELLED, REFUNDED',
                          method VARCHAR(50) NOT NULL COMMENT 'Enum: CREDIT_CARD, DEBIT_CARD, BANK_TRANSFER, ACH, PAYPAL, STRIPE',
                          attempted_at DATETIME(6) NOT NULL, -- DATETIME(6) for microsecond precision if needed, else DATETIME
                          processed_at DATETIME(6),
                          transaction_id VARCHAR(255),
                          gateway_response TEXT,
                          failure_reason TEXT,
                          CONSTRAINT fk_payments_billing FOREIGN KEY (billing_id) REFERENCES billings(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Indexes for payments (as per plan and repository methods)
CREATE INDEX idx_payments_billing_id ON payments(billing_id);
CREATE INDEX idx_payments_policy_id ON payments(policy_id);
CREATE INDEX idx_payments_customer_id ON payments(customer_id);
CREATE INDEX idx_payments_transaction_id ON payments(transaction_id);
CREATE INDEX idx_payments_status ON payments(status);
-- For findPaymentHistory (customerId, attemptedAt)
CREATE INDEX idx_payments_customer_attempted_at ON payments(customer_id, attempted_at);
-- For findFailedPaymentsForBillings (status = 'FAILED' AND billingId IN :billingIds)
-- (idx_payments_status and idx_payments_billing_id will be used)


-- 3. PaymentRetries Table
CREATE TABLE payment_retries (
                                 id VARCHAR(255) PRIMARY KEY,
                                 payment_id VARCHAR(255) NOT NULL,
                                 billing_id VARCHAR(255) NOT NULL,
                                 retry_attempt INT NOT NULL,
                                 scheduled_at DATETIME(6) NOT NULL,
                                 attempted_at DATETIME(6),
                                 status VARCHAR(50) NOT NULL COMMENT 'Enum: SCHEDULED, IN_PROGRESS, SUCCESS, FAILED, SKIPPED, EXHAUSTED',
                                 failure_reason TEXT,
                                 CONSTRAINT fk_payment_retries_payment FOREIGN KEY (payment_id) REFERENCES payments(id),
                                 CONSTRAINT fk_payment_retries_billing FOREIGN KEY (billing_id) REFERENCES billings(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Indexes for payment_retries (as per plan and repository methods)
CREATE INDEX idx_payment_retries_payment_id ON payment_retries(payment_id);
CREATE INDEX idx_payment_retries_billing_id ON payment_retries(billing_id);
-- For findByStatus and findDueRetries
CREATE INDEX idx_payment_retries_status_scheduled_at ON payment_retries(status, scheduled_at);
CREATE INDEX idx_payment_retries_status ON payment_retries(status); -- Also useful for findByStatus alone
CREATE INDEX idx_payment_retries_scheduled_at ON payment_retries(scheduled_at); -- Also useful


-- 4. BillingEvents Table
CREATE TABLE billing_events (
                                id VARCHAR(255) PRIMARY KEY,
                                billing_id VARCHAR(255) NOT NULL,
                                policy_id VARCHAR(255) NOT NULL,
                                event_type VARCHAR(50) NOT NULL COMMENT 'Enum: BILLING_CREATED, PAYMENT_DUE, PAYMENT_SUCCESS, PAYMENT_FAILED, RETRY_SCHEDULED, GRACE_PERIOD_STARTED, DELINQUENT, REMINDER_SENT',
                                occurred_at DATETIME(6) NOT NULL,
                                payload TEXT,
                                metadata TEXT,
                                CONSTRAINT fk_billing_events_billing FOREIGN KEY (billing_id) REFERENCES billings(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Indexes for billing_events (as per plan and repository methods)
CREATE INDEX idx_billing_events_billing_id ON billing_events(billing_id);
CREATE INDEX idx_billing_events_policy_id ON billing_events(policy_id);
CREATE INDEX idx_billing_events_event_type ON billing_events(event_type);
CREATE INDEX idx_billing_events_occurred_at ON billing_events(occurred_at);
-- For findEventHistoryForBilling (billingId, occurredAt DESC)
CREATE INDEX idx_billing_events_billing_id_occurred_at ON billing_events(billing_id, occurred_at);