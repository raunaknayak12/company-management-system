/**
 * Company Management System (CMS) — Main Application Frontend Engine
 */

const CMS = {
  TOKEN_KEY: 'cms_jwt_token',
  USER_KEY: 'cms_user_info',

  getToken() {
    return localStorage.getItem(this.TOKEN_KEY);
  },

  getUser() {
    const raw = localStorage.getItem(this.USER_KEY);
    try {
      return raw ? JSON.parse(raw) : null;
    } catch (e) {
      return null;
    }
  },

  setAuth(token, user) {
    localStorage.setItem(this.TOKEN_KEY, token);
    localStorage.setItem(this.USER_KEY, JSON.stringify(user));
  },

  clearAuth() {
    localStorage.removeItem(this.TOKEN_KEY);
    localStorage.removeItem(this.USER_KEY);
  },

  isAuthenticated() {
    return !!this.getToken();
  },

  requireAuth() {
    if (!this.isAuthenticated()) {
      if (!window.location.pathname.endsWith('login.html')) {
        window.location.href = '/login.html';
      }
    }
  },

  logout() {
    this.clearAuth();
    window.location.href = '/login.html';
  },

  async fetchApi(url, options = {}) {
    const token = this.getToken();
    const headers = {
      'Content-Type': 'application/json',
      ...(options.headers || {})
    };

    if (token) {
      headers['Authorization'] = `Bearer ${token}`;
    }

    try {
      const response = await fetch(url, { ...options, headers });

      if (response.status === 401) {
        this.clearAuth();
        if (!window.location.pathname.endsWith('login.html')) {
          window.location.href = '/login.html?expired=1';
        }
        throw new Error('Session expired. Please log in again.');
      }

      if (response.status === 204) {
        return null;
      }

      const contentType = response.headers.get('content-type');
      let data = null;
      if (contentType && contentType.includes('application/json')) {
        data = await response.json();
      } else {
        data = await response.text();
      }

      if (!response.ok) {
        const errorMsg = (data && data.message) ? data.message : `Request failed with status ${response.status}`;
        throw new Error(errorMsg);
      }

      return data;
    } catch (err) {
      console.error(`API Error on ${url}:`, err);
      throw err;
    }
  },

  showToast(message, type = 'info') {
    let container = document.getElementById('toastContainer');
    if (!container) {
      container = document.createElement('div');
      container.id = 'toastContainer';
      container.className = 'toast-container';
      document.body.appendChild(container);
    }

    const toast = document.createElement('div');
    toast.className = `toast toast-${type}`;
    toast.innerHTML = `<span>${message}</span><button style="background:transparent;border:none;color:white;cursor:pointer;margin-left:12px;" onclick="this.parentElement.remove()">&times;</button>`;
    container.appendChild(toast);

    setTimeout(() => {
      toast.style.opacity = '0';
      toast.style.transform = 'translateY(10px)';
      toast.style.transition = 'all 0.3s ease';
      setTimeout(() => toast.remove(), 300);
    }, 4000);
  },

  formatCurrency(amount) {
    if (amount === null || amount === undefined) return '₹0.00';
    return new Intl.NumberFormat('en-IN', { style: 'currency', currency: 'INR', maximumFractionDigits: 2 }).format(amount);
  },

  formatDate(dateStr) {
    if (!dateStr) return '-';
    return new Date(dateStr).toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric'
    });
  },

  getStatusBadge(status) {
    const s = (status || '').toUpperCase();
    const map = {
      'INQUIRY': '<span class="badge badge-inquiry">Inquiry</span>',
      'QUOTED': '<span class="badge badge-quoted">Quoted</span>',
      'CONFIRMED': '<span class="badge badge-confirmed">Confirmed</span>',
      'PAID': '<span class="badge badge-paid">Paid</span>',
      'DELIVERED': '<span class="badge badge-delivered">Delivered</span>',
      'CANCELLED': '<span class="badge badge-cancelled">Cancelled</span>',
      'ACTIVE': '<span class="badge badge-active">Active</span>',
      'INACTIVE': '<span class="badge badge-inactive">Inactive</span>',
      'PLANNED': '<span class="badge badge-quoted">Planned</span>',
      'COMPLETED': '<span class="badge badge-delivered">Completed</span>',
      'POSITIVE': '<span class="badge badge-positive">Positive</span>',
      'NEUTRAL': '<span class="badge badge-neutral">Neutral</span>',
      'NEGATIVE': '<span class="badge badge-negative">Negative</span>'
    };
    return map[s] || `<span class="badge badge-neutral">${s}</span>`;
  },

  initSidebar() {
    const user = this.getUser();
    if (!user) return;

    // Display user profile in footer
    const nameEl = document.getElementById('sidebarUserName');
    const roleEl = document.getElementById('sidebarUserRole');
    const avatarEl = document.getElementById('sidebarUserAvatar');

    if (nameEl) nameEl.textContent = user.fullName || user.email;
    if (roleEl) roleEl.textContent = user.role;
    if (avatarEl) {
      const initial = (user.fullName || user.email || 'U')[0].toUpperCase();
      avatarEl.textContent = initial;
    }

    // Role-based navigation visibility
    const role = user.role;
    document.querySelectorAll('[data-role]').forEach(el => {
      const allowedRoles = el.getAttribute('data-role').split(',');
      if (!allowedRoles.includes(role)) {
        el.style.display = 'none';
      }
    });

    // Highlight active link
    const currentPath = window.location.pathname.split('/').pop() || 'index.html';
    document.querySelectorAll('.nav-link').forEach(link => {
      const href = link.getAttribute('href');
      if (href === currentPath || (currentPath === '' && href === 'index.html')) {
        link.classList.add('active');
      } else {
        link.classList.remove('active');
      }
    });
  }
};

document.addEventListener('DOMContentLoaded', () => {
  if (!window.location.pathname.endsWith('login.html')) {
    CMS.requireAuth();
    CMS.initSidebar();
  }
});
