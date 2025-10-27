// Group Management JavaScript
document.addEventListener('DOMContentLoaded', function() {
    // Initialize the application
    initializeApp();
    
    // Set up event listeners
    setupEventListeners();
    
    // Load initial data
    loadGroupData();
});

function initializeApp() {
    console.log('Group Management App initialized');
    
    // Add loading states
    addLoadingStates();
    
    // Initialize tooltips
    initializeTooltips();
}

function setupEventListeners() {
    // Search functionality
    const searchInput = document.querySelector('.search-input');
    if (searchInput) {
        searchInput.addEventListener('input', handleSearch);
    }
    
    // Filter functionality
    const filterSelect = document.querySelector('.filter-select');
    if (filterSelect) {
        filterSelect.addEventListener('change', handleFilter);
    }
    
    // Create group button
    const createGroupBtn = document.querySelector('.btn-create-group');
    if (createGroupBtn) {
        createGroupBtn.addEventListener('click', handleCreateGroup);
    }
    
    // Group action buttons
    setupGroupActionButtons();
    
    // Pagination
    setupPagination();
    
    // Navigation
    setupNavigation();
}

function handleSearch(event) {
    const searchTerm = event.target.value.toLowerCase();
    const groupCards = document.querySelectorAll('.group-card');
    
    groupCards.forEach(card => {
        const groupName = card.querySelector('.group-name').textContent.toLowerCase();
        const members = card.querySelector('.members-label').textContent.toLowerCase();
        const cars = card.querySelector('.cars-label').textContent.toLowerCase();
        
        const isVisible = groupName.includes(searchTerm) || 
                         members.includes(searchTerm) || 
                         cars.includes(searchTerm);
        
        card.style.display = isVisible ? 'block' : 'none';
    });
    
    updatePaginationInfo();
}

function handleFilter(event) {
    const filterValue = event.target.value;
    const groupCards = document.querySelectorAll('.group-card');
    
    groupCards.forEach(card => {
        const statusBadge = card.querySelector('.status-badge');
        const status = statusBadge.textContent.toLowerCase();
        
        let isVisible = true;
        
        switch(filterValue) {
            case 'Hoạt động':
                isVisible = status.includes('hoạt động');
                break;
            case 'Tạm dừng':
                isVisible = status.includes('tạm dừng');
                break;
            case 'Đã kết thúc':
                isVisible = status.includes('kết thúc');
                break;
            default:
                isVisible = true;
        }
        
        card.style.display = isVisible ? 'block' : 'none';
    });
    
    updatePaginationInfo();
}

function handleCreateGroup() {
    // Show create group modal or redirect
    console.log('Create group clicked');
    
    // For now, show an alert
    alert('Tính năng tạo nhóm mới sẽ được triển khai sớm!');
    
    // In a real application, you would:
    // 1. Show a modal with a form
    // 2. Handle form submission
    // 3. Send data to backend
    // 4. Refresh the group list
}

function setupGroupActionButtons() {
    const actionButtons = document.querySelectorAll('.action-btn');
    
    actionButtons.forEach(button => {
        button.addEventListener('click', function(e) {
            e.preventDefault();
            
            const action = this.classList.contains('edit') ? 'edit' :
                          this.classList.contains('members') ? 'members' :
                          this.classList.contains('cars') ? 'cars' :
                          this.classList.contains('delete') ? 'delete' : 'unknown';
            
            const groupCard = this.closest('.group-card');
            const groupName = groupCard.querySelector('.group-name').textContent;
            
            handleGroupAction(action, groupName, groupCard);
        });
    });
}

function handleGroupAction(action, groupName, groupCard) {
    switch(action) {
        case 'edit':
            handleEditGroup(groupName, groupCard);
            break;
        case 'members':
            handleManageMembers(groupName, groupCard);
            break;
        case 'cars':
            handleManageCars(groupName, groupCard);
            break;
        case 'delete':
            handleDeleteGroup(groupName, groupCard);
            break;
        default:
            console.log('Unknown action:', action);
    }
}

function handleEditGroup(groupName, groupCard) {
    console.log('Edit group:', groupName);
    
    // Show edit modal or redirect to edit page
    alert(`Chỉnh sửa nhóm: ${groupName}`);
    
    // In a real application:
    // 1. Show edit modal with current group data
    // 2. Handle form submission
    // 3. Update group data via API
    // 4. Refresh the group card
}

