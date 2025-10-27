// Cost Sharing JavaScript
class CostSharingManager {
    constructor() {
        this.groups = [];
        this.vehicles = [];
        this.costSplits = [];
        this.currentSplit = null;
        
        this.init();
    }

    async init() {
        await this.loadGroups();
        await this.loadVehicles();
        await this.loadCostSplits();
        this.setupEventListeners();
        this.updateStats();
    }

    async loadGroups() {
        try {
            const response = await fetch('/api/groups');
            this.groups = await response.json();
            this.populateGroupSelect();
        } catch (error) {
            console.error('Error loading groups:', error);
        }
    }

    async loadVehicles() {
        try {
            // This would typically come from a vehicle service
            // For now, we'll use mock data
            this.vehicles = [
                { id: 1, name: 'Tesla Model 3', plateNumber: '30A-12345' },
                { id: 2, name: 'BMW i3', plateNumber: '30B-67890' }
            ];
            this.populateVehicleSelect();
        } catch (error) {
            console.error('Error loading vehicles:', error);
        }
    }

    async loadCostSplits() {
        try {
            const response = await fetch('/api/cost-splits');
            this.costSplits = await response.json();
            this.renderCostSplits();
            this.updateStats();
        } catch (error) {
            console.error('Error loading cost splits:', error);
        }
    }

    populateGroupSelect() {
        const groupSelect = document.getElementById('groupId');
        const filterGroupSelect = document.getElementById('filterGroup');
        
        groupSelect.innerHTML = '<option value="">Chọn nhóm sở hữu</option>';
        filterGroupSelect.innerHTML = '<option value="">Tất cả nhóm</option>';
        
        this.groups.forEach(group => {
            const option = document.createElement('option');
            option.value = group.groupId;
            option.textContent = group.groupName;
            groupSelect.appendChild(option);
            
            const filterOption = document.createElement('option');
            filterOption.value = group.groupId;
            filterOption.textContent = group.groupName;
            filterGroupSelect.appendChild(filterOption);
        });
    }

    populateVehicleSelect() {
        const vehicleSelect = document.getElementById('vehicleId');
        vehicleSelect.innerHTML = '<option value="">Chọn xe</option>';
        
        this.vehicles.forEach(vehicle => {
            const option = document.createElement('option');
            option.value = vehicle.id;
            option.textContent = `${vehicle.name} (${vehicle.plateNumber})`;
            vehicleSelect.appendChild(option);
        });
    }

    setupEventListeners() {
        // Form submission
        document.getElementById('costSharingForm').addEventListener('submit', (e) => {
            e.preventDefault();
            this.handleFormSubmit();
        });

        // Group change - update vehicles
        document.getElementById('groupId').addEventListener('change', (e) => {
            this.updateVehiclesForGroup(e.target.value);
        });

        // Split method change
        document.getElementById('splitMethod').addEventListener('change', (e) => {
            this.handleSplitMethodChange(e.target.value);
        });

        // Filter changes
        document.getElementById('filterStatus').addEventListener('change', (e) => {
            this.filterCostSplits();
        });

        document.getElementById('filterGroup').addEventListener('change', (e) => {
            this.filterCostSplits();
        });

        // Payment form
        document.getElementById('paymentForm').addEventListener('submit', (e) => {
            e.preventDefault();
            this.handlePaymentSubmit();
        });
    }

    async updateVehiclesForGroup(groupId) {
        if (!groupId) return;
        
        try {
            // In a real app, this would fetch vehicles for the specific group
            const response = await fetch(`/api/groups/${groupId}/vehicles`);
            const groupVehicles = await response.json();
            
            const vehicleSelect = document.getElementById('vehicleId');
            vehicleSelect.innerHTML = '<option value="">Chọn xe</option>';
            
            groupVehicles.forEach(vehicle => {
                const option = document.createElement('option');
                option.value = vehicle.id;
                option.textContent = `${vehicle.name} (${vehicle.plateNumber})`;
                vehicleSelect.appendChild(option);
            });
        } catch (error) {
            console.error('Error loading vehicles for group:', error);
        }
    }

    handleSplitMethodChange(method) {
        const customSplitDiv = document.getElementById('customSplitDiv');
        if (method === 'CUSTOM' && !customSplitDiv) {
            this.createCustomSplitInterface();
        } else if (method !== 'CUSTOM' && customSplitDiv) {
            customSplitDiv.remove();
        }
    }

    createCustomSplitInterface() {
        const form = document.getElementById('costSharingForm');
        const customDiv = document.createElement('div');
        customDiv.id = 'customSplitDiv';
        customDiv.innerHTML = `
            <div class="form-group">
                <label>Phân chia tùy chỉnh</label>
                <div id="customSplitMembers">
                    <!-- Custom split members will be added here -->
                </div>
                <button type="button" class="btn btn-outline" onclick="costSharingManager.addCustomSplitMember()">
                    <i class="fas fa-plus"></i>
                    Thêm thành viên
                </button>
            </div>
        `;
        
        form.insertBefore(customDiv, form.querySelector('.form-actions'));
        this.loadGroupMembers();
    }

