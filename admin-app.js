// Admin Dashboard JavaScript
class AdminDashboard {
    constructor() {
        this.apiBaseUrl = 'http://localhost:8080/api/v1';
        this.currentView = 'dashboard';
        this.charts = {};
        this.policies = [];
        this.filteredPolicies = [];
        
        this.init();
    }

    init() {
        this.setupEventListeners();
        this.loadDashboardData();
        this.initializeCharts();
        this.showLoadingOverlay();
        
        // Hide loading overlay after initial load
        setTimeout(() => this.hideLoadingOverlay(), 2000);
    }

    setupEventListeners() {
        // Navigation menu
        document.querySelectorAll('.menu-item').forEach(item => {
            item.addEventListener('click', (e) => {
                e.preventDefault();
                const view = item.dataset.view;
                this.switchView(view);
            });
        });

        // Sidebar toggle for mobile
        const sidebarToggle = document.querySelector('.sidebar-toggle');
        if (sidebarToggle) {
            sidebarToggle.addEventListener('click', () => {
                document.querySelector('.sidebar').classList.toggle('open');
            });
        }

        // Global search
        const globalSearch = document.getElementById('global-search');
        if (globalSearch) {
            globalSearch.addEventListener('input', (e) => {
                this.handleGlobalSearch(e.target.value);
            });
        }

        // Chart period selector
        const volumePeriod = document.getElementById('volume-period');
        if (volumePeriod) {
            volumePeriod.addEventListener('change', (e) => {
                this.updatePaymentVolumeChart(e.target.value);
            });
        }

        // Policy filters
        const policySearch = document.getElementById('policy-search');
        if (policySearch) {
            policySearch.addEventListener('input', (e) => {
                this.filterPolicies();
            });
        }

        const statusFilter = document.getElementById('policy-status-filter');
        if (statusFilter) {
            statusFilter.addEventListener('change', (e) => {
                this.filterPolicies();
            });
        }

        const typeFilter = document.getElementById('policy-type-filter');
        if (typeFilter) {
            typeFilter.addEventListener('change', (e) => {
                this.filterPolicies();
            });
        }
    }

    switchView(viewName) {
        // Update active menu item
        document.querySelectorAll('.menu-item').forEach(item => {
            item.classList.remove('active');
        });
        document.querySelector(`[data-view="${viewName}"]`).classList.add('active');

        // Update page title
        const titleMap = {
            dashboard: 'Dashboard',
            policies: 'Policy Management',
            payments: 'Payment Management',
            delinquent: 'Delinquent Accounts',
            analytics: 'Analytics & Reports',
            settings: 'System Settings'
        };
        document.getElementById('page-title').textContent = titleMap[viewName];

        // Show/hide views
        document.querySelectorAll('.view').forEach(view => {
            view.classList.remove('active');
        });
        document.getElementById(`${viewName}-view`).classList.add('active');

        this.currentView = viewName;

        // Load view-specific data
        this.loadViewData(viewName);
    }

    async loadViewData(viewName) {
        switch (viewName) {
            case 'dashboard':
                await this.loadDashboardData();
                break;
            case 'policies':
                await this.loadPoliciesData();
                break;
            case 'payments':
                await this.loadPaymentsData();
                break;
            case 'delinquent':
                await this.loadDelinquentData();
                break;
        }
    }

    async loadDashboardData() {
        try {
            // Load key metrics
            await this.loadKeyMetrics();
            
            // Load recent activities
            await this.loadRecentActivities();
            
            // Update last sync time
            document.getElementById('last-sync').textContent = new Date().toLocaleTimeString();
        } catch (error) {
            console.error('Error loading dashboard data:', error);
            this.showErrorMessage('Failed to load dashboard data');
        }
    }

    async loadKeyMetrics() {
        try {
            // Try to load real data from APIs, fall back to mock data
            const metrics = await this.fetchWithFallback('/billing/policies/stats', {
                totalPolicies: 2847,
                outstandingAmount: 485673.50,
                successRate: 87.3,
                delinquentCount: 156
            });

            // Update metric values with animation
            this.animateValue('total-policies', 0, metrics.totalPolicies, 1500, (value) => 
                value.toLocaleString()
            );
            
            this.animateValue('outstanding-amount', 0, metrics.outstandingAmount, 1500, (value) => 
                '$' + value.toLocaleString('en-US', { minimumFractionDigits: 2 })
            );
            
            this.animateValue('success-rate', 0, metrics.successRate, 1500, (value) => 
                value.toFixed(1) + '%'
            );
            
            this.animateValue('delinquent-count', 0, metrics.delinquentCount, 1500, (value) => 
                value.toLocaleString()
            );
        } catch (error) {
            console.error('Error loading metrics:', error);
        }
    }

