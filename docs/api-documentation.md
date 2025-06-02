# Policy Billing System - REST API Endpoints

This document outlines the REST API endpoints for the insurance policy billing system simulation.

## API Base URL
```
http://localhost:8080/api/v1
```

## Authentication
All endpoints require API key authentication via header:
```
Authorization: Bearer {api_key}
```

## Core Endpoints

### 1. Retrieve Premium Schedule for a Policy

**GET** `/billing/policies/{policyId}/premium-schedule`

Retrieves the premium schedule and billing details for a specific policy.

**Parameters:**
- `policyId` (path) - The policy identifier

**Response:**
```json
{
  "policyId": "12345",
  "policyType": "auto_insurance",
  "premiumAmount": 156.00,
  "billingFrequency": "monthly",
  "nextDueDate": "2024-12-15T00:00:00Z",
  "gracePeriodDays": 10,
  "lateFee": 15.00,
  "status": "overdue",
  "daysOverdue": 3,
  "totalAmountDue": 171.00,
  "schedule": [
    {
      "dueDate": "2024-12-15T00:00:00Z",
      "amount": 156.00,
      "status": "overdue",
      "lateFee": 15.00
    },
    {
      "dueDate": "2025-01-15T00:00:00Z",
      "amount": 156.00,
      "status": "pending"
    }
  ]
}
```

### 2. Record a Payment Attempt

**POST** `/payments/process`

Records a payment attempt and processes it through the payment gateway.

**Request Body:**
```json
{
  "policyId": "12345",
  "amount": 171.00,
  "paymentMethod": {
    "type": "credit_card",
    "cardNumber": "4532123456789012",
    "expiryMonth": 12,
    "expiryYear": 2026,
    "cvv": "123",
    "holderName": "John Doe"
  },
  "billingAddress": {
    "street": "123 Main St",
    "city": "Boston",
    "state": "MA",
    "zipCode": "02101"
  }
}
```

**Response:**
```json
{
  "transactionId": "TXN-2024-001235",
  "status": "success",
  "amount": 171.00,
  "policyId": "12345",
  "paymentMethod": "visa_****1234",
  "timestamp": "2024-12-18T15:15:00Z",
  "confirmationCode": "CONF-ABC123",
  "processingFee": 0.00,
  "message": "Payment processed successfully"
}
```

**Error Response:**
```json
{
  "transactionId": "TXN-2024-001236",
  "status": "failed",
  "amount": 171.00,
  "policyId": "12345",
  "paymentMethod": "visa_****1234",
  "timestamp": "2024-12-18T15:15:00Z",
  "errorCode": "INSUFFICIENT_FUNDS",
  "message": "Payment declined due to insufficient funds",
  "retrySchedule": {
    "nextRetryDate": "2024-12-21T00:00:00Z",
    "maxRetries": 3,
    "currentRetryCount": 0
  }
}
```

### 3. List Delinquent Policies

**GET** `/billing/policies/delinquent`

Returns a list of policies that are currently delinquent (overdue payments).

**Query Parameters:**
- `limit` (optional) - Number of records to return (default: 50)
- `offset` (optional) - Number of records to skip (default: 0)
- `minDaysOverdue` (optional) - Minimum days overdue filter
- `customerId` (optional) - Filter by specific customer

**Response:**
```json
{
  "totalCount": 156,
  "delinquentPolicies": [
    {
      "policyId": "12345",
      "customerId": "CUST-001",
      "customerName": "John Doe",
      "policyType": "auto_insurance",
      "premiumAmount": 156.00,
      "dueDate": "2024-12-15T00:00:00Z",
      "daysOverdue": 3,
      "lateFee": 15.00,
      "totalAmountDue": 171.00,
      "status": "overdue",
      "gracePeriodExpires": "2024-12-25T00:00:00Z",
      "lastPaymentDate": "2024-11-15T00:00:00Z",
      "contactInfo": {
        "email": "john.doe@example.com",
        "phone": "+1-555-123-4567"
      }
    }
  ]
}
```

### 4. Trigger Payment Retry

**POST** `/payments/{transactionId}/retry`

Triggers a retry for a previously failed payment.

**Parameters:**
- `transactionId` (path) - The original transaction ID that failed

**Request Body:**
```json
{
  "retryReason": "customer_request",
  "useAlternatePaymentMethod": false,
  "newPaymentMethod": null
}
```

**Response:**
```json
{
  "originalTransactionId": "TXN-2024-001236",
  "newTransactionId": "TXN-2024-001237",
  "status": "processing",
  "retryAttempt": 1,
  "timestamp": "2024-12-18T16:00:00Z",
  "estimatedCompletionTime": "2024-12-18T16:05:00Z"
}
```

## Additional API Endpoints

### 5. Get Payment History

**GET** `/payments/history`

**Query Parameters:**
- `policyId` (optional)
- `customerId` (optional) 
- `startDate` (optional)
- `endDate` (optional)
- `status` (optional) - paid, failed, pending
- `limit` (optional)
- `offset` (optional)

### 6. Update Payment Method

**PUT** `/customers/{customerId}/payment-methods/{methodId}`

### 7. Setup AutoPay

**POST** `/billing/policies/{policyId}/autopay`

### 8. Send Payment Reminder

**POST** `/notifications/payment-reminders`

### 9. Grace Period Status

**GET** `/billing/policies/{policyId}/grace-period`

### 10. Calculate Late Fees

**GET** `/billing/policies/{policyId}/late-fees`

## Error Codes

| Code | Description |
|------|-------------|
| `INVALID_POLICY` | Policy ID not found or invalid |
| `PAYMENT_DECLINED` | Payment was declined by processor |
| `INSUFFICIENT_FUNDS` | Not enough funds in account |
| `INVALID_CARD` | Card number or details invalid |
| `EXPIRED_CARD` | Card has expired |
| `PROCESSING_ERROR` | Generic processing error |
| `RETRY_LIMIT_EXCEEDED` | Maximum retry attempts reached |
| `GRACE_PERIOD_EXPIRED` | Policy grace period has expired |

## Rate Limiting

- 1000 requests per hour per API key
- 10 requests per second burst limit

## Webhook Events

The system can send webhooks for the following events:
- `payment.succeeded`
- `payment.failed` 
- `payment.retry_scheduled`
- `policy.grace_period_entered`
- `policy.lapsed`
- `autopay.enabled`
- `autopay.failed`

## Testing

Use the following test data for development:

**Test Policy IDs:**
- `12345` - Overdue auto insurance
- `67890` - Current home insurance  
- `24680` - Current life insurance

**Test Payment Methods:**
- `4532123456789012` - Success
- `4000000000000002` - Declined
- `4000000000000119` - Processing error
- `4000000000000051` - Expired card

**Test Customer IDs:**
- `CUST-001` - John Doe (multiple policies)
- `CUST-002` - Jane Smith (single policy)