function handleManageMembers(groupName, groupCard) {
    console.log('Manage members for group:', groupName);
    
    // Show members management modal
    alert(`Quản lý thành viên nhóm: ${groupName}`);
    
    // In a real application:
    // 1. Show modal with member list
    // 2. Allow adding/removing members
    // 3. Update member list via API
    // 4. Refresh the group card
}

function handleManageCars(groupName, groupCard) {
    console.log('Manage cars for group:', groupName);
    
    // Show cars management modal
    alert(`Quản lý xe nhóm: ${groupName}`);
    
    // In a real application:
    // 1. Show modal with car list
    // 2. Allow adding/removing cars
    // 3. Update car list via API
    // 4. Refresh the group card
}

function handleDeleteGroup(groupName, groupCard) {
    if (confirm(`Bạn có chắc chắn muốn xóa nhóm "${groupName}"?`)) {
        console.log('Delete group:', groupName);
        
        // Show loading state
        groupCard.style.opacity = '0.5';
        groupCard.style.pointerEvents = 'none';
        
        // Simulate API call
        setTimeout(() => {
            groupCard.remove();
            updatePaginationInfo();
            showNotification('Nhóm đã được xóa thành công!', 'success');
        }, 1000);
        
        // In a real application:
        // 1. Send DELETE request to API
        // 2. Handle success/error responses
        // 3. Update UI accordingly
    }
}

function setupPagination() {
    const paginationButtons = document.querySelectorAll('.pagination-btn');
    
    paginationButtons.forEach(button => {
        button.addEventListener('click', function(e) {
            e.preventDefault();
            
            if (this.disabled) return;
            
            // Remove active class from all buttons
            paginationButtons.forEach(btn => btn.classList.remove('active'));
            
            // Add active class to clicked button (if it's a number)
            if (!isNaN(this.textContent)) {
                this.classList.add('active');
            }
            
            // Handle pagination logic
            handlePagination(this.textContent);
        });
    });
}

function handlePagination(page) {
    console.log('Navigate to page:', page);
    
    // In a real application:
    // 1. Calculate offset and limit
    // 2. Fetch data from API
    // 3. Update the group cards
    // 4. Update pagination info
    
    // For now, just show a message
    if (page !== 'Trước' && page !== 'Sau') {
        showNotification(`Đang tải trang ${page}...`, 'info');
    }
}

function setupNavigation() {
    const navLinks = document.querySelectorAll('.nav-link');
    
    navLinks.forEach(link => {
        link.addEventListener('click', function(e) {
            e.preventDefault();
            
            // Remove active class from all nav items
            document.querySelectorAll('.nav-item').forEach(item => {
                item.classList.remove('active');
            });
            
            // Add active class to clicked item
            this.closest('.nav-item').classList.add('active');
            
            // Handle navigation
            const navText = this.querySelector('span').textContent;
            handleNavigation(navText);
        });
    });
}

function handleNavigation(navText) {
    console.log('Navigate to:', navText);
    
    // In a real application, you would:
    // 1. Update the URL
    // 2. Load the appropriate content
    // 3. Update the page title
    
    switch(navText) {
        case 'Dashboard':
            showNotification('Chuyển đến Dashboard', 'info');
            break;
        case 'Xe của tôi':
            showNotification('Chuyển đến Xe của tôi', 'info');
            break;
        case 'Nhóm sở hữu':
            // Already on this page
            break;
        case 'Lịch đặt xe':
            showNotification('Chuyển đến Lịch đặt xe', 'info');
            break;
        case 'Báo cáo':
            showNotification('Chuyển đến Báo cáo', 'info');
            break;
        case 'Cài đặt':
            showNotification('Chuyển đến Cài đặt', 'info');
            break;
    }
}

function loadGroupData() {
    // Simulate loading data from API
    console.log('Loading group data...');
    
    // Show loading state
    showLoadingState();
    
    // Simulate API call
    setTimeout(() => {
        hideLoadingState();
        updateSummaryCards();
        updatePaginationInfo();
    }, 1000);
}

function updateSummaryCards() {
    // In a real application, update with actual data from API
    const cards = document.querySelectorAll('.summary-card .card-number');
    
    // Simulate data updates
    const newData = {
        totalGroups: 12,
        totalMembers: 48,
        totalCars: 25,
        activeGroups: 10
    };
    
    if (cards.length >= 4) {
        cards[0].textContent = newData.totalGroups;
        cards[1].textContent = newData.totalMembers;
        cards[2].textContent = newData.totalCars;
        cards[3].textContent = newData.activeGroups;
    }
}