    async loadRecentActivities() {
        try {
            // Try to load real activity data, fall back to mock
            const activities = await this.fetchWithFallback('/billing/activities/recent', [
                {
                    type: 'payment',
                    title: 'Payment Received',
                    description: 'POLICY-12345 - $156.00',
                    time: '2 minutes ago',
                    icon: 'payment'
                },
                {
                    type: 'failed',
                    title: 'Payment Failed',
                    description: 'POLICY-67890 - Insufficient funds',
                    time: '15 minutes ago',
                    icon: 'failed'
                },
                {
                    type: 'reminder',
                    title: 'Payment Reminder Sent',
                    description: 'POLICY-24680 - Due in 3 days',
                    time: '1 hour ago',
                    icon: 'reminder'
                },
                {
                    type: 'payment',
                    title: 'Payment Received',
                    description: 'POLICY-13579 - $289.50',
                    time: '2 hours ago',
                    icon: 'payment'
                }
            ]);

            const activitiesHtml = activities.map(activity => `
                <div class="activity-item">
                    <div class="activity-icon ${activity.icon}">
                        ${this.getActivityIcon(activity.icon)}
                    </div>
                    <div class="activity-content">
                        <div class="activity-title">${activity.title}</div>
                        <div class="activity-description">${activity.description}</div>
                    </div>
                    <div class="activity-time">${activity.time}</div>
                </div>
            `).join('');

            document.getElementById('recent-activities').innerHTML = activitiesHtml;
        } catch (error) {
            console.error('Error loading activities:', error);
        }
    }

    async loadPoliciesData() {
        try {
            // Try to load real policies data, fall back to mock
            this.policies = await this.fetchWithFallback('/billing/policies', [
                {
                    policyId: 'POLICY-12345',
                    customerName: 'John Doe',
                    type: 'Auto Insurance',
                    premium: 156.00,
                    dueDate: '2024-12-15',
                    status: 'overdue',
                    customerId: 'CUST-001'
                },
                {
                    policyId: 'POLICY-67890',
                    customerName: 'Jane Smith',
                    type: 'Home Insurance',
                    premium: 289.50,
                    dueDate: '2024-12-20',
                    status: 'current',
                    customerId: 'CUST-002'
                },
                {
                    policyId: 'POLICY-24680',
                    customerName: 'Bob Johnson',
                    type: 'Life Insurance',
                    premium: 125.75,
                    dueDate: '2024-12-18',
                    status: 'grace',
                    customerId: 'CUST-003'
                },
                {
                    policyId: 'POLICY-13579',
                    customerName: 'Alice Brown',
                    type: 'Auto Insurance',
                    premium: 178.25,
                    dueDate: '2024-12-22',
                    status: 'current',
                    customerId: 'CUST-004'
                },
                {
                    policyId: 'POLICY-97531',
                    customerName: 'Charlie Wilson',
                    type: 'Home Insurance',
                    premium: 312.00,
                    dueDate: '2024-12-10',
                    status: 'overdue',
                    customerId: 'CUST-005'
                }
            ]);

            this.filteredPolicies = [...this.policies];
            this.renderPoliciesTable(this.filteredPolicies);
        } catch (error) {
            console.error('Error loading policies:', error);
            this.showErrorMessage('Failed to load policies data');
        }
    }

    async loadPaymentsData() {
        // Placeholder for payments data loading
        console.log('Loading payments data...');
    }

    async loadDelinquentData() {
        // Placeholder for delinquent data loading
        console.log('Loading delinquent data...');
    }

    filterPolicies() {
        const searchTerm = document.getElementById('policy-search')?.value.toLowerCase() || '';
        const statusFilter = document.getElementById('policy-status-filter')?.value || '';
        const typeFilter = document.getElementById('policy-type-filter')?.value || '';

        this.filteredPolicies = this.policies.filter(policy => {
            const matchesSearch = !searchTerm || 
                policy.policyId.toLowerCase().includes(searchTerm) ||
                policy.customerName.toLowerCase().includes(searchTerm);
            
            const matchesStatus = !statusFilter || policy.status === statusFilter;
            const matchesType = !typeFilter || policy.type.toLowerCase().replace(' ', '_') === typeFilter;

            return matchesSearch && matchesStatus && matchesType;
        });

        this.renderPoliciesTable(this.filteredPolicies);
    }

