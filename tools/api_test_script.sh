#!/bin/bash

# Policy Billing System API Test Script
# This script tests all available API endpoints across the four microservices

# Color codes for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Service configuration
POLICY_SERVICE_URL="http://localhost:8081"
BILLING_SERVICE_URL="http://localhost:8082"
PAYMENT_SERVICE_URL="http://localhost:8083"
NOTIFICATION_SERVICE_URL="http://localhost:8084"

# Authentication
AUTH_HEADER="Authorization: Basic $(echo -n 'admin:adminPassword' | base64)"

# Function to print colored output
print_header() {
    echo -e "${BLUE}================================${NC}"
    echo -e "${BLUE}$1${NC}"
    echo -e "${BLUE}================================${NC}"
}

print_success() {
    echo -e "${GREEN}✓ $1${NC}"
}

print_error() {
    echo -e "${RED}✗ $1${NC}"
}

print_warning() {
    echo -e "${YELLOW}⚠ $1${NC}"
}

# Function to make API calls with error handling
api_call() {
    local method=$1
    local url=$2
    local data=$3
    local description=$4
    
    echo -e "\n${YELLOW}Testing: $description${NC}"
    echo "Request: $method $url"
    
    if [ "$method" = "GET" ]; then
        response=$(curl -s -w "%{http_code}" -H "$AUTH_HEADER" "$url")
    elif [ "$method" = "POST" ]; then
        response=$(curl -s -w "%{http_code}" -X POST -H "$AUTH_HEADER" -H "Content-Type: application/json" -d "$data" "$url")
    elif [ "$method" = "PUT" ]; then
        response=$(curl -s -w "%{http_code}" -X PUT -H "$AUTH_HEADER" -H "Content-Type: application/json" -d "$data" "$url")
    fi
    
    http_code="${response: -3}"
    response_body="${response%???}"
    
    if [ "$http_code" -ge 200 ] && [ "$http_code" -lt 300 ]; then
        print_success "HTTP $http_code - Success"
        echo "Response: $response_body" | jq . 2>/dev/null || echo "Response: $response_body"
    elif [ "$http_code" -ge 400 ] && [ "$http_code" -lt 500 ]; then
        print_warning "HTTP $http_code - Client Error"
        echo "Response: $response_body"
    else
        print_error "HTTP $http_code - Server Error"
        echo "Response: $response_body"
    fi
}

# Function to check service health
check_service_health() {
    local service_name=$1
    local service_url=$2
    
    echo -e "\n${BLUE}Checking $service_name health...${NC}"
    
    # Check actuator health endpoint
    health_response=$(curl -s -w "%{http_code}" "$service_url/actuator/health")
    health_code="${health_response: -3}"
    
    if [ "$health_code" = "200" ]; then
        print_success "$service_name is healthy"
    else
        print_error "$service_name health check failed (HTTP $health_code)"
        return 1
    fi
}

