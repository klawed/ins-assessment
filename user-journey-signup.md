# User Journey: Sign-Up Flow

## Sequence Diagram

```mermaid
sequenceDiagram
    participant U as User
    participant WEB as Web Interface
    participant AUTH as Auth Service
    participant BILLING as Billing Service
    participant NOTIFY as Notification Service
    participant DB as Database

    U->>WEB: Click "Sign Up"
    WEB->>U: Display registration form
    U->>WEB: Submit registration data
    WEB->>AUTH: POST /auth/register
    AUTH->>DB: Check if email exists
    DB-->>AUTH: Email availability
    
    alt Email already exists
        AUTH-->>WEB: Error: Email taken
        WEB-->>U: Display error message
    else Email available
        AUTH->>DB: Create user account
        DB-->>AUTH: User created
        AUTH->>NOTIFY: Send verification email
        NOTIFY-->>U: Email verification link
        AUTH-->>WEB: Registration successful
        WEB-->>U: Show verification message
        
        U->>WEB: Click verification link
        WEB->>AUTH: GET /auth/verify/{token}
        AUTH->>DB: Update user status
        DB-->>AUTH: Account verified
        AUTH-->>WEB: Verification successful
        WEB->>BILLING: Initialize billing profile
        BILLING->>DB: Create billing record
        DB-->>BILLING: Profile created
        WEB-->>U: Redirect to dashboard
    end
```

## UI Mockups

### Registration Form
```
┌─────────────────────────────────────────────────────────┐
│                    InsuranceCorp                        │
│                                                         │
│                   Create Account                        │
│                                                         │
│   ┌─────────────────────────────────────────────────┐   │
│   │  First Name                                     │   │
│   │  ┌─────────────────────────────────────────┐   │   │
│   │  │ John                                   │   │   │
│   │  └─────────────────────────────────────────┘   │   │
│   └─────────────────────────────────────────────────┘   │
│                                                         │
│   ┌─────────────────────────────────────────────────┐   │
│   │  Last Name                                      │   │
│   │  ┌─────────────────────────────────────────┐   │   │
│   │  │ Doe                                    │   │   │
│   │  └─────────────────────────────────────────┘   │   │
│   └─────────────────────────────────────────────────┘   │
│                                                         │
│   ┌─────────────────────────────────────────────────┐   │
│   │  Email Address                                  │   │
│   │  ┌─────────────────────────────────────────┐   │   │
│   │  │ john.doe@example.com                   │   │   │
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
│   ┌─────────────────────────────────────────────────┐   │
│   │  Confirm Password                               │   │
│   │  ┌─────────────────────────────────────────┐   │   │
│   │  │ ••••••••••••                           │   │   │
│   │  └─────────────────────────────────────────┘   │   │
│   └─────────────────────────────────────────────────┘   │
│                                                         │
│   ☐ I agree to the Terms of Service and Privacy Policy │
│                                                         │
│              ┌─────────────────┐                       │
│              │   CREATE ACCOUNT │                       │
│              └─────────────────┘                       │
│                                                         │
│              Already have an account? Login             │
└─────────────────────────────────────────────────────────┘
```

### Email Verification Screen
```
┌─────────────────────────────────────────────────────────┐
│                    InsuranceCorp                        │
│                                                         │
│                 ✅ Account Created!                     │
│                                                         │
│     We've sent a verification email to:                │
│            john.doe@example.com                         │
│                                                         │
│     Please check your inbox and click the              │
│     verification link to activate your account.        │
│                                                         │
│                                                         │
│     📧 Didn't receive the email?                       │
│                                                         │
│              ┌─────────────────┐                       │
│              │   RESEND EMAIL  │                       │
│              └─────────────────┘                       │
│                                                         │
│              Change Email Address | Support             │
└─────────────────────────────────────────────────────────┘
```

### Account Verification Success
```
┌─────────────────────────────────────────────────────────┐
│                    InsuranceCorp                        │
│                                                         │
│              ✅ Email Verified!                        │
│                                                         │
│     Your account has been successfully verified.       │
│     You can now access your dashboard and manage       │
│     your insurance policies.                           │
│                                                         │
│                                                         │
│              ┌─────────────────┐                       │
│              │  GO TO DASHBOARD │                       │
│              └─────────────────┘                       │
│                                                         │
│                                                         │
│     Next steps:                                         │
│     • Complete your profile                            │
│     • Add your first policy                            │
│     • Set up payment methods                           │
└─────────────────────────────────────────────────────────┘
```

### Error States

#### Email Already Exists
```
┌─────────────────────────────────────────────────────────┐
│                    InsuranceCorp                        │
│                                                         │
│  ❌ Registration Failed                                │
│  An account with this email already exists.            │
│                                                         │
│   ┌─────────────────────────────────────────────────┐   │
│   │  Email Address                                  │   │
│   │  ┌─────────────────────────────────────────┐   │   │
│   │  │ john.doe@example.com              🔴   │   │   │
│   │  └─────────────────────────────────────────┘   │   │
│   └─────────────────────────────────────────────────┘   │
│                                                         │
│              ┌─────────────────┐                       │
│              │     LOGIN       │                       │
│              └─────────────────┘                       │
│                                                         │
│              Forgot Password?                           │
└─────────────────────────────────────────────────────────┘
```

#### Password Validation Error
```
┌─────────────────────────────────────────────────────────┐
│                    InsuranceCorp                        │
│                                                         │
│  ⚠️  Password Requirements                             │
│  Password must meet the following criteria:            │
│                                                         │
│   ✅ At least 8 characters                             │
│   ❌ At least one uppercase letter                     │
│   ✅ At least one lowercase letter                     │
│   ❌ At least one number                               │
│   ❌ At least one special character                    │
│                                                         │
│   ┌─────────────────────────────────────────────────┐   │
│   │  Password                                       │   │
│   │  ┌─────────────────────────────────────────┐   │   │
│   │  │ password123                      🔴    │   │   │
│   │  └─────────────────────────────────────────┘   │   │
│   └─────────────────────────────────────────────────┘   │
│                                                         │
│              ┌─────────────────┐                       │
│              │   UPDATE PASSWORD│                       │
│              └─────────────────┘                       │
└─────────────────────────────────────────────────────────┘
```

## Mobile Responsive View
```
┌─────────────────────┐
│   InsuranceCorp     │
├─────────────────────┤
│  Create Account     │
│                     │
│ ┌─────────────────┐ │
│ │ First Name      │ │
│ │ John           │ │
│ └─────────────────┘ │
│                     │
│ ┌─────────────────┐ │
│ │ Last Name       │ │
│ │ Doe            │ │
│ └─────────────────┘ │
│                     │
│ ┌─────────────────┐ │
│ │ Email           │ │
│ │ john@email.com │ │
│ └─────────────────┘ │
│                     │
│ ┌─────────────────┐ │
│ │ Password        │ │
│ │ ••••••••••     │ │
│ └─────────────────┘ │
│                     │
│ ☐ I agree to T&C    │
│                     │
│ ┌─────────────────┐ │
│ │ CREATE ACCOUNT  │ │
│ └─────────────────┘ │
│                     │
│ Have an account?    │
│ Login               │
└─────────────────────┘
```
