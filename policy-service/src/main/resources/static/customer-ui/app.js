// Customer UI JavaScript for API integration
const API_BASE = window.location.origin;

// Utility functions
function showLoading(elementId) {
    const element = document.getElementById(elementId);
    if (element) {
        element.innerHTML = '<div class="loading">Loading...</div>';
    }
}

function showError(elementId, message) {
    const element = document.getElementById(elementId);
    if (element) {
        element.innerHTML = `<div class="alert alert-danger">${message}</div>`;
    }
}

function formatCurrency(amount) {
    return new Intl.NumberFormat('en-US', {
        style: 'currency',
        currency: 'USD'
    }).format(amount);
}

function formatDate(dateString) {
    return new Date(dateString).toLocaleDateString('en-US', {
        year: 'numeric',
        month: 'short',
        day: 'numeric'
    });
}

// API Functions
async function fetchPolicies(customerId = 'CUST-001') {
    try {
        const response = await fetch(`${API_BASE}/api/policies/customer/${customerId}`);
        if (!response.ok) throw new Error('Failed to fetch policies');
        return await response.json();
    } catch (error) {
        console.error('Error fetching policies:', error);
        throw error;
    }
}

async function fetchPolicyDetails(policyId) {
    try {
        const response = await fetch(`${API_BASE}/api/policies/${policyId}`);
        if (!response.ok) throw new Error('Failed to fetch policy details');
        return await response.json();
    } catch (error) {
        console.error('Error fetching policy details:', error);
        throw error;
    }
}

async function fetchPremiumSchedule(policyId) {
    try {
        const response = await fetch(`${API_BASE}/api/policies/${policyId}/premium-schedule`);
        if (!response.ok) throw new Error('Failed to fetch premium schedule');
        return await response.json();
    } catch (error) {
        console.error('Error fetching premium schedule:', error);
        throw error;
    }
}

async function processPayment(paymentData) {
    try {
        const response = await fetch(`${API_BASE}/api/payments/process`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(paymentData)
        });
        if (!response.ok) throw new Error('Payment processing failed');
        return await response.json();
    } catch (error) {
        console.error('Error processing payment:', error);
        throw error;
    }
}

async function fetchPaymentHistory(filters = {}) {
    try {
        const params = new URLSearchParams(filters);
        const response = await fetch(`${API_BASE}/api/payments/history?${params}`);
        if (!response.ok) throw new Error('Failed to fetch payment history');
        return await response.json();
    } catch (error) {
        console.error('Error fetching payment history:', error);
        throw error;
    }
}

async function retryPayment(transactionId) {
    try {
        const response = await fetch(`${API_BASE}/api/payments/${transactionId}/retry`, {
            method: 'POST'
        });
        if (!response.ok) throw new Error('Failed to retry payment');
        return await response.json();
    } catch (error) {
        console.error('Error retrying payment:', error);
        throw error;
    }
}

// UI Rendering Functions
function renderPolicyCard(policy) {
    const statusClass = policy.status === 'OVERDUE' ? 'overdue' : 
                       policy.status === 'ACTIVE' ? 'current' : 'grace-period';
    
    const statusText = policy.status === 'OVERDUE' ? 'Overdue' :
                      policy.status === 'ACTIVE' ? 'Current' : 'Grace Period';

    return `
        <div class="policy-card ${statusClass}">
            <div class="policy-header">
                <div>
                    <div class="policy-type">${policy.policyType.replace('_', ' ')}</div>
                    <div class="policy-number">${policy.policyNumber}</div>
                </div>
                <div class="status ${statusClass}">${statusText}</div>
            </div>
            <div class="policy-details">
                <div class="policy-detail">
                    <span>Premium:</span>
                    <strong>${formatCurrency(policy.premiumAmount)}/month</strong>
                </div>
                <div class="policy-detail">
                    <span>Next Due:</span>
                    <strong>${formatDate(policy.nextDueDate)}</strong>
                </div>
                ${policy.status === 'OVERDUE' ? `
                <div class="policy-detail">
                    <span>Days Overdue:</span>
                    <strong style="color: #e74c3c;">3 days</strong>
                </div>
                <div class="policy-detail">
                    <span>Late Fee:</span>
                    <strong>${formatCurrency(15)}</strong>
                </div>` : ''}
            </div>
            <div class="btn-group">
                <button class="btn ${policy.status === 'OVERDUE' ? 'btn-danger' : 'btn-success'}" 
                        onclick="goToPayment('${policy.policyId}')">
                    Pay Now${policy.status === 'OVERDUE' ? ' (' + formatCurrency(policy.premiumAmount + 15) + ')' : ''}
                </button>
                <button class="btn btn-secondary" onclick="viewPolicyDetails('${policy.policyId}')">
                    View Details
                </button>
            </div>
        </div>
    `;
}