# Main execution
main() {
    print_header "Policy Billing System API Test Suite"
    
    # Check if jq is available for JSON formatting
    if ! command -v jq &> /dev/null; then
        print_warning "jq not found - JSON responses will not be formatted"
    fi
    
    # Health checks for all services
    print_header "Service Health Checks"
    check_service_health "Policy Service" "$POLICY_SERVICE_URL"
    check_service_health "Billing Service" "$BILLING_SERVICE_URL"
    check_service_health "Payment Service" "$PAYMENT_SERVICE_URL"
    check_service_health "Notification Service" "$NOTIFICATION_SERVICE_URL"
    
    # Policy Service Tests
    print_header "Policy Service API Tests"
    
    api_call "GET" "$POLICY_SERVICE_URL/api/policies/hello" "" "Policy Service Hello"
    api_call "GET" "$POLICY_SERVICE_URL/api/policies" "" "Get All Policies"
    api_call "GET" "$POLICY_SERVICE_URL/api/policies/POLICY-001" "" "Get Policy by ID"
    api_call "GET" "$POLICY_SERVICE_URL/api/policies/customer/CUST-001" "" "Get Policies for Customer"
    api_call "GET" "$POLICY_SERVICE_URL/api/policies/POLICY-001/premium-schedule" "" "Get Premium Schedule"
    
    # Create a new policy
    policy_data='{
        "policyNumber": "PN-TEST-001",
        "customerId": "CUST-TEST-001",
        "policyType": "AUTO",
        "status": "ACTIVE",
        "effectiveDate": "2024-01-01",
        "expirationDate": "2025-01-01",
        "premiumAmount": 200.00,
        "frequency": "MONTHLY",
        "gracePeriodDays": 15
    }'
    api_call "POST" "$POLICY_SERVICE_URL/api/policies" "$policy_data" "Create New Policy"
    
    # Billing Service Tests
    print_header "Billing Service API Tests"
    
    api_call "GET" "$BILLING_SERVICE_URL/api/billing/hello" "" "Billing Service Hello"
    api_call "GET" "$BILLING_SERVICE_URL/api/billing/POLICY-123/premium" "" "Get Premium for Policy"
    api_call "GET" "$BILLING_SERVICE_URL/api/billing/due" "" "Get Due Premiums"
    api_call "GET" "$BILLING_SERVICE_URL/api/billing/delinquent" "" "Get Delinquent Policies"
    api_call "GET" "$BILLING_SERVICE_URL/api/billing/POLICY-123/calculate" "" "Calculate Premium for Policy"
    api_call "GET" "$BILLING_SERVICE_URL/api/billing/customer/CUST-001" "" "Get Billings by Customer"
    api_call "GET" "$BILLING_SERVICE_URL/api/billing/policy/POLICY-123" "" "Get Billings by Policy"
    
    # Calculate premium
    premium_calc_data='{
        "policyType": "AUTO_INSURANCE",
        "coverageAmount": 50000,
        "riskFactors": ["GOOD_DRIVER"]
    }'
    api_call "POST" "$BILLING_SERVICE_URL/api/billing/calculate" "$premium_calc_data" "Calculate Premium"
    
    # Update billing status
    status_update_data='{"paymentStatus": "PAID"}'
    api_call "POST" "$BILLING_SERVICE_URL/api/billing/POLICY-123/status" "$status_update_data" "Update Billing Status"
    
    # Submit payment through billing service
    payment_data='{
        "billId": "BILL-123",
        "amount": 171.00,
        "paymentMethod": "CREDIT_CARD"
    }'
    api_call "POST" "$BILLING_SERVICE_URL/api/billing/payments" "$payment_data" "Submit Payment via Billing"
    
    # Payment Service Tests
    print_header "Payment Service API Tests"
    
    api_call "GET" "$PAYMENT_SERVICE_URL/api/payments/hello" "" "Payment Service Hello"
    api_call "GET" "$PAYMENT_SERVICE_URL/api/payments/history?policyId=POLICY-123" "" "Get Payment History"
    api_call "GET" "$PAYMENT_SERVICE_URL/api/payments/failed" "" "Get Failed Payments"
    api_call "GET" "$PAYMENT_SERVICE_URL/api/payments/policy/POLICY-123" "" "Get Payment History for Policy"
    api_call "GET" "$PAYMENT_SERVICE_URL/api/payments/statistics" "" "Get Payment Statistics"
    
    # Process payment
    payment_process_data='{
        "policyId": "POLICY-123",
        "amount": 171.00,
        "paymentMethod": "CREDIT_CARD"
    }'
    api_call "POST" "$PAYMENT_SERVICE_URL/api/payments/process" "$payment_process_data" "Process Payment"
    
    # Get delinquent policies
    api_call "GET" "$PAYMENT_SERVICE_URL/api/payments/delinquent?limit=50&offset=0&minDaysOverdue=1" "" "Get Delinquent Policies"
    
    # Notification Service Tests
    print_header "Notification Service API Tests"
    
    api_call "GET" "$NOTIFICATION_SERVICE_URL/api/notifications/hello" "" "Notification Service Hello"
    api_call "GET" "$NOTIFICATION_SERVICE_URL/api/notifications/POLICY-123" "" "Get Notifications for Policy"
    
    # Send notification
    notification_data='{
        "customerId": "CUST-001",
        "type": "PAYMENT_REMINDER",
        "message": "Your payment is due soon"
    }'
    api_call "POST" "$NOTIFICATION_SERVICE_URL/api/notifications/send" "$notification_data" "Send Notification"
    
    # Error Case Tests
    print_header "Error Case Tests"
    
    api_call "GET" "$POLICY_SERVICE_URL/api/policies/NONEXISTENT" "" "Get Non-existent Policy (404)"
    api_call "GET" "$BILLING_SERVICE_URL/api/billing/NONEXISTENT/premium" "" "Get Premium for Non-existent Policy"
    api_call "GET" "$PAYMENT_SERVICE_URL/api/payments/payments/NONEXISTENT/status" "" "Get Status for Non-existent Transaction"
    
    # Actuator Endpoints Tests
    print_header "Actuator Endpoints Tests"
    
    api_call "GET" "$POLICY_SERVICE_URL/actuator/health" "" "Policy Service Health"
    api_call "GET" "$POLICY_SERVICE_URL/actuator/info" "" "Policy Service Info"
    api_call "GET" "$BILLING_SERVICE_URL/actuator/health" "" "Billing Service Health"
    api_call "GET" "$BILLING_SERVICE_URL/actuator/info" "" "Billing Service Info"
    api_call "GET" "$PAYMENT_SERVICE_URL/actuator/health" "" "Payment Service Health"
    api_call "GET" "$PAYMENT_SERVICE_URL/actuator/info" "" "Payment Service Info"
    api_call "GET" "$NOTIFICATION_SERVICE_URL/actuator/health" "" "Notification Service Health"
    api_call "GET" "$NOTIFICATION_SERVICE_URL/actuator/info" "" "Notification Service Info"
    
    print_header "API Test Suite Complete"
    echo -e "${GREEN}All tests have been executed. Check the output above for results.${NC}"
    echo -e "${YELLOW}Note: Some endpoints may return mock data as they are still in development.${NC}"
}

