// Reports JavaScript
class ReportsManager {
    constructor() {
        this.currentSection = 'overview';
        this.charts = {};
        
        this.init();
    }

    async init() {
        this.setupEventListeners();
        await this.loadOverviewData();
        this.initializeCharts();
    }

    setupEventListeners() {
        // Report period change
        document.getElementById('costReportPeriod')?.addEventListener('change', (e) => {
            this.handleReportPeriodChange(e.target.value);
        });

        // Custom report form
        document.getElementById('customReportForm')?.addEventListener('submit', (e) => {
            e.preventDefault();
            this.handleCustomReportSubmit();
        });

        // Report period change for custom reports
        document.getElementById('reportPeriod')?.addEventListener('change', (e) => {
            this.handleCustomReportPeriodChange(e.target.value);
        });
    }

    showReportSection(sectionName) {
        // Hide all sections
        document.querySelectorAll('.report-section').forEach(section => {
            section.classList.remove('active');
        });
        
        // Remove active class from all nav buttons
        document.querySelectorAll('.nav-btn').forEach(btn => {
            btn.classList.remove('active');
        });
        
        // Show selected section
        document.getElementById(`${sectionName}Section`).classList.add('active');
        
        // Add active class to clicked button
        event.target.classList.add('active');
        
        this.currentSection = sectionName;
        
        // Load section-specific data
        this.loadSectionData(sectionName);
    }

    async loadSectionData(sectionName) {
        switch (sectionName) {
            case 'overview':
                await this.loadOverviewData();
                break;
            case 'costs':
                await this.loadCostsData();
                break;
            case 'groups':
                await this.loadGroupsData();
                break;
            case 'voting':
                await this.loadVotingData();
                break;
            case 'fund':
                await this.loadFundData();
                break;
        }
    }

    async loadOverviewData() {
        try {
            // Load overview statistics
            const [costsResponse, groupsResponse, votingsResponse, fundResponse] = await Promise.all([
                fetch('/api/costs'),
                fetch('/groups/api/all'),
                fetch('/api/votings'),
                fetch('/api/fund/balances')
            ]);

            const costs = await costsResponse.json();
            const groups = await groupsResponse.json();
            const votings = await votingsResponse.json();
            const fundBalances = await fundResponse.json();

            // Update overview stats
            this.updateOverviewStats(costs, groups, votings, fundBalances);
            
            // Update charts
            this.updateOverviewCharts(costs, groups, votings);
        } catch (error) {
            console.error('Error loading overview data:', error);
        }
    }

    updateOverviewStats(costs, groups, votings, fundBalances) {
        const totalCosts = costs.reduce((sum, cost) => sum + (cost.amount || 0), 0);
        const totalGroups = groups.length;
        const totalVotings = votings.length;
        const totalFundBalance = fundBalances.total || 0;

        document.getElementById('totalCosts').textContent = this.formatCurrency(totalCosts);
        document.getElementById('totalGroups').textContent = totalGroups;
        document.getElementById('totalVotings').textContent = totalVotings;
        document.getElementById('totalFundBalance').textContent = this.formatCurrency(totalFundBalance);
    }

    updateOverviewCharts(costs, groups, votings) {
        this.createCostsByTypeChart(costs);
        this.createGroupActivityChart(groups, votings);
    }