    renderPoliciesTable(policies) {
        const tableBody = document.getElementById('policies-table-body');
        if (!tableBody) return;

        const tableHtml = policies.map(policy => `
            <tr>
                <td>
                    <a href="#" class="policy-link" data-policy-id="${policy.policyId}">
                        ${policy.policyId}
                    </a>
                </td>
                <td>${policy.customerName}</td>
                <td>${policy.type}</td>
                <td>$${policy.premium.toFixed(2)}</td>
                <td>${this.formatDate(policy.dueDate)}</td>
                <td>
                    <span class="status-badge ${policy.status}">
                        ${policy.status}
                    </span>
                </td>
                <td>
                    <div class="table-actions">
                        <button class="btn btn-secondary btn-sm" onclick="adminDashboard.viewPolicy('${policy.policyId}')">
                            View
                        </button>
                        <button class="btn btn-primary btn-sm" onclick="adminDashboard.sendReminder('${policy.policyId}')">
                            Remind
                        </button>
                    </div>
                </td>
            </tr>
        `).join('');

        tableBody.innerHTML = tableHtml;

        // Add click handlers for policy links
        document.querySelectorAll('.policy-link').forEach(link => {
            link.addEventListener('click', (e) => {
                e.preventDefault();
                this.viewPolicy(e.target.dataset.policyId);
            });
        });
    }

    initializeCharts() {
        // Payment Volume Chart
        const volumeCtx = document.getElementById('payment-volume-chart');
        if (volumeCtx) {
            this.charts.paymentVolume = new Chart(volumeCtx, {
                type: 'line',
                data: {
                    labels: this.generateDateLabels(30),
                    datasets: [{
                        label: 'Payment Volume',
                        data: this.generateMockVolumeData(30),
                        borderColor: '#2563eb',
                        backgroundColor: 'rgba(37, 99, 235, 0.1)',
                        borderWidth: 2,
                        fill: true,
                        tension: 0.4
                    }]
                },
                options: {
                    responsive: true,
                    maintainAspectRatio: false,
                    plugins: {
                        legend: {
                            display: false
                        }
                    },
                    scales: {
                        y: {
                            beginAtZero: true,
                            grid: {
                                color: 'rgba(0, 0, 0, 0.05)'
                            }
                        },
                        x: {
                            grid: {
                                display: false
                            }
                        }
                    }
                }
            });
        }

        // Payment Methods Chart
        const methodsCtx = document.getElementById('payment-methods-chart');
        if (methodsCtx) {
            this.charts.paymentMethods = new Chart(methodsCtx, {
                type: 'doughnut',
                data: {
                    labels: ['Credit Card', 'Bank Transfer', 'Auto Pay', 'Check'],
                    datasets: [{
                        data: [45, 25, 20, 10],
                        backgroundColor: [
                            '#2563eb',
                            '#059669',
                            '#d97706',
                            '#dc2626'
                        ],
                        borderWidth: 0
                    }]
                },
                options: {
                    responsive: true,
                    maintainAspectRatio: false,
                    plugins: {
                        legend: {
                            position: 'bottom',
                            labels: {
                                padding: 20,
                                usePointStyle: true
                            }
                        }
                    }
                }
            });
        }
    }

    updatePaymentVolumeChart(period) {
        if (!this.charts.paymentVolume) return;

        const days = period === '7d' ? 7 : period === '30d' ? 30 : 90;
        
        this.charts.paymentVolume.data.labels = this.generateDateLabels(days);
        this.charts.paymentVolume.data.datasets[0].data = this.generateMockVolumeData(days);
        this.charts.paymentVolume.update();
    }

    generateDateLabels(days) {
        const labels = [];
        for (let i = days - 1; i >= 0; i--) {
            const date = new Date();
            date.setDate(date.getDate() - i);
            labels.push(date.toLocaleDateString('en-US', { month: 'short', day: 'numeric' }));
        }
        return labels;
    }

    generateMockVolumeData(days) {
        const data = [];
        for (let i = 0; i < days; i++) {
            data.push(Math.floor(Math.random() * 100) + 20);
        }
        return data;
    }