# Check if script is being run with arguments
if [ "$1" = "--help" ] || [ "$1" = "-h" ]; then
    echo "Policy Billing System API Test Script"
    echo ""
    echo "Usage: $0 [OPTIONS]"
    echo ""
    echo "Options:"
    echo "  --help, -h          Show this help message"
    echo "  --service SERVICE   Test only specific service (policy|billing|payment|notification)"
    echo "  --health-only       Run only health checks"
    echo "  --no-color          Disable colored output"
    echo ""
    echo "Examples:"
    echo "  $0                  Run all tests"
    echo "  $0 --service policy Test only policy service"
    echo "  $0 --health-only    Check service health only"
    echo ""
    echo "Environment Variables:"
    echo "  POLICY_SERVICE_URL  Override policy service URL (default: http://localhost:8081)"
    echo "  BILLING_SERVICE_URL Override billing service URL (default: http://localhost:8082)"
    echo "  PAYMENT_SERVICE_URL Override payment service URL (default: http://localhost:8083)"
    echo "  NOTIFICATION_SERVICE_URL Override notification service URL (default: http://localhost:8084)"
    exit 0
fi

# Override URLs from environment variables if set
POLICY_SERVICE_URL=${POLICY_SERVICE_URL:-"http://localhost:8081"}
BILLING_SERVICE_URL=${BILLING_SERVICE_URL:-"http://localhost:8082"}
PAYMENT_SERVICE_URL=${PAYMENT_SERVICE_URL:-"http://localhost:8083"}
NOTIFICATION_SERVICE_URL=${NOTIFICATION_SERVICE_URL:-"http://localhost:8084"}

# Disable colors if requested
if [ "$1" = "--no-color" ]; then
    RED=''
    GREEN=''
    YELLOW=''
    BLUE=''
    NC=''
fi

# Function to test specific service
test_policy_service() {
    print_header "Policy Service API Tests"

    api_call "GET" "$POLICY_SERVICE_URL/api/policies/hello" "" "Policy Service Hello"
    api_call "GET" "$POLICY_SERVICE_URL/api/policies" "" "Get All Policies"
    api_call "GET" "$POLICY_SERVICE_URL/api/policies/POLICY-001" "" "Get Policy by ID"
    api_call "GET" "$POLICY_SERVICE_URL/api/policies/customer/CUST-001" "" "Get Policies for Customer"
    api_call "GET" "$POLICY_SERVICE_URL/api/policies/POLICY-001/premium-schedule" "" "Get Premium Schedule"

    # Create a new policy
    policy_data='{
        "policyNumber": "PN-TEST-001",
        "customerId": "CUST-TEST-001",
        "policyType": "AUTO",
        "status": "ACTIVE",
        "effectiveDate": "2024-01-01",
        "expirationDate": "2025-01-01",
        "premiumAmount": 200.00,
        "frequency": "MONTHLY",
        "gracePeriodDays": 15
    }'
    api_call "POST" "$POLICY_SERVICE_URL/api/policies" "$policy_data" "Create New Policy"

    # Test error cases
    api_call "GET" "$POLICY_SERVICE_URL/api/policies/NONEXISTENT" "" "Get Non-existent Policy (404)"
}

test_billing_service() {
    print_header "Billing Service API Tests"

    api_call "GET" "$BILLING_SERVICE_URL/api/billing/hello" "" "Billing Service Hello"
    api_call "GET" "$BILLING_SERVICE_URL/api/billing/POLICY-123/premium" "" "Get Premium for Policy"
    api_call "GET" "$BILLING_SERVICE_URL/api/billing/due" "" "Get Due Premiums"
    api_call "GET" "$BILLING_SERVICE_URL/api/billing/delinquent" "" "Get Delinquent Policies"
    api_call "GET" "$BILLING_SERVICE_URL/api/billing/POLICY-123/calculate" "" "Calculate Premium for Policy"
    api_call "GET" "$BILLING_SERVICE_URL/api/billing/customer/CUST-001" "" "Get Billings by Customer"
    api_call "GET" "$BILLING_SERVICE_URL/api/billing/policy/POLICY-123" "" "Get Billings by Policy"

    # Calculate premium
    premium_calc_data='{
        "policyType": "AUTO_INSURANCE",
        "coverageAmount": 50000,
        "riskFactors": ["GOOD_DRIVER"]
    }'
    api_call "POST" "$BILLING_SERVICE_URL/api/billing/calculate" "$premium_calc_data" "Calculate Premium"

    # Update billing status
    status_update_data='{"paymentStatus": "PAID"}'
    api_call "POST" "$BILLING_SERVICE_URL/api/billing/POLICY-123/status" "$status_update_data" "Update Billing Status"

    # Submit payment through billing service
    payment_data='{
        "billId": "BILL-123",
        "amount": 171.00,
        "paymentMethod": "CREDIT_CARD"
    }'
    api_call "POST" "$BILLING_SERVICE_URL/api/billing/payments" "$payment_data" "Submit Payment via Billing"

    # Test error cases
    api_call "GET" "$BILLING_SERVICE_URL/api/billing/NONEXISTENT/premium" "" "Get Premium for Non-existent Policy"
}

