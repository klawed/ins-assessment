// Customer UI JavaScript for API integration
const API_BASE = '/api';

class PolicyManager {
    constructor() {
        this.customerId = 'CUST-001'; // TODO: Get from auth
        this.init();
    }

    async init() {
        await this.loadPolicies();
        this.setupEventListeners();
    }

    async loadPolicies() {
        try {
            const response = await fetch(`${API_BASE}/policies/customer/${this.customerId}`);
            if (!response.ok) throw new Error('Failed to load policies');
            
            const policies = await response.json();
            this.renderPolicies(policies);
        } catch (error) {
            console.error('Error loading policies:', error);
            this.showError('Unable to load policies. Please try again later.');
        }
    }

    renderPolicies(policies) {
        const container = document.getElementById('policies-list');
        container.innerHTML = policies.map(policy => this.createPolicyCard(policy)).join('');
    }

    createPolicyCard(policy) {
        return `
            <div class="policy-card status-${policy.status.toLowerCase()}" data-policy-id="${policy.policyId}">
                <h3>Policy ${policy.policyNumber}</h3>
                <p>Type: ${policy.policyType}</p>
                <p>Status: ${policy.status}</p>
                <p>Premium: $${policy.premiumAmount}</p>
                <button onclick="policyManager.viewPremiumSchedule('${policy.policyId}')">
                    View Schedule
                </button>
                <button onclick="policyManager.makePayment('${policy.policyId}')">
                    Make Payment
                </button>
            </div>
        `;
    }

    async viewPremiumSchedule(policyId) {
        try {
            const response = await fetch(`${API_BASE}/policies/${policyId}/premium-schedule`);
            if (!response.ok) throw new Error('Failed to load premium schedule');
            
            const schedule = await response.json();
            this.renderPremiumSchedule(schedule);
        } catch (error) {
            console.error('Error loading premium schedule:', error);
            this.showError('Unable to load premium schedule. Please try again later.');
        }
    }

    makePayment(policyId) {
        // TODO: Implement payment flow
        console.log('Making payment for policy:', policyId);
    }

    setupEventListeners() {
        // TODO: Add event listeners for UI interactions
    }

    showError(message) {
        // TODO: Implement error display
        alert(message);
    }
}

// Initialize the application
const policyManager = new PolicyManager();