    createCostsByTypeChart(costs) {
        const ctx = document.getElementById('costsByTypeChart');
        if (!ctx) return;

        // Group costs by type
        const costsByType = costs.reduce((acc, cost) => {
            const type = cost.costType || 'Other';
            acc[type] = (acc[type] || 0) + (cost.amount || 0);
            return acc;
        }, {});

        const labels = Object.keys(costsByType);
        const data = Object.values(costsByType);

        if (this.charts.costsByType) {
            this.charts.costsByType.destroy();
        }

        this.charts.costsByType = new Chart(ctx, {
            type: 'doughnut',
            data: {
                labels: labels,
                datasets: [{
                    data: data,
                    backgroundColor: [
                        '#3B82F6',
                        '#10B981',
                        '#F59E0B',
                        '#EF4444',
                        '#8B5CF6',
                        '#06B6D4'
                    ]
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: {
                        position: 'bottom'
                    }
                }
            }
        });
    }

    createGroupActivityChart(groups, votings) {
        const ctx = document.getElementById('groupActivityChart');
        if (!ctx) return;

        // Group activity data (example)
        const activityData = groups.map(group => ({
            name: group.groupName,
            members: group.memberCount || 0,
            votings: votings.filter(v => v.groupId === group.groupId).length
        }));

        const labels = activityData.map(item => item.name);
        const membersData = activityData.map(item => item.members);
        const votingsData = activityData.map(item => item.votings);

        if (this.charts.groupActivity) {
            this.charts.groupActivity.destroy();
        }

        this.charts.groupActivity = new Chart(ctx, {
            type: 'bar',
            data: {
                labels: labels,
                datasets: [{
                    label: 'Thành viên',
                    data: membersData,
                    backgroundColor: '#3B82F6'
                }, {
                    label: 'Cuộc bỏ phiếu',
                    data: votingsData,
                    backgroundColor: '#10B981'
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                scales: {
                    y: {
                        beginAtZero: true
                    }
                }
            }
        });
    }

    async loadCostsData() {
        try {
            const response = await fetch('/api/costs');
            const costs = await response.json();
            
            const content = document.getElementById('costReportContent');
            content.innerHTML = this.generateCostReportHTML(costs);
        } catch (error) {
            console.error('Error loading costs data:', error);
        }
    }

    async loadGroupsData() {
        try {
            const response = await fetch('/groups/api/all');
            const groups = await response.json();
            
            const content = document.getElementById('groupReportContent');
            content.innerHTML = this.generateGroupReportHTML(groups);
        } catch (error) {
            console.error('Error loading groups data:', error);
        }
    }

    async loadVotingData() {
        try {
            const response = await fetch('/api/votings');
            const votings = await response.json();
            
            const content = document.getElementById('votingReportContent');
            content.innerHTML = this.generateVotingReportHTML(votings);
        } catch (error) {
            console.error('Error loading voting data:', error);
        }
    }

    async loadFundData() {
        try {
            const response = await fetch('/api/fund/balances');
            const fundBalances = await response.json();
            
            const content = document.getElementById('fundReportContent');
            content.innerHTML = this.generateFundReportHTML(fundBalances);
        } catch (error) {
            console.error('Error loading fund data:', error);
        }
    }

    generateCostReportHTML(costs) {
        const totalCosts = costs.reduce((sum, cost) => sum + (cost.amount || 0), 0);
        const costsByType = costs.reduce((acc, cost) => {
            const type = cost.costType || 'Other';
            acc[type] = (acc[type] || 0) + (cost.amount || 0);
            return acc;
        }, {});

        return `
            <div class="report-summary">
                <h4>Báo cáo chi phí</h4>
                <div class="report-stats">
                    <div class="report-stat">
                        <span class="stat-label">Tổng chi phí:</span>
                        <span class="stat-value">${this.formatCurrency(totalCosts)}</span>
                    </div>
                    <div class="report-stat">
                        <span class="stat-label">Số lượng:</span>
                        <span class="stat-value">${costs.length}</span>
                    </div>
                </div>
                <div class="costs-breakdown">
                    <h5>Chi phí theo loại:</h5>
                    ${Object.entries(costsByType).map(([type, amount]) => `
                        <div class="cost-item">
                            <span class="cost-type">${this.getCostTypeLabel(type)}</span>
                            <span class="cost-amount">${this.formatCurrency(amount)}</span>
                        </div>
                    `).join('')}
                </div>
            </div>
        `;
    }

    generateGroupReportHTML(groups) {
        const totalGroups = groups.length;
        const totalMembers = groups.reduce((sum, group) => sum + (group.memberCount || 0), 0);

        return `
            <div class="report-summary">
                <h4>Báo cáo nhóm</h4>
                <div class="report-stats">
                    <div class="report-stat">
                        <span class="stat-label">Tổng số nhóm:</span>
                        <span class="stat-value">${totalGroups}</span>
                    </div>
                    <div class="report-stat">
                        <span class="stat-label">Tổng thành viên:</span>
                        <span class="stat-value">${totalMembers}</span>
                    </div>
                </div>
                <div class="groups-list">
                    <h5>Danh sách nhóm:</h5>
                    ${groups.map(group => `
                        <div class="group-item">
                            <span class="group-name">${group.groupName}</span>
                            <span class="group-members">${group.memberCount || 0} thành viên</span>
                        </div>
                    `).join('')}
                </div>
            </div>
        `;
    }

    generateVotingReportHTML(votings) {
        const totalVotings = votings.length;
        const completedVotings = votings.filter(v => v.status === 'COMPLETED').length;
        const activeVotings = votings.filter(v => v.status === 'ACTIVE').length;

        return `
            <div class="report-summary">
                <h4>Báo cáo bỏ phiếu</h4>
                <div class="report-stats">
                    <div class="report-stat">
                        <span class="stat-label">Tổng cuộc bỏ phiếu:</span>
                        <span class="stat-value">${totalVotings}</span>
                    </div>
                    <div class="report-stat">
                        <span class="stat-label">Đã hoàn thành:</span>
                        <span class="stat-value">${completedVotings}</span>
                    </div>
                    <div class="report-stat">
                        <span class="stat-label">Đang diễn ra:</span>
                        <span class="stat-value">${activeVotings}</span>
                    </div>
                </div>
            </div>
        `;
    }

    generateFundReportHTML(fundBalances) {
        return `
            <div class="report-summary">
                <h4>Báo cáo quỹ chung</h4>
                <div class="report-stats">
                    <div class="report-stat">
                        <span class="stat-label">Tổng quỹ:</span>
                        <span class="stat-value">${this.formatCurrency(fundBalances.total || 0)}</span>
                    </div>
                    <div class="report-stat">
                        <span class="stat-label">Quỹ bảo dưỡng:</span>
                        <span class="stat-value">${this.formatCurrency(fundBalances.maintenance || 0)}</span>
                    </div>
                    <div class="report-stat">
                        <span class="stat-label">Phí dự phòng:</span>
                        <span class="stat-value">${this.formatCurrency(fundBalances.reserve || 0)}</span>
                    </div>
                </div>
            </div>
        `;
    }

    async generateCostReport() {
        try {
            const response = await fetch('/api/reports/costs', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                }
            });

            if (response.ok) {
                this.showSuccess('Báo cáo chi phí đã được tạo thành công!');
            }
        } catch (error) {
            console.error('Error generating cost report:', error);
            this.showError('Không thể tạo báo cáo chi phí. Vui lòng thử lại.');
        }
    }

    async generateGroupReport() {
        try {
            const response = await fetch('/api/reports/groups', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                }
            });

            if (response.ok) {
                this.showSuccess('Báo cáo nhóm đã được tạo thành công!');
            }
        } catch (error) {
            console.error('Error generating group report:', error);
            this.showError('Không thể tạo báo cáo nhóm. Vui lòng thử lại.');
        }
    }

    async generateVotingReport() {
        try {
            const response = await fetch('/api/reports/voting', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                }
            });

            if (response.ok) {
                this.showSuccess('Báo cáo bỏ phiếu đã được tạo thành công!');
            }
        } catch (error) {
            console.error('Error generating voting report:', error);
            this.showError('Không thể tạo báo cáo bỏ phiếu. Vui lòng thử lại.');
        }
    }

    async generateFundReport() {
        try {
            const response = await fetch('/api/reports/fund', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                }
            });

            if (response.ok) {
                this.showSuccess('Báo cáo quỹ chung đã được tạo thành công!');
            }
        } catch (error) {
            console.error('Error generating fund report:', error);
            this.showError('Không thể tạo báo cáo quỹ chung. Vui lòng thử lại.');
        }
    }

    generateCustomReport() {
        document.getElementById('customReportModal').style.display = 'block';
    }

    closeCustomReportModal() {
        document.getElementById('customReportModal').style.display = 'none';
        document.getElementById('customReportForm').reset();
    }

    handleCustomReportPeriodChange(period) {
        const customDateRange = document.getElementById('customDateRange');
        if (period === 'custom') {
            customDateRange.style.display = 'block';
        } else {
            customDateRange.style.display = 'none';
        }
    }

    async handleCustomReportSubmit() {
        const formData = new FormData(document.getElementById('customReportForm'));
        const reportData = {
            type: formData.get('reportType'),
            period: formData.get('reportPeriod'),
            format: formData.get('reportFormat'),
            startDate: formData.get('startDate'),
            endDate: formData.get('endDate')
        };

        try {
            const response = await fetch('/api/reports/custom', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(reportData)
            });

            if (response.ok) {
                this.showSuccess('Báo cáo tùy chỉnh đã được tạo thành công!');
                this.closeCustomReportModal();
            }
        } catch (error) {
            console.error('Error generating custom report:', error);
            this.showError('Không thể tạo báo cáo tùy chỉnh. Vui lòng thử lại.');
        }
    }

    async exportAllReports() {
        try {
            const response = await fetch('/api/reports/export-all', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                }
            });

            if (response.ok) {
                const blob = await response.blob();
                const url = window.URL.createObjectURL(blob);
                const a = document.createElement('a');
                a.href = url;
                a.download = 'all-reports.zip';
                document.body.appendChild(a);
                a.click();
                window.URL.revokeObjectURL(url);
                document.body.removeChild(a);
            }
        } catch (error) {
            console.error('Error exporting all reports:', error);
            this.showError('Không thể xuất tất cả báo cáo. Vui lòng thử lại.');
        }
    }

    handleReportPeriodChange(period) {
        console.log('Report period changed to:', period);
        // This would trigger a reload of the cost report data
    }

    initializeCharts() {
        // Initialize any additional charts if needed
    }

    // Utility functions
    formatCurrency(amount) {
        return new Intl.NumberFormat('vi-VN', {
            style: 'currency',
            currency: 'VND'
        }).format(amount);
    }

    getCostTypeLabel(type) {
        const labels = {
            'CHARGING': 'Phí sạc điện',
            'MAINTENANCE': 'Bảo dưỡng',
            'INSURANCE': 'Bảo hiểm',
            'REGISTRATION': 'Đăng kiểm',
            'CLEANING': 'Vệ sinh xe',
            'PARKING': 'Phí đỗ xe',
            'TOLLS': 'Phí cầu đường',
            'REPAIRS': 'Sửa chữa',
            'UPGRADES': 'Nâng cấp',
            'OTHER': 'Khác'
        };
        return labels[type] || type;
    }

    showSuccess(message) {
        alert(message);
    }

    showError(message) {
        alert(message);
    }
}

// Global functions for HTML onclick handlers
function showReportSection(sectionName) {
    reportsManager.showReportSection(sectionName);
}

function generateCostReport() {
    reportsManager.generateCostReport();
}

function generateGroupReport() {
    reportsManager.generateGroupReport();
}

function generateVotingReport() {
    reportsManager.generateVotingReport();
}

function generateFundReport() {
    reportsManager.generateFundReport();
}

function generateCustomReport() {
    reportsManager.generateCustomReport();
}

function closeCustomReportModal() {
    reportsManager.closeCustomReportModal();
}

function exportAllReports() {
    reportsManager.exportAllReports();
}

// Initialize the reports manager when the page loads
let reportsManager;
document.addEventListener('DOMContentLoaded', () => {
    reportsManager = new ReportsManager();
});