    async loadGroupMembers() {
        const groupId = document.getElementById('groupId').value;
        if (!groupId) return;

        try {
            const response = await fetch(`/api/groups/${groupId}/members`);
            const members = await response.json();
            
            const customSplitMembers = document.getElementById('customSplitMembers');
            customSplitMembers.innerHTML = '';
            
            members.forEach(member => {
                const memberDiv = document.createElement('div');
                memberDiv.className = 'custom-split-member';
                memberDiv.innerHTML = `
                    <div class="member-info">
                        <span>${member.userName || `User ${member.userId}`}</span>
                    </div>
                    <div class="member-inputs">
                        <input type="number" placeholder="%" min="0" max="100" 
                               data-user-id="${member.userId}" class="split-percentage">
                        <input type="number" placeholder="Số tiền" 
                               data-user-id="${member.userId}" class="split-amount">
                    </div>
                `;
                customSplitMembers.appendChild(memberDiv);
            });
        } catch (error) {
            console.error('Error loading group members:', error);
        }
    }

    addCustomSplitMember() {
        // This would allow adding additional members not in the group
        console.log('Add custom split member functionality');
    }

    async previewSplit() {
        const formData = this.getFormData();
        if (!this.validateForm(formData)) return;

        try {
            const response = await fetch('/api/cost-splits/preview', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(formData)
            });

            const preview = await response.json();
            this.showSplitPreview(preview);
        } catch (error) {
            console.error('Error previewing split:', error);
            this.showError('Không thể xem trước chia sẻ. Vui lòng thử lại.');
        }
    }

    showSplitPreview(preview) {
        const previewDiv = document.getElementById('splitPreview');
        const contentDiv = document.getElementById('splitPreviewContent');
        
        contentDiv.innerHTML = `
            <div class="split-preview-header">
                <h4>Xem trước chia sẻ</h4>
                <p>Tổng chi phí: ${this.formatCurrency(preview.totalAmount)}</p>
            </div>
            <div class="split-preview-list">
                ${preview.splits.map(split => `
                    <div class="split-preview-item">
                        <span>${split.userName}</span>
                        <strong>${this.formatCurrency(split.amount)}</strong>
                    </div>
                `).join('')}
            </div>
        `;
        
        previewDiv.style.display = 'block';
    }

    async handleFormSubmit() {
        const formData = this.getFormData();
        if (!this.validateForm(formData)) return;

        try {
            const response = await fetch('/api/cost-splits', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(formData)
            });

            if (response.ok) {
                this.showSuccess('Chi phí đã được tạo và chia sẻ thành công!');
                document.getElementById('costSharingForm').reset();
                document.getElementById('splitPreview').style.display = 'none';
                await this.loadCostSplits();
            } else {
                throw new Error('Failed to create cost split');
            }
        } catch (error) {
            console.error('Error creating cost split:', error);
            this.showError('Không thể tạo chi phí. Vui lòng thử lại.');
        }
    }

    getFormData() {
        const form = document.getElementById('costSharingForm');
        const formData = new FormData(form);
        
        return {
            groupId: formData.get('groupId'),
            vehicleId: formData.get('vehicleId'),
            costType: formData.get('costType'),
            splitMethod: formData.get('splitMethod'),
            amount: parseFloat(formData.get('amount')),
            description: formData.get('description'),
            invoiceNumber: formData.get('invoiceNumber'),
            receiptUrl: formData.get('receiptUrl')
        };
    }

    validateForm(data) {
        if (!data.groupId) {
            this.showError('Vui lòng chọn nhóm sở hữu');
            return false;
        }
        if (!data.vehicleId) {
            this.showError('Vui lòng chọn xe');
            return false;
        }
        if (!data.costType) {
            this.showError('Vui lòng chọn loại chi phí');
            return false;
        }
        if (!data.amount || data.amount <= 0) {
            this.showError('Vui lòng nhập số tiền hợp lệ');
            return false;
        }
        return true;
    }

    renderCostSplits() {
        const tbody = document.getElementById('splitsTableBody');
        tbody.innerHTML = '';

        this.costSplits.forEach(split => {
            const row = document.createElement('tr');
            row.innerHTML = `
                <td>
                    <div class="cost-info">
                        <div class="cost-title">${split.costDescription}</div>
                        <div class="cost-type">${this.getCostTypeLabel(split.costType)}</div>
                    </div>
                </td>
                <td>${split.groupName}</td>
                <td>${split.userName}</td>
                <td>${this.formatCurrency(split.splitAmount)}</td>
                <td>
                    <span class="status-badge status-${split.status.toLowerCase()}">
                        ${this.getStatusLabel(split.status)}
                    </span>
                </td>
                <td>${this.formatDate(split.createdAt)}</td>
                <td>
                    <div class="action-buttons">
                        ${split.status === 'PENDING' ? `
                            <button class="btn btn-sm btn-primary" onclick="costSharingManager.openPaymentModal(${split.id}, ${split.splitAmount})">
                                <i class="fas fa-credit-card"></i>
                                Thanh toán
                            </button>
                        ` : ''}
                        <button class="btn btn-sm btn-outline" onclick="costSharingManager.viewSplitDetails(${split.id})">
                            <i class="fas fa-eye"></i>
                            Xem
                        </button>
                    </div>
                </td>
            `;
            tbody.appendChild(row);
        });
    }

    filterCostSplits() {
        const statusFilter = document.getElementById('filterStatus').value;
        const groupFilter = document.getElementById('filterGroup').value;
        
        // This would filter the cost splits based on the selected filters
        console.log('Filtering by status:', statusFilter, 'group:', groupFilter);
    }

    openPaymentModal(splitId, amount) {
        this.currentSplit = splitId;
        document.getElementById('paymentAmount').value = amount;
        document.getElementById('paymentModal').style.display = 'block';
    }

    closePaymentModal() {
        document.getElementById('paymentModal').style.display = 'none';
        this.currentSplit = null;
    }

    async handlePaymentSubmit() {
        const formData = new FormData(document.getElementById('paymentForm'));
        
        const paymentData = {
            splitId: this.currentSplit,
            method: formData.get('paymentMethod'),
            amount: parseFloat(formData.get('paymentAmount')),
            transactionCode: formData.get('transactionCode')
        };

        try {
            const response = await fetch('/api/payments', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(paymentData)
            });

            if (response.ok) {
                this.showSuccess('Thanh toán thành công!');
                this.closePaymentModal();
                await this.loadCostSplits();
            } else {
                throw new Error('Payment failed');
            }
        } catch (error) {
            console.error('Error processing payment:', error);
            this.showError('Không thể xử lý thanh toán. Vui lòng thử lại.');
        }
    }

    viewSplitDetails(splitId) {
        // This would open a modal or navigate to details page
        console.log('View split details for:', splitId);
    }

    updateStats() {
        const totalCosts = this.costSplits.reduce((sum, split) => sum + split.splitAmount, 0);
        const totalShares = this.costSplits.length;
        const totalPaid = this.costSplits
            .filter(split => split.status === 'PAID')
            .reduce((sum, split) => sum + split.splitAmount, 0);
        const pendingPayments = this.costSplits.filter(split => split.status === 'PENDING').length;

        document.getElementById('totalCosts').textContent = this.formatCurrency(totalCosts);
        document.getElementById('totalShares').textContent = totalShares;
        document.getElementById('totalPaid').textContent = this.formatCurrency(totalPaid);
        document.getElementById('pendingPayments').textContent = pendingPayments;
    }

    async generateReport() {
        try {
            const response = await fetch('/api/reports/cost-sharing', {
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
                a.download = 'cost-sharing-report.pdf';
                document.body.appendChild(a);
                a.click();
                window.URL.revokeObjectURL(url);
                document.body.removeChild(a);
            }
        } catch (error) {
            console.error('Error generating report:', error);
            this.showError('Không thể tạo báo cáo. Vui lòng thử lại.');
        }
    }

    // Utility functions
    formatCurrency(amount) {
        return new Intl.NumberFormat('vi-VN', {
            style: 'currency',
            currency: 'VND'
        }).format(amount);
    }

    formatDate(dateString) {
        return new Date(dateString).toLocaleDateString('vi-VN');
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

    getStatusLabel(status) {
        const labels = {
            'PENDING': 'Chờ thanh toán',
            'PAID': 'Đã thanh toán',
            'OVERDUE': 'Quá hạn',
            'WAIVED': 'Miễn phí'
        };
        return labels[status] || status;
    }

    showSuccess(message) {
        // You would implement a proper notification system here
        alert(message);
    }

    showError(message) {
        // You would implement a proper error notification system here
        alert(message);
    }
}

// Global functions for HTML onclick handlers
function openAddCostModal() {
    // This would open a modal for adding costs
    console.log('Open add cost modal');
}

function previewSplit() {
    costSharingManager.previewSplit();
}

function closePaymentModal() {
    costSharingManager.closePaymentModal();
}

function generateReport() {
    costSharingManager.generateReport();
}

// Initialize the cost sharing manager when the page loads
let costSharingManager;
document.addEventListener('DOMContentLoaded', () => {
    costSharingManager = new CostSharingManager();
});