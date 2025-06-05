# Policy Billing System - Updated REST API Documentation

This document provides the current API endpoints for the insurance policy billing system based on the actual implementation.

## Service Overview

The system consists of four microservices:
- **Policy Service** (port 8081) - Manages policy metadata and premium schedules
- **Billing Service** (port 8082) - Calculates premiums and manages billing cycles  
- **Payment Service** (port 8083) - Handles payment processing and retry logic
- **Notification Service** (port 8084) - Manages payment reminders and notifications

## Authentication

All endpoints currently use basic authentication:
```
Authorization: Basic admin:adminPassword
```

## Policy Service API (Port 8081)

### Health Check
**GET** `/api/policies/hello`

**Response:**
```json
{
  "service": "policy-service",
  "message": "Hello from Policy Service!",
  "status": "UP"
}
```

### Get Policy by ID
**GET** `/api/policies/{id}`

**Response:**
```json
{
  "id": "POLICY-123",
  "policyNumber": "PN-12345",
  "customerId": "CUST-001",
  "policyType": "LIFE",
  "status": "ACTIVE",
  "effectiveDate": "2024-01-01",
  "expirationDate": "2025-01-01",
  "premiumAmount": 156.00,
  "frequency": "MONTHLY",
  "gracePeriodDays": 10,
  "nextDueDate": "2024-12-15"
}
```

### Get All Policies
**GET** `/api/policies`

**Response:** Array of policy objects

### Create Policy
**POST** `/api/policies`

**Request Body:**
```json
{
  "policyNumber": "PN-12345",
  "customerId": "CUST-001",
  "policyType": "AUTO",
  "status": "ACTIVE",
  "effectiveDate": "2024-01-01",
  "expirationDate": "2025-01-01",
  "premiumAmount": 200.00,
  "frequency": "MONTHLY",
  "gracePeriodDays": 15
}
```

### Get Policies for Customer
**GET** `/api/policies/customer/{customerId}`

### Get Premium Schedule for Policy
**GET** `/api/policies/{id}/premium-schedule`

**Response:**
```json
{
  "policyId": "POLICY-123",
  "premiumAmount": 200.00,
  "billingFrequency": "MONTHLY",
  "nextDueDate": "2024-12-15",
  "gracePeriodDays": 10,
  "status": "ACTIVE",
  "daysOverdue": null,
  "lateFee": null,
  "totalAmountDue": null,
  "schedule": []
}
```

## Billing Service API (Port 8082)

### Health Check
**GET** `/api/billing/hello`

**Response:**
```json
{
  "service": "billing-service",
  "message": "Hello from Billing Service!",
  "timestamp": "2024-12-18T15:15:00Z",
  "status": "UP"
}
```

### Get Premium for Policy
**GET** `/api/billing/{policyId}/premium`

**Response:**
```json
{
  "policyId": "POLICY-123",
  "premiumAmount": 150.00,
  "frequency": "MONTHLY",
  "calculatedAt": "2024-12-18T15:15:00Z",
  "message": "Premium calculation completed - implementation pending"
}
```

### Calculate Premium
**POST** `/api/billing/calculate`

**Request Body:**
```json
{
  "policyType": "AUTO_INSURANCE",
  "coverageAmount": 50000,
  "riskFactors": ["GOOD_DRIVER"]
}
```

**Response:**
```json
{
  "calculatedPremium": 150.00,
  "frequency": "MONTHLY",
  "effectiveDate": "2024-12-18T15:15:00Z",
  "baseAmount": 135.00,
  "fees": 15.00,
  "message": "Premium calculation completed - implementation pending"
}
```

### Get Due Premiums
**GET** `/api/billing/due`

**Response:**
```json
{
  "duePremiums": [],
  "totalDue": 0,
  "count": 0,
  "message": "Due premiums endpoint - implementation pending"
}
```

### Get Delinquent Policies
**GET** `/api/billing/delinquent`

**Response:**
```json
[
  {
    "policyId": "POLICY-123",
    "customerId": "CUST-001",
    "customerName": "John Doe",
    "daysOverdue": 15,
    "amountOverdue": 171.00,
    "lastPaymentDate": "2024-11-15",
    "gracePeriodExpiry": "2024-12-13T15:15:00Z"
  }
]
```

### Calculate Premium for Policy
**GET** `/api/billing/{policyId}/calculate`

### Update Billing Status
**POST** `/api/billing/{policyId}/status`

**Request Body:**
```json
{
  "paymentStatus": "PAID"
}
```

### Get Billings by Customer
**GET** `/api/billing/customer/{customerId}`

### Get Billings by Policy
**GET** `/api/billing/policy/{policyId}`

### Submit Payment
**POST** `/api/billing/payments`

**Request Body:**
```json
{
  "billId": "BILL-123",
  "amount": 171.00,
  "paymentMethod": "CREDIT_CARD"
}
```

## Payment Service API (Port 8083)

### Health Check
**GET** `/api/payments/hello`

**Response:**
```json
{
  "service": "payment-service",
  "message": "Hello from Payment Service!",
  "timestamp": "2024-12-18T15:15:00Z",
  "status": "UP"
}
```

### Process Payment
**POST** `/api/payments/process`

**Request Body:**
```json
{
  "policyId": "POLICY-123",
  "amount": 171.00,
  "paymentMethod": "CREDIT_CARD"
}
```

**Response:**
```json
{
  "id": "TXN-12345",
  "policyId": "POLICY-123",
  "amount": 171.00,
  "status": "COMPLETED",
  "timestamp": "2024-12-18T15:15:00Z",
  "paymentMethod": "CREDIT_CARD"
}
```

### Get Payment History
**GET** `/api/payments/history?policyId={policyId}&status={status}&limit={limit}&offset={offset}`

### Get Payment Status
**GET** `/api/payments/payments/{transactionId}/status`

**Response:**
```json
{
  "transactionId": "TXN-12345",
  "status": "COMPLETED",
  "amount": 171.00,
  "policyId": "POLICY-123",
  "billId": "BILL-1",
  "attemptedAt": "2024-12-18T15:15:00Z"
}
```

### Retry Payment
**POST** `/api/payments/{paymentId}/retry`

### Get Failed Payments
**GET** `/api/payments/failed`

### Get Payment History for Policy
**GET** `/api/payments/policy/{policyId}`

### Initiate Refund
**POST** `/api/payments/{transactionId}/refund`

**Request Body (optional):**
```json
{
  "amount": 100.00
}
```

### Get Payment Statistics
**GET** `/api/payments/statistics`

**Response:**
```json
{
  "totalTransactions": 150,
  "completedTransactions": 120,
  "failedTransactions": 30,
  "successRate": 0.8,
  "totalAmountProcessed": 25000.00,
  "generatedAt": "2024-12-18T15:15:00Z"
}
```

### Update Payment Status
**PUT** `/api/payments/{transactionId}/status`

**Request Body:**
```json
{
  "status": "COMPLETED"
}
```

### Get Delinquent Policies
**GET** `/api/payments/delinquent?limit={limit}&offset={offset}&minDaysOverdue={minDaysOverdue}&customerId={customerId}`

**Response:**
```json
{
  "totalCount": 2,
  "delinquentPolicies": ["POLICY-123", "POLICY-456"]
}
```

## Notification Service API (Port 8084)

### Health Check
**GET** `/api/notifications/hello`

**Response:**
```json
{
  "service": "notification-service",
  "message": "Hello from Notification Service!",
  "timestamp": "2024-12-18T15:15:00Z",
  "status": "UP"
}
```

### Send Notification
**POST** `/api/notifications/send`

**Request Body:**
```json
{
  "customerId": "CUST-001",
  "type": "PAYMENT_REMINDER",
  "message": "Your payment is due soon"
}
```

**Response:**
```json
{
  "notificationId": "NOTIF-1734539700123",
  "status": "SENT",
  "message": "Notification send endpoint - implementation pending",
  "timestamp": "2024-12-18T15:15:00Z"
}
```

### Get Notifications for Policy
**GET** `/api/notifications/{policyId}`

**Response:**
```json
{
  "policyId": "POLICY-123",
  "notifications": [],
  "count": 0,
  "message": "Notification retrieval endpoint - implementation pending"
}
```

## Health Check Endpoints

All services expose health check endpoints:
- **GET** `/actuator/health` - Spring Boot Actuator health endpoint
- **GET** `/actuator/info` - Application information
- **GET** `/actuator/metrics` - Application metrics

## Error Responses

Standard error response format:
```json
{
  "error": "Error description",
  "timestamp": "2024-12-18T15:15:00Z",
  "status": 404,
  "path": "/api/policies/NONEXISTENT"
}
```

## Status Codes

- `200 OK` - Successful request
- `201 Created` - Resource created successfully
- `400 Bad Request` - Invalid request data
- `401 Unauthorized` - Authentication required
- `404 Not Found` - Resource not found
- `500 Internal Server Error` - Server error

## Notes

- All timestamps are in ISO 8601 format (UTC)
- Monetary amounts are represented as decimal numbers
- Policy IDs, Customer IDs, and Transaction IDs are strings
- Some endpoints are marked as "implementation pending" and return mock data
- The system uses event-driven architecture with Kafka for inter-service communication