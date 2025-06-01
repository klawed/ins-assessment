# UI Application Plan

## Overview

The UI applications are designed to exercise the billing system APIs with a focus on developer ergonomics and simplicity. In a production environment, these would likely be SPAs with a backend-for-frontend service, but for development and testing purposes, we're creating simple HTML applications that demonstrate API functionality.

## Application Architecture

### Customer UI Application
**Purpose:** Exercise customer-facing billing APIs and demonstrate end-user workflows

**Target Users:** 
- Insurance policyholders
- Customer service representatives
- QA testers validating customer flows

**Core Functions:**
- View policy details and premium schedules
- Make payments and view payment history
- Manage payment methods and autopay settings
- Receive billing notifications and alerts

**Hosting Service:** **Policy Service (Port 8081)**
- Rationale: Customer UI primarily deals with policy data and associated billing
- Policy service already handles policy metadata and premium schedules
- Natural fit for customer-centric operations
- Can easily proxy billing/payment API calls to other services

---

### Admin UI Application
**Purpose:** Exercise administrative and operational billing APIs for system management

**Target Users:**
- Billing operations team
- Customer service managers
- System administrators
- Developers testing internal APIs

**Core Functions:**
- View delinquent policies dashboard
- Manage payment retry operations
- Monitor payment processing status
- Generate billing reports and analytics
- Trigger manual billing operations

**Hosting Service:** **Billing Service (Port 8082)**
- Rationale: Admin UI focuses on billing operations and system management
- Billing service handles premium calculations and delinquency tracking
- Contains the business logic for retry scheduling and grace period management
- Appropriate for internal operational tools

---

## Technical Implementation

### Customer UI (`/customer-ui/`)
**Static Files Structure:**
```
policy-service/src/main/resources/static/customer-ui/
├── index.html (dashboard)
├── policies.html
├── policy-details.html
├── billing.html
├── payment.html
├── payment-history.html
├── login.html
├── styles.css
└── app.js (minimal JavaScript for API calls)
```

**API Integration:**
- Served by Policy Service static content handler
- Makes AJAX calls to `/api/policies/*`, `/api/billing/*`, `/api/payments/*`
- Cross-service API calls handled via service-to-service communication

### Admin UI (`/admin-ui/`)
**Static Files Structure:**
```
billing-service/src/main/resources/static/admin-ui/
├── index.html (admin dashboard)
├── delinquent-policies.html
├── retry-management.html
├── payment-monitoring.html
├── billing-reports.html
├── login.html
├── admin-styles.css
└── admin.js (JavaScript for admin operations)
```

**API Integration:**
- Served by Billing Service static content handler
- Makes AJAX calls to `/api/billing/*`, `/api/payments/*`, `/api/admin/*`
- Focus on operational and administrative endpoints

---

## Developer Ergonomics

### Simplified Development Workflow
1. **No Build Process:** Plain HTML/CSS/JS files
2. **Hot Reload:** Static files served directly from classpath
3. **API Testing:** Forms and buttons directly call REST endpoints
4. **Error Handling:** Simple alerts and console logging
5. **Mock Data:** Embedded test data for offline development

### API Exercise Strategy
- Each UI page corresponds to specific API endpoints
- Form submissions demonstrate POST/PUT operations
- Tables and lists demonstrate GET operations with filtering
- Action buttons demonstrate DELETE and operational endpoints

### Testing Benefits
- **Manual Testing:** Easy to test API endpoints manually
- **Integration Testing:** UI can be used in automated tests
- **Demo/Documentation:** Visual representation of API capabilities
- **Debugging:** Clear visibility into API request/response cycles

---

## Service Hosting Rationale

### Why Policy Service for Customer UI?
- **Data Ownership:** Policy service owns customer policy data
- **API Proximity:** Most customer operations start with policy lookup
- **Logical Grouping:** Customer-facing vs admin-facing separation
- **Scalability:** Customer UI traffic patterns align with policy service scaling

### Why Billing Service for Admin UI?
- **Operational Focus:** Admin operations are billing-centric
- **Internal Tool:** Admin UI is internal-only, matches billing service security model
- **Data Access:** Billing service has direct access to delinquency and retry data
- **Maintenance:** Admin tools typically have different release cycles

---

## Alternative Hosting Options

### Option 1: Dedicated UI Service
**Pros:** Clean separation, independent scaling, unified asset management
**Cons:** Additional service complexity, extra deployment, overkill for simple UIs

### Option 2: API Gateway
**Pros:** Centralized routing, security, caching
**Cons:** Gateway becomes stateful, static asset handling complexity

### Option 3: Notification Service
**Pros:** Low traffic service, simple hosting
**Cons:** Doesn't align with UI functionality, confusing service boundaries

---

## Recommended Implementation

1. **Start with proposed architecture** (Policy Service → Customer UI, Billing Service → Admin UI)
2. **Add static content handlers** to both services
3. **Implement simple HTML pages** with minimal JavaScript
4. **Test API integration** through form submissions and AJAX calls
5. **Iterate based on developer feedback** and API testing needs

This approach prioritizes simplicity and developer productivity while providing clear API exercise capabilities for both customer and administrative workflows.
