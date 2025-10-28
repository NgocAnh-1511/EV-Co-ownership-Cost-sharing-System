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
    const searchInput = document.getElementById('groupSearch');
    if (searchInput) {
        searchInput.addEventListener('input', debounce(handleSearch, 300));
    }
    
    // Filter functionality
    const filterSelect = document.getElementById('statusFilter');
    if (filterSelect) {
        filterSelect.addEventListener('change', handleFilter);
    }
    
    // Create group button
    const createGroupBtn = document.querySelector('.btn-primary');
    if (createGroupBtn) {
        createGroupBtn.addEventListener('click', handleCreateGroup);
    }
    
    // Group action buttons
    setupGroupActionButtons();
}

function handleSearch(event) {
    const searchTerm = event.target.value.toLowerCase();
    const groupCards = document.querySelectorAll('.group-card');
    
    groupCards.forEach(card => {
        const groupName = card.querySelector('.group-name').textContent.toLowerCase();
        const groupId = card.querySelector('.group-id').textContent.toLowerCase();
        
        const isVisible = groupName.includes(searchTerm) || groupId.includes(searchTerm);
        
        card.style.display = isVisible ? 'block' : 'none';
    });
    
    updateSearchResults();
}

function handleFilter(event) {
    const filterValue = event.target.value;
    const groupCards = document.querySelectorAll('.group-card');
    
    groupCards.forEach(card => {
        const statusBadge = card.querySelector('.status-badge');
        const status = card.dataset.groupStatus;
        
        let isVisible = true;
        
        if (filterValue && filterValue !== '') {
            isVisible = status === filterValue;
        }
        
        card.style.display = isVisible ? 'block' : 'none';
    });
    
    updateSearchResults();
}

function handleCreateGroup() {
    console.log('Create group clicked');
    // Redirect to create group page
    window.location.href = '/groups/create';
}

function setupGroupActionButtons() {
    const actionButtons = document.querySelectorAll('.action-btn');
    
    actionButtons.forEach(button => {
        button.addEventListener('click', function(e) {
            if (this.classList.contains('delete')) {
                e.preventDefault();
                const groupCard = this.closest('.group-card');
                const groupName = groupCard.querySelector('.group-name').textContent;
                const groupId = groupCard.querySelector('.group-id span').textContent;
                handleDeleteGroup(groupName, groupId, groupCard);
            }
        });
    });
}

function handleDeleteGroup(groupName, groupId, groupCard) {
    if (confirm(`Bạn có chắc chắn muốn xóa nhóm "${groupName}"?`)) {
        console.log('Delete group:', groupName, groupId);
        
        // Show loading state
        groupCard.style.opacity = '0.5';
        groupCard.style.pointerEvents = 'none';
        
        // Simulate API call
        setTimeout(() => {
            groupCard.remove();
            updateSearchResults();
            showNotification('Nhóm đã được xóa thành công!', 'success');
        }, 1000);
    }
}

function updateSearchResults() {
    const visibleCards = document.querySelectorAll('.group-card[style*="block"], .group-card:not([style*="none"])');
    const totalCards = document.querySelectorAll('.group-card');
    
    console.log(`Hiển thị ${visibleCards.length} trong tổng số ${totalCards.length} nhóm`);
}

function loadGroupData() {
    console.log('Loading group data...');
    
    // Show loading state
    showLoadingState();
    
    // Simulate API call
    setTimeout(() => {
        hideLoadingState();
        updateSummaryCards();
    }, 1000);
}

function updateSummaryCards() {
    // Update summary cards with actual data
    const cards = document.querySelectorAll('.summary-card .card-number');
    
    if (cards.length >= 4) {
        const totalGroups = document.querySelectorAll('.group-card').length;
        const activeGroups = document.querySelectorAll('.group-card[data-group-status="Active"]').length;
        
        cards[0].textContent = totalGroups;
        cards[1].textContent = activeGroups;
        // Other cards would be updated with real data from API
    }
}

function addLoadingStates() {
    // Add loading states to interactive elements
    const buttons = document.querySelectorAll('button, .btn');
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
    const content = document.querySelector('.content-area');
    if (content) {
        content.style.opacity = '0.7';
        content.style.pointerEvents = 'none';
    }
}

function hideLoadingState() {
    const content = document.querySelector('.content-area');
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

// Global function for delete group (called from HTML)
function deleteGroup(groupId) {
    const groupCard = document.querySelector(`[data-group-id="${groupId}"]`);
    if (groupCard) {
        const groupName = groupCard.querySelector('.group-name').textContent;
        handleDeleteGroup(groupName, groupId, groupCard);
    }
}

// Export functions for testing or external use
window.GroupManagement = {
    handleSearch,
    handleFilter,
    handleCreateGroup,
    showNotification,
    updateSummaryCards,
    deleteGroup
};