function updatePaginationInfo() {
    const visibleCards = document.querySelectorAll('.group-card[style*="block"], .group-card:not([style*="none"])');
    const totalCards = document.querySelectorAll('.group-card');
    
    const paginationInfo = document.querySelector('.pagination-info');
    if (paginationInfo) {
        paginationInfo.textContent = `Hiển thị 1-${visibleCards.length} trong tổng số ${totalCards.length} nhóm`;
    }
}

function addLoadingStates() {
    // Add loading states to interactive elements
    const buttons = document.querySelectorAll('button');
    buttons.forEach(button => {
        button.addEventListener('click', function() {
            if (!this.disabled) {
                this.style.opacity = '0.7';
                this.style.pointerEvents = 'none';
                
                setTimeout(() => {
                    this.style.opacity = '1';
                    this.style.pointerEvents = 'auto';
                }, 1000);
            }
        });
    });
}

function initializeTooltips() {
    // Add tooltips to action buttons
    const actionButtons = document.querySelectorAll('.action-btn');
    actionButtons.forEach(button => {
        button.addEventListener('mouseenter', function() {
            const title = this.getAttribute('title');
            if (title) {
                showTooltip(this, title);
            }
        });
        
        button.addEventListener('mouseleave', function() {
            hideTooltip();
        });
    });
}

function showTooltip(element, text) {
    const tooltip = document.createElement('div');
    tooltip.className = 'tooltip';
    tooltip.textContent = text;
    tooltip.style.cssText = `
        position: absolute;
        background: #1f2937;
        color: white;
        padding: 0.5rem 0.75rem;
        border-radius: 0.375rem;
        font-size: 0.75rem;
        z-index: 1000;
        pointer-events: none;
        white-space: nowrap;
    `;
    
    document.body.appendChild(tooltip);
    
    const rect = element.getBoundingClientRect();
    tooltip.style.left = rect.left + (rect.width / 2) - (tooltip.offsetWidth / 2) + 'px';
    tooltip.style.top = rect.top - tooltip.offsetHeight - 8 + 'px';
}

function hideTooltip() {
    const tooltip = document.querySelector('.tooltip');
    if (tooltip) {
        tooltip.remove();
    }
}

function showLoadingState() {
    const content = document.querySelector('.content');
    if (content) {
        content.style.opacity = '0.7';
        content.style.pointerEvents = 'none';
    }
}

function hideLoadingState() {
    const content = document.querySelector('.content');
    if (content) {
        content.style.opacity = '1';
        content.style.pointerEvents = 'auto';
    }
}

function showNotification(message, type = 'info') {
    const notification = document.createElement('div');
    notification.className = `notification notification-${type}`;
    notification.textContent = message;
    notification.style.cssText = `
        position: fixed;
        top: 20px;
        right: 20px;
        padding: 1rem 1.5rem;
        border-radius: 0.5rem;
        color: white;
        font-weight: 500;
        z-index: 1000;
        transform: translateX(100%);
        transition: transform 0.3s ease;
    `;
    
    // Set background color based on type
    switch(type) {
        case 'success':
            notification.style.background = '#10b981';
            break;
        case 'error':
            notification.style.background = '#ef4444';
            break;
        case 'warning':
            notification.style.background = '#f59e0b';
            break;
        default:
            notification.style.background = '#3b82f6';
    }
    
    document.body.appendChild(notification);
    
    // Animate in
    setTimeout(() => {
        notification.style.transform = 'translateX(0)';
    }, 100);
    
    // Auto remove after 3 seconds
    setTimeout(() => {
        notification.style.transform = 'translateX(100%)';
        setTimeout(() => {
            notification.remove();
        }, 300);
    }, 3000);
}

// Utility functions
function debounce(func, wait) {
    let timeout;
    return function executedFunction(...args) {
        const later = () => {
            clearTimeout(timeout);
            func(...args);
        };
        clearTimeout(timeout);
        timeout = setTimeout(later, wait);
    };
}

function throttle(func, limit) {
    let inThrottle;
    return function() {
        const args = arguments;
        const context = this;
        if (!inThrottle) {
            func.apply(context, args);
            inThrottle = true;
            setTimeout(() => inThrottle = false, limit);
        }
    };
}

// Export functions for testing or external use
window.GroupManagement = {
    handleSearch,
    handleFilter,
    handleCreateGroup,
    showNotification,
    updateSummaryCards,
    updatePaginationInfo
};