function renderPaymentHistoryRow(payment) {
    const statusClass = payment.status === 'SUCCESS' ? 'current' : 
                       payment.status === 'FAILED' ? 'overdue' : 'grace-period';
    
    return `
        <tr>
            <td>${formatDate(payment.timestamp)}<br><small class="text-muted">${new Date(payment.timestamp).toLocaleTimeString()}</small></td>
            <td>${payment.transactionId}</td>
            <td>
                <strong>${payment.policyId}</strong><br>
                <small>Auto Insurance</small>
            </td>
            <td><strong>${formatCurrency(payment.amount)}</strong></td>
            <td>${payment.paymentMethod}</td>
            <td><span class="status ${statusClass}">${payment.status}</span></td>
            <td>
                ${payment.status === 'FAILED' ? 
                    `<button class="btn btn-danger" onclick="retryFailedPayment('${payment.transactionId}')">Retry</button>` :
                    `<button class="btn btn-secondary" onclick="viewReceipt('${payment.transactionId}')">Receipt</button>`
                }
            </td>
        </tr>
    `;
}

// Navigation Functions
function goToPayment(policyId) {
    window.location.href = `payment.html?policy=${policyId}`;
}

function viewPolicyDetails(policyId) {
    window.location.href = `policy-details.html?id=${policyId}`;
}

function viewReceipt(transactionId) {
    // For now, just show an alert - in real implementation would open receipt
    alert(`Viewing receipt for transaction: ${transactionId}`);
}

async function retryFailedPayment(transactionId) {
    try {
        showLoading('payment-result');
        const result = await retryPayment(transactionId);
        alert(`Payment retry initiated: ${result.newTransactionId}`);
        // Refresh the page to show updated data
        location.reload();
    } catch (error) {
        alert('Failed to retry payment: ' + error.message);
    }
}

// Page-specific initialization functions
async function initDashboard() {
    try {
        showLoading('policies-container');
        const policies = await fetchPolicies();
        
        const policiesHtml = policies.map(renderPolicyCard).join('');
        document.getElementById('policies-container').innerHTML = policiesHtml;
        
        // Update dashboard stats
        const overduePolicies = policies.filter(p => p.status === 'OVERDUE');
        const totalPremium = policies.reduce((sum, p) => sum + parseFloat(p.premiumAmount), 0);
        const totalOverdue = overduePolicies.reduce((sum, p) => sum + parseFloat(p.premiumAmount) + 15, 0);
        
        document.getElementById('active-policies').textContent = policies.length;
        document.getElementById('monthly-premium').textContent = formatCurrency(totalPremium);
        document.getElementById('overdue-count').textContent = overduePolicies.length;
        document.getElementById('amount-due').textContent = formatCurrency(totalOverdue);
        
    } catch (error) {
        showError('policies-container', 'Failed to load dashboard data');
    }
}

async function initPaymentHistory() {
    try {
        showLoading('payment-history-table');
        const payments = await fetchPaymentHistory();
        
        const tableBody = payments.map(renderPaymentHistoryRow).join('');
        document.getElementById('payment-history-table').innerHTML = `
            <table class="table">
                <thead>
                    <tr>
                        <th>Date</th>
                        <th>Transaction ID</th>
                        <th>Policy</th>
                        <th>Amount</th>
                        <th>Method</th>
                        <th>Status</th>
                        <th>Actions</th>
                    </tr>
                </thead>
                <tbody>
                    ${tableBody}
                </tbody>
            </table>
        `;
        
    } catch (error) {
        showError('payment-history-table', 'Failed to load payment history');
    }
}

async function initPaymentForm() {
    const urlParams = new URLSearchParams(window.location.search);
    const policyId = urlParams.get('policy');
    
    if (policyId) {
        try {
            const schedule = await fetchPremiumSchedule(policyId);
            
            // Update payment summary
            document.getElementById('policy-id').textContent = policyId;
            document.getElementById('premium-amount').textContent = formatCurrency(schedule.premiumAmount);
            document.getElementById('late-fee').textContent = formatCurrency(schedule.lateFee);
            document.getElementById('total-due').textContent = formatCurrency(schedule.totalAmountDue);
            
            // Set form data
            document.getElementById('payment-policy-id').value = policyId;
            document.getElementById('payment-amount').value = schedule.totalAmountDue;
            
        } catch (error) {
            showError('payment-summary', 'Failed to load payment details');
        }
    }
}

async function submitPayment(event) {
    event.preventDefault();
    
    const formData = new FormData(event.target);
    const paymentData = {
        policyId: formData.get('policyId'),
        amount: parseFloat(formData.get('amount')),
        paymentMethod: {
            type: formData.get('paymentMethod'),
            // Add other payment method details as needed
        }
    };
    
    try {
        showLoading('payment-result');
        const result = await processPayment(paymentData);
        
        if (result.status === 'SUCCESS') {
            window.location.href = `payment-success.html?transaction=${result.transactionId}`;
        } else {
            document.getElementById('payment-result').innerHTML = `
                <div class="alert alert-danger">
                    <strong>Payment Failed:</strong> ${result.message}
                    <br>Error Code: ${result.errorCode}
                    ${result.retrySchedule ? `<br>Next retry: ${formatDate(result.retrySchedule.nextRetryDate)}` : ''}
                </div>
            `;
        }
    } catch (error) {
        showError('payment-result', 'Payment processing failed: ' + error.message);
    }
}

// Initialize page based on current page
document.addEventListener('DOMContentLoaded', function() {
    const page = window.location.pathname.split('/').pop();
    
    switch (page) {
        case 'dashboard.html':
        case 'index.html':
        case '':
            initDashboard();
            break;
        case 'payments.html':
            initPaymentHistory();
            break;
        case 'payment.html':
            initPaymentForm();
            break;
    }
});
