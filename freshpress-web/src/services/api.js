const API_BASE = '/api/v1';

let authToken = localStorage.getItem('freshpress_token');

const headers = () => ({
  'Content-Type': 'application/json',
  ...(authToken ? { Authorization: `Bearer ${authToken}` } : {})
});

export const setToken = (token) => {
  authToken = token;
  localStorage.setItem('freshpress_token', token);
};

export const clearToken = () => {
  authToken = null;
  localStorage.removeItem('freshpress_token');
};

export const getToken = () => authToken;

async function request(path, options = {}) {
  const res = await fetch(`${API_BASE}${path}`, {
    headers: headers(),
    ...options
  });
  if (!res.ok) {
    const err = await res.json().catch(() => ({}));
    let msg = err.message;
    if (err.error === 'Validation Failed' && err.fields) {
      msg = Object.values(err.fields)[0];
    }
    throw new Error(msg || `Request failed: ${res.status}`);
  }
  return res.json();
}

// Auth
export const login = (data) => request('/auth/login', { method: 'POST', body: JSON.stringify(data) });
export const register = (data) => request('/auth/register', { method: 'POST', body: JSON.stringify(data) });

// Dashboard
export const getStats = () => request('/dashboard/stats');

// Customers
export const getCustomers = (page = 0, size = 20, search = '') =>
  request(`/customers?page=${page}&size=${size}${search ? `&search=${search}` : ''}`);
export const createCustomer = (data) => request('/customers', { method: 'POST', body: JSON.stringify(data) });

// Orders
export const getOrders = (page = 0, size = 20, status = '') =>
  request(`/orders?page=${page}&size=${size}${status ? `&status=${status}` : ''}`);
export const createOrder = (data) => request('/orders', { method: 'POST', body: JSON.stringify(data) });
export const updateOrderStatus = (id, status) =>
  request(`/orders/${id}/status`, { method: 'PATCH', body: JSON.stringify({ status }) });

// Services
export const getServices = () => request('/services');
export const createService = (data) => request('/services', { method: 'POST', body: JSON.stringify(data) });