    // Utility Methods
    async fetchWithFallback(endpoint, fallbackData) {
        try {
            const response = await fetch(`${this.apiBaseUrl}${endpoint}`);
            if (response.ok) {
                return await response.json();
            }
            throw new Error(`API call failed: ${response.status}`);
        } catch (error) {
            console.warn(`Using fallback data for ${endpoint}:`, error.message);
            return fallbackData;
        }
    }

    animateValue(elementId, start, end, duration, formatter = (value) => value) {
        const element = document.getElementById(elementId);
        if (!element) return;

        const startTime = performance.now();
        const animate = (currentTime) => {
            const elapsed = currentTime - startTime;
            const progress = Math.min(elapsed / duration, 1);
            
            const easeOutQuart = 1 - Math.pow(1 - progress, 4);
            const currentValue = start + (end - start) * easeOutQuart;
            
            element.textContent = formatter(currentValue);
            
            if (progress < 1) {
                requestAnimationFrame(animate);
            }
        };
        
        requestAnimationFrame(animate);
    }

    getActivityIcon(type) {
        const icons = {
            payment: '<svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M20 6L9 17l-5-5"/></svg>',
            failed: '<svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>',
            reminder: '<svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9"/><path d="M13.73 21a2 2 0 0 1-3.46 0"/></svg>'
        };
        return icons[type] || icons.payment;
    }

    formatDate(dateString) {
        const date = new Date(dateString);
        return date.toLocaleDateString('en-US', { 
            year: 'numeric', 
            month: 'short', 
            day: 'numeric' 
        });
    }

    showLoadingOverlay() {
        const overlay = document.getElementById('loading-overlay');
        if (overlay) {
            overlay.classList.remove('hidden');
        }
    }

    hideLoadingOverlay() {
        const overlay = document.getElementById('loading-overlay');
        if (overlay) {
            overlay.classList.add('hidden');
        }
    }

    showErrorMessage(message) {
        // Create a simple error notification
        const errorDiv = document.createElement('div');
        errorDiv.className = 'alert alert-error';
        errorDiv.innerHTML = `
            <svg class="alert-icon" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <circle cx="12" cy="12" r="10"/>
                <line x1="12" y1="8" x2="12" y2="12"/>
                <line x1="12" y1="16" x2="12.01" y2="16"/>
            </svg>
            <div><strong>Error:</strong> ${message}</div>
        `;
        
        const content = document.querySelector('.content');
        if (content) {
            content.insertBefore(errorDiv, content.firstChild);
            
            // Remove after 5 seconds
            setTimeout(() => {
                errorDiv.remove();
            }, 5000);
        }
    }

    handleGlobalSearch(query) {
        // Global search functionality
        console.log('Searching for:', query);
        // TODO: Implement global search across all entities
    }

    // Policy Actions
    viewPolicy(policyId) {
        console.log('Viewing policy:', policyId);
        // TODO: Open policy detail modal or navigate to detail view
        alert(`Viewing policy: ${policyId}\n\nThis would open a detailed view of the policy with billing history, payment timeline, and management options.`);
    }

    sendReminder(policyId) {
        console.log('Sending reminder for policy:', policyId);
        // TODO: Send payment reminder
        alert(`Payment reminder sent for policy: ${policyId}\n\nThe customer will receive an email and SMS notification about their upcoming or overdue payment.`);
    }
}

// Additional CSS for error alerts and button styles
const additionalStyles = `
.alert-error {
    background-color: rgba(220, 38, 38, 0.1);
    border-color: var(--danger-color);
    color: var(--danger-color);
}

.table-actions {
    display: flex;
    gap: 0.5rem;
}

.btn-sm {
    padding: 0.25rem 0.5rem;
    font-size: 12px;
}

.policy-link {
    color: var(--primary-color);
    text-decoration: none;
    font-weight: 500;
}

.policy-link:hover {
    text-decoration: underline;
}
`;

// Add additional styles to the document
if (typeof document !== 'undefined') {
    const styleSheet = document.createElement('style');
    styleSheet.textContent = additionalStyles;
    document.head.appendChild(styleSheet);
}

// Initialize the dashboard when the DOM is loaded
let adminDashboard;
if (typeof document !== 'undefined') {
    document.addEventListener('DOMContentLoaded', () => {
        adminDashboard = new AdminDashboard();
    });
}