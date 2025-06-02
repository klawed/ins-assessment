# User Journey: Login Flow

## Sequence Diagram

```mermaid
sequenceDiagram
    participant U as User
    participant WEB as Web Interface
    participant AUTH as Auth Service
    participant BILLING as Billing Service
    participant DB as Database

    U->>WEB: Enter login credentials
    WEB->>AUTH: POST /auth/login
    AUTH->>DB: Validate credentials
    DB-->>AUTH: User data
    AUTH-->>WEB: JWT Token + User info
    WEB->>BILLING: GET /policies/user/{userId}
    BILLING->>DB: Query user policies
    DB-->>BILLING: Policy data
    BILLING-->>WEB: Policy list with billing status
    WEB-->>U: Dashboard with policies
```

## UI Mockups

### Login Screen
```
┌─────────────────────────────────────────────────────────┐
│                    InsuranceCorp                        │
│                                                         │
│                     Login Portal                       │
│                                                         │
│   ┌─────────────────────────────────────────────────┐   │
│   │  Email Address                                  │   │
│   │  ┌─────────────────────────────────────────┐   │   │
│   │  │ user@example.com                       │   │   │
│   │  └─────────────────────────────────────────┘   │   │
│   └─────────────────────────────────────────────────┘   │
│                                                         │
│   ┌─────────────────────────────────────────────────┐   │
│   │  Password                                       │   │
│   │  ┌─────────────────────────────────────────┐   │   │
│   │  │ ••••••••••••                           │   │   │
│   │  └─────────────────────────────────────────┘   │   │
│   └─────────────────────────────────────────────────┘   │
│                                                         │
│              ┌─────────────────┐                       │
│              │     LOGIN       │                       │
│              └─────────────────┘                       │
│                                                         │
│              Forgot Password? | Sign Up                 │
└─────────────────────────────────────────────────────────┘
```

### Dashboard After Login
```
┌─────────────────────────────────────────────────────────┐
│  InsuranceCorp | Welcome, John Doe          [Logout]   │
├─────────────────────────────────────────────────────────┤
│                                                         │
│  My Policies                                            │
│                                                         │
│  ┌─────────────────────────────────────────────────┐   │
│  │ Auto Insurance - Policy #12345                 │   │
│  │ Premium: $156.00/month                         │   │
│  │ Next Due: Dec 15, 2024                         │   │
│  │ Status: ⚠️  OVERDUE (3 days)                   │   │
│  │                        [PAY NOW] [VIEW DETAILS]│   │
│  └─────────────────────────────────────────────────┘   │
│                                                         │
│  ┌─────────────────────────────────────────────────┐   │
│  │ Home Insurance - Policy #67890                 │   │
│  │ Premium: $89.00/month                          │   │
│  │ Next Due: Jan 1, 2025                          │   │
│  │ Status: ✅ CURRENT                             │   │
│  │                        [PAY NOW] [VIEW DETAILS]│   │
│  └─────────────────────────────────────────────────┘   │
│                                                         │
│  Payment History | Account Settings                    │
└─────────────────────────────────────────────────────────┘
```

## Error Handling
```
┌─────────────────────────────────────────────────────────┐
│                    InsuranceCorp                        │
│                                                         │
│  ❌ Login Failed                                       │
│  Invalid email or password. Please try again.          │
│                                                         │
│   ┌─────────────────────────────────────────────────┐   │
│   │  Email Address                                  │   │
│   │  ┌─────────────────────────────────────────┐   │   │
│   │  │ user@example.com                       │   │   │
│   │  └─────────────────────────────────────────┘   │   │
│   └─────────────────────────────────────────────────┘   │
│                                                         │
│   ┌─────────────────────────────────────────────────┐   │
│   │  Password                                       │   │
│   │  ┌─────────────────────────────────────────┐   │   │
│   │  │                                        │   │   │
│   │  └─────────────────────────────────────────┘   │   │
│   └─────────────────────────────────────────────────┘   │
│                                                         │
│              ┌─────────────────┐                       │
│              │     LOGIN       │                       │
│              └─────────────────┘                       │
└─────────────────────────────────────────────────────────┘
```
