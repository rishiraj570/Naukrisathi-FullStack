/* =====================================================
   api.js – NaukriSaathi Frontend API Helper
   Base URL: http://localhost:8080/api
   Roles: jobseeker | hiringpartner
   ===================================================== */
const API_BASE = "http://localhost:8080/api";

// ── Auth helpers ──────────────────────────────────────
const getToken  = ()            => localStorage.getItem('ns_token');
const getUser   = ()            => { try { return JSON.parse(localStorage.getItem('ns_user')); } catch { return null; } };
const setAuth   = (token, user) => { localStorage.setItem('ns_token', token); localStorage.setItem('ns_user', JSON.stringify(user)); };
const clearAuth = ()            => { localStorage.removeItem('ns_token'); localStorage.removeItem('ns_user'); };
const isLoggedIn= ()            => !!getToken() && !!getUser();

function logout() {
  clearAuth();
  window.location.href = '../signin.html';
}

// ── Core fetch wrapper ────────────────────────────────
async function apiFetch(endpoint, options = {}) {
  const token = getToken();
  const headers = {
    'Content-Type': 'application/json',
    ...(token && { Authorization: `Bearer ${token}` }),
    ...(options.headers || {})
  };
  const response = await fetch(`${API_BASE}${endpoint}`, { ...options, headers });
  const data = await response.json().catch(() => ({}));
  if (!response.ok) throw new Error(data.message || `Error ${response.status}`);
  return data;
}

// ── Route protection helpers ──────────────────────────
function requireLogin(redirectPath = '/signin.html') {
  if (!isLoggedIn()) {
    window.location.href = redirectPath;
    return false;
  }
  return true;
}

function requireRole(role, redirectPath = '/index.html') {
  const user = getUser();
  if (!user || user.role !== role) {
    window.location.href = redirectPath;
    return false;
  }
  return true;
}

// ── Auth API ──────────────────────────────────────────
const AuthAPI = {
  register: (body) => apiFetch('/auth/register', { method: 'POST', body: JSON.stringify(body) }),
  login:    (body) => apiFetch('/auth/login',    { method: 'POST', body: JSON.stringify(body) }),
  me:       ()     => apiFetch('/auth/me'),
  logout:   ()     => apiFetch('/auth/logout',   { method: 'POST' }),
};

// ── Jobs API ──────────────────────────────────────────
const JobAPI = {
  getAll:  (params = {}) => apiFetch('/jobs?' + new URLSearchParams(params)),
  getById: (id)          => apiFetch(`/jobs/${id}`),
  create:  (body)        => apiFetch('/jobs', { method: 'POST', body: JSON.stringify(body) }),
  update:  (id, body)    => apiFetch(`/jobs/${id}`, { method: 'PUT', body: JSON.stringify(body) }),
  delete:  (id)          => apiFetch(`/jobs/${id}`, { method: 'DELETE' }),
};

// ── Applications API ──────────────────────────────────
const ApplicationAPI = {
  apply:      (body)   => apiFetch('/applications', { method: 'POST', body: JSON.stringify(body) }),
  myApps:     ()       => apiFetch('/applications/my'),
  forJob:     (jobId)  => apiFetch(`/applications/job/${jobId}`),
};

// ── Users API ─────────────────────────────────────────
const UserAPI = {
  profile: ()          => apiFetch('/users/profile'),
  update:  (body)      => apiFetch('/users/profile', { method: 'PUT', body: JSON.stringify(body) }),
  saved:   ()          => apiFetch('/users/saved'),
};

// ── Toast helper ──────────────────────────────────────
function showToast(msg, icon = '✅') {
  const toast = document.getElementById('toast');
  if (!toast) return;
  const msgEl  = document.getElementById('toast-msg');
  const iconEl = document.getElementById('toast-icon');
  if (msgEl)  msgEl.textContent  = msg;
  if (iconEl) iconEl.textContent = icon;
  toast.style.transform = 'translateY(0)';
  toast.style.opacity   = '1';
  setTimeout(() => { toast.style.transform = 'translateY(80px)'; toast.style.opacity = '0'; }, 3500);
}