test_payment_service() {
    print_header "Payment Service API Tests"

    api_call "GET" "$PAYMENT_SERVICE_URL/api/payments/hello" "" "Payment Service Hello"
    api_call "GET" "$PAYMENT_SERVICE_URL/api/payments/history?policyId=POLICY-123" "" "Get Payment History"
    api_call "GET" "$PAYMENT_SERVICE_URL/api/payments/failed" "" "Get Failed Payments"
    api_call "GET" "$PAYMENT_SERVICE_URL/api/payments/policy/POLICY-123" "" "Get Payment History for Policy"
    api_call "GET" "$PAYMENT_SERVICE_URL/api/payments/statistics" "" "Get Payment Statistics"

    # Process payment
    payment_process_data='{
        "policyId": "POLICY-123",
        "amount": 171.00,
        "paymentMethod": "CREDIT_CARD"
    }'
    api_call "POST" "$PAYMENT_SERVICE_URL/api/payments/process" "$payment_process_data" "Process Payment"

    # Get delinquent policies
    api_call "GET" "$PAYMENT_SERVICE_URL/api/payments/delinquent?limit=50&offset=0&minDaysOverdue=1" "" "Get Delinquent Policies"

    # Test retry payment (this will likely fail since we don't have a real transaction ID)
    api_call "POST" "$PAYMENT_SERVICE_URL/api/payments/TEST-PAYMENT-123/retry" "" "Retry Payment (may fail)"

    # Test error cases
    api_call "GET" "$PAYMENT_SERVICE_URL/api/payments/payments/NONEXISTENT/status" "" "Get Status for Non-existent Transaction"
}

test_notification_service() {
    print_header "Notification Service API Tests"

    api_call "GET" "$NOTIFICATION_SERVICE_URL/api/notifications/hello" "" "Notification Service Hello"
    api_call "GET" "$NOTIFICATION_SERVICE_URL/api/notifications/POLICY-123" "" "Get Notifications for Policy"

    # Send notification
    notification_data='{
        "customerId": "CUST-001",
        "type": "PAYMENT_REMINDER",
        "message": "Your payment is due soon"
    }'
    api_call "POST" "$NOTIFICATION_SERVICE_URL/api/notifications/send" "$notification_data" "Send Notification"
}

test_actuator_endpoints() {
    print_header "Actuator Endpoints Tests"

    api_call "GET" "$POLICY_SERVICE_URL/actuator/health" "" "Policy Service Health"
    api_call "GET" "$POLICY_SERVICE_URL/actuator/info" "" "Policy Service Info"
    api_call "GET" "$BILLING_SERVICE_URL/actuator/health" "" "Billing Service Health"
    api_call "GET" "$BILLING_SERVICE_URL/actuator/info" "" "Billing Service Info"
    api_call "GET" "$PAYMENT_SERVICE_URL/actuator/health" "" "Payment Service Health"
    api_call "GET" "$PAYMENT_SERVICE_URL/actuator/info" "" "Payment Service Info"
    api_call "GET" "$NOTIFICATION_SERVICE_URL/actuator/health" "" "Notification Service Health"
    api_call "GET" "$NOTIFICATION_SERVICE_URL/actuator/info" "" "Notification Service Info"
}

# Handle specific service testing
if [ "$1" = "--service" ] && [ -n "$2" ]; then
    case "$2" in
        "policy")
            check_service_health "Policy Service" "$POLICY_SERVICE_URL"
            test_policy_service
            ;;
        "billing")
            check_service_health "Billing Service" "$BILLING_SERVICE_URL"
            test_billing_service
            ;;
        "payment")
            check_service_health "Payment Service" "$PAYMENT_SERVICE_URL"
            test_payment_service
            ;;
        "notification")
            check_service_health "Notification Service" "$NOTIFICATION_SERVICE_URL"
            test_notification_service
            ;;
        *)
            print_error "Unknown service: $2"
            echo "Valid services: policy, billing, payment, notification"
            exit 1
            ;;
    esac
    exit 0
fi

# Handle health-only option
if [ "$1" = "--health-only" ]; then
    print_header "Service Health Checks Only"
    check_service_health "Policy Service" "$POLICY_SERVICE_URL"
    check_service_health "Billing Service" "$BILLING_SERVICE_URL"
    check_service_health "Payment Service" "$PAYMENT_SERVICE_URL"
    check_service_health "Notification Service" "$NOTIFICATION_SERVICE_URL"
    exit 0
fi

# Run the main test suite
main