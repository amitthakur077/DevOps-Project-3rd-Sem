// ============================================================
// IMS Web App — Core Logic (localStorage-backed)
// Mirrors all Java InventoryService business rules in JS
// ============================================================

const STORAGE_KEY = 'ims_inventory';

// ============================================================
// DATA LAYER — mirrors InventoryService.java
// ============================================================

function loadInventory() {
  try {
    const raw = localStorage.getItem(STORAGE_KEY);
    return raw ? JSON.parse(raw) : [];
  } catch { return []; }
}

function saveInventory(products) {
  localStorage.setItem(STORAGE_KEY, JSON.stringify(products));
}

function getProducts() {
  return loadInventory();
}

/** Returns existing product by ID (case-insensitive), or null */
function findById(id) {
  if (!id || !id.trim()) return null;
  return getProducts().find(p => p.id.toUpperCase() === id.trim().toUpperCase()) || null;
}

/** Add product — mirrors InventoryService.addProduct() */
function addProduct(id, name, price, qty, threshold) {
  id    = id.trim();
  name  = name.trim();
  price = parseFloat(price);
  qty   = parseInt(qty, 10);
  threshold = parseInt(threshold, 10);

  if (!id)                   return { ok: false, msg: 'Product ID cannot be empty.' };
  if (!name)                 return { ok: false, msg: 'Product Name cannot be empty.' };
  if (isNaN(price)||price<0) return { ok: false, msg: 'Price must be a non-negative number.' };
  if (isNaN(qty)||qty<0)     return { ok: false, msg: 'Quantity must be a non-negative integer.' };
  if (isNaN(threshold)||threshold<0) return { ok: false, msg: 'Low-stock threshold must be non-negative.' };

  const existing = findById(id);
  if (existing) return { ok: false, msg: `Duplicate ID: A product with ID '${id}' already exists.` };

  const products = getProducts();
  products.push({ id: id.toUpperCase(), name, price, qty, threshold, createdAt: new Date().toISOString() });
  saveInventory(products);
  return { ok: true };
}

/** Update stock — mirrors InventoryService.updateStock() */
function updateStock(id, delta) {
  const products = getProducts();
  const idx = products.findIndex(p => p.id.toUpperCase() === id.trim().toUpperCase());
  if (idx === -1) return { ok: false, msg: `Product '${id}' not found.` };

  const newQty = products[idx].qty + delta;
  if (newQty < 0) return {
    ok: false,
    msg: `Cannot reduce stock by ${Math.abs(delta)}. Current: ${products[idx].qty}. Would result in: ${newQty}.`
  };

  products[idx].qty = newQty;
  saveInventory(products);
  return { ok: true, product: products[idx] };
}

/** Delete product — mirrors InventoryService.deleteProduct() */
function deleteProduct(id) {
  let products = getProducts();
  const before = products.length;
  products = products.filter(p => p.id.toUpperCase() !== id.trim().toUpperCase());
  saveInventory(products);
  return products.length < before;
}

/** Edit product name, price, threshold */
function editProduct(id, name, price, threshold) {
  name      = name.trim();
  price     = parseFloat(price);
  threshold = parseInt(threshold, 10);

  if (!name)                 return { ok: false, msg: 'Name cannot be empty.' };
  if (isNaN(price)||price<0) return { ok: false, msg: 'Price must be non-negative.' };
  if (isNaN(threshold)||threshold<0) return { ok: false, msg: 'Threshold must be non-negative.' };

  const products = getProducts();
  const idx = products.findIndex(p => p.id.toUpperCase() === id.toUpperCase());
  if (idx === -1) return { ok: false, msg: 'Product not found.' };

  products[idx].name      = name;
  products[idx].price     = price;
  products[idx].threshold = threshold;
  saveInventory(products);
  return { ok: true };
}

/** Low-stock filter — mirrors Product.isLowStock() */
function getLowStock() {
  return getProducts().filter(p => p.qty <= p.threshold);
}

/** Search by ID or name substring */
function searchProducts(query) {
  if (!query || !query.trim()) return [];
  const q = query.trim().toLowerCase();
  return getProducts().filter(p =>
    p.id.toLowerCase() === q ||
    p.id.toLowerCase().includes(q) ||
    p.name.toLowerCase().includes(q)
  );
}

function totalValue() {
  return getProducts().reduce((sum, p) => sum + p.price * p.qty, 0);
}

// ============================================================
// SEED DATA (only on first ever load)
// ============================================================

function seedIfEmpty() {
  if (getProducts().length > 0) return;
  const seeds = [
    { id: 'P101', name: 'Dell XPS 15 Laptop', price: 1499.99, qty: 12, threshold: 5 },
    { id: 'P102', name: 'Logitech MX Master 3S Mouse', price: 99.50,  qty: 4,  threshold: 10 },
    { id: 'P103', name: 'Mechanical RGB Keyboard',     price: 79.99,  qty: 25, threshold: 8  },
    { id: 'P104', name: 'USB-C Multiport Hub',         price: 34.50,  qty: 6,  threshold: 15 },
  ];
  seeds.forEach(s => {
    const p = getProducts();
    p.push({ ...s, createdAt: new Date().toISOString() });
    saveInventory(p);
  });
}

// ============================================================
// ROUTING / NAVIGATION
// ============================================================

function navigate(section) {
  document.querySelectorAll('.section').forEach(s => s.classList.remove('active'));
  document.querySelectorAll('.nav-item').forEach(n => n.classList.remove('active'));

  const sec = document.getElementById(`section-${section}`);
  const nav = document.getElementById(`nav-${section}`);
  if (sec) sec.classList.add('active');
  if (nav) nav.classList.add('active');

  const titles = {
    dashboard: ['Dashboard',     'Overview of your inventory'],
    products:  ['All Products',  'View and manage all registered products'],
    add:       ['Add Product',   'Register a new product to inventory'],
    search:    ['Search',        'Search by product ID or name'],
    stock:     ['Update Stock',  'Increase or decrease quantity of a product'],
    alerts:    ['Low-Stock Alerts', 'Products at or below their threshold'],
  };
  const [title, sub] = titles[section] || ['IMS', ''];
  document.getElementById('page-title').textContent    = title;
  document.getElementById('page-subtitle').textContent = sub;

  // Refresh view data
  if (section === 'dashboard') renderDashboard();
  if (section === 'products')  renderProductsTable();
  if (section === 'alerts')    renderAlertsSection();
}

// ============================================================
// CLOCK
// ============================================================

function updateClock() {
  const el = document.getElementById('topbar-time');
  if (el) el.textContent = new Date().toLocaleTimeString('en-IN', { hour: '2-digit', minute: '2-digit', second: '2-digit' });
}

// ============================================================
// TOAST
// ============================================================

function toast(msg, type = 'info') {
  const icons = { success: '✅', error: '❌', warn: '⚠️', info: 'ℹ️' };
  const el = document.createElement('div');
  el.className = `toast toast-${type}`;
  el.innerHTML = `<span>${icons[type]||'ℹ️'}</span><span>${msg}</span>`;
  document.getElementById('toast-container').appendChild(el);
  setTimeout(() => { el.classList.add('toast-out'); setTimeout(() => el.remove(), 300); }, 3500);
}

// ============================================================
// GLOBAL ALERT BADGE
// ============================================================

function refreshAlertBadge() {
  const count = getLowStock().length;
  const badge = document.getElementById('alert-badge');
  if (badge) {
    badge.textContent = count;
    badge.style.display = count > 0 ? 'inline-flex' : 'none';
  }
}

// ============================================================
// HELPERS
// ============================================================

function fmtPrice(val) {
  return `$${parseFloat(val).toFixed(2)}`;
}

function statusBadge(p) {
  const low = p.qty <= p.threshold;
  return low
    ? `<span class="status-badge status-low">⚠️ Low Stock</span>`
    : `<span class="status-badge status-ok">✅ OK</span>`;
}

function actionBtns(id, section = '') {
  return `
    <div class="actions-cell">
      <button class="btn btn-sm btn-edit-sm"  onclick="openEditModal('${id}')">✏️ Edit</button>
      <button class="btn btn-sm btn-stock-sm" onclick="openStockQuick('${id}')">📊 Stock</button>
      <button class="btn btn-sm btn-del-sm"   onclick="confirmDelete('${id}')">🗑</button>
    </div>`;
}

// ============================================================
// DASHBOARD
// ============================================================

function renderDashboard() {
  const all   = getProducts();
  const low   = getLowStock();
  const ok    = all.filter(p => p.qty > p.threshold);
  const total = totalValue();

  document.getElementById('stat-total').textContent = all.length;
  document.getElementById('stat-value').textContent = `$${total.toLocaleString('en-US', { minimumFractionDigits: 2 })}`;
  document.getElementById('stat-low').textContent   = low.length;
  document.getElementById('stat-ok').textContent    = ok.length;

  // Recent products table (last 5)
  const tbody = document.getElementById('dashboard-tbody');
  const recent = [...all].reverse().slice(0, 5);
  tbody.innerHTML = recent.length === 0
    ? `<tr><td colspan="5" style="text-align:center;color:var(--text-muted);padding:24px">No products yet.</td></tr>`
    : recent.map(p => `<tr>
        <td>${p.id}</td>
        <td>${esc(p.name)}</td>
        <td>${fmtPrice(p.price)}</td>
        <td>${p.qty}</td>
        <td>${statusBadge(p)}</td>
      </tr>`).join('');

  // Low-stock alert list
  const list = document.getElementById('dashboard-alerts-list');
  if (low.length === 0) {
    list.innerHTML = `<div class="alert-empty">✅ All stock levels are healthy!</div>`;
  } else {
    list.innerHTML = low.map(p => `
      <div class="alert-item">
        <div class="alert-item-name">${esc(p.name)}</div>
        <div class="alert-item-detail">ID: ${p.id} — Qty: <strong>${p.qty}</strong> / Threshold: ${p.threshold}</div>
      </div>`).join('');
  }

  refreshAlertBadge();
}

// ============================================================
// ALL PRODUCTS TABLE
// ============================================================

function renderProductsTable(filter = '') {
  let products = getProducts();
  if (filter) {
    const q = filter.toLowerCase();
    products = products.filter(p =>
      p.id.toLowerCase().includes(q) || p.name.toLowerCase().includes(q)
    );
  }

  const tbody   = document.getElementById('products-tbody');
  const empty   = document.getElementById('products-empty');

  if (products.length === 0) {
    tbody.innerHTML = '';
    empty.style.display = 'block';
  } else {
    empty.style.display = 'none';
    tbody.innerHTML = products.map(p => `<tr>
      <td>${p.id}</td>
      <td>${esc(p.name)}</td>
      <td>${fmtPrice(p.price)}</td>
      <td>${p.qty}</td>
      <td>${p.threshold}</td>
      <td>${fmtPrice(p.price * p.qty)}</td>
      <td>${statusBadge(p)}</td>
      <td>${actionBtns(p.id)}</td>
    </tr>`).join('');
  }

  refreshAlertBadge();
}

function filterProductTable() {
  const val = document.getElementById('product-filter').value;
  renderProductsTable(val);
}

function clearAllProducts() {
  if (!confirm('⚠️ This will permanently delete ALL products from inventory. Are you sure?')) return;
  localStorage.removeItem(STORAGE_KEY);
  renderProductsTable();
  renderDashboard();
  toast('All products cleared.', 'warn');
}

// ============================================================
// ADD PRODUCT
// ============================================================

function handleAddProduct(e) {
  e.preventDefault();
  const id        = document.getElementById('add-id').value;
  const name      = document.getElementById('add-name').value;
  const price     = document.getElementById('add-price').value;
  const qty       = document.getElementById('add-qty').value;
  const threshold = document.getElementById('add-threshold').value;

  const result = addProduct(id, name, price, qty, threshold);
  if (!result.ok) { toast(result.msg, 'error'); return; }

  toast(`Product '${name}' added successfully!`, 'success');
  resetAddForm();
  refreshAlertBadge();
  renderDashboard();
}

function resetAddForm() {
  document.getElementById('add-product-form').reset();
}

// ============================================================
// SEARCH
// ============================================================

function handleSearch() {
  const query   = document.getElementById('search-input').value;
  const wrapper = document.getElementById('search-results-wrapper');
  const empty   = document.getElementById('search-empty');
  const tbody   = document.getElementById('search-tbody');
  const count   = document.getElementById('search-count');

  if (!query.trim()) {
    wrapper.style.display = 'none';
    empty.style.display   = 'none';
    tbody.innerHTML = '';
    return;
  }

  const results = searchProducts(query);
  if (results.length === 0) {
    wrapper.style.display = 'none';
    empty.style.display   = 'block';
    return;
  }

  empty.style.display   = 'none';
  wrapper.style.display = 'block';
  count.textContent = `${results.length} product(s) found for "${query}"`;
  tbody.innerHTML = results.map(p => `<tr>
    <td>${p.id}</td>
    <td>${esc(p.name)}</td>
    <td>${fmtPrice(p.price)}</td>
    <td>${p.qty}</td>
    <td>${p.threshold}</td>
    <td>${statusBadge(p)}</td>
    <td>${actionBtns(p.id, 'search')}</td>
  </tr>`).join('');
}

// ============================================================
// UPDATE STOCK
// ============================================================

function previewStockProduct() {
  const id = document.getElementById('stock-id').value;
  const preview = document.getElementById('stock-preview');
  const content = document.getElementById('stock-preview-content');
  const p = findById(id);
  if (p) {
    preview.style.display = 'block';
    content.innerHTML = `<strong>${esc(p.name)}</strong> (${p.id}) — Current Stock: <strong>${p.qty}</strong> units ${statusBadge(p)}`;
  } else {
    preview.style.display = 'none';
  }
}

function handleUpdateStock(e) {
  e.preventDefault();
  const id     = document.getElementById('stock-id').value;
  const action = document.getElementById('stock-action').value;
  const qty    = parseInt(document.getElementById('stock-qty').value, 10);

  if (!id.trim())    { toast('Please enter a Product ID.', 'error'); return; }
  if (isNaN(qty)||qty<1) { toast('Quantity must be a positive integer.', 'error'); return; }

  const delta  = action === 'add' ? qty : -qty;
  const result = updateStock(id, delta);

  if (!result.ok) { toast(result.msg, 'error'); return; }

  const p = result.product;
  const verb = action === 'add' ? 'added' : 'deducted';
  toast(`${qty} units ${verb}. New stock for '${esc(p.name)}': ${p.qty} units.`, 'success');

  if (p.qty <= p.threshold) {
    toast(`⚠️ Warning: '${esc(p.name)}' is now at low-stock level (${p.qty} / ${p.threshold})!`, 'warn');
  }

  resetStockForm();
  refreshAlertBadge();
  renderDashboard();
}

function resetStockForm() {
  document.getElementById('stock-form').reset();
  document.getElementById('stock-preview').style.display = 'none';
}

function openStockQuick(id) {
  document.getElementById('stock-id').value = id;
  navigate('stock');
  previewStockProduct();
}

// ============================================================
// LOW-STOCK ALERTS SECTION
// ============================================================

function renderAlertsSection() {
  const low     = getLowStock();
  const tbody   = document.getElementById('alerts-tbody');
  const empty   = document.getElementById('alerts-empty');
  const wrapper = document.getElementById('alerts-table-wrapper');
  const label   = document.getElementById('alerts-count-label');

  label.textContent = `${low.length} item${low.length !== 1 ? 's' : ''}`;

  if (low.length === 0) {
    wrapper.style.display = 'none';
    empty.style.display = 'block';
  } else {
    empty.style.display   = 'none';
    wrapper.style.display = 'block';
    tbody.innerHTML = low.map(p => {
      const deficit = p.threshold - p.qty;
      return `<tr>
        <td>${p.id}</td>
        <td>${esc(p.name)}</td>
        <td>${fmtPrice(p.price)}</td>
        <td style="color:var(--accent-orange);font-weight:700">${p.qty}</td>
        <td>${p.threshold}</td>
        <td><span style="color:var(--accent-red);font-weight:600">-${deficit} units</span></td>
        <td>${actionBtns(p.id)}</td>
      </tr>`;
    }).join('');
  }

  refreshAlertBadge();
}

// ============================================================
// DELETE
// ============================================================

let _pendingDeleteId = null;

function confirmDelete(id) {
  const p = findById(id);
  if (!p) { toast('Product not found.', 'error'); return; }

  _pendingDeleteId = id;
  document.getElementById('modal-body').textContent =
    `Delete "${p.name}" (ID: ${p.id})? This action cannot be undone.`;
  document.getElementById('modal-overlay').style.display = 'flex';
}

function closeModal() {
  document.getElementById('modal-overlay').style.display = 'none';
  _pendingDeleteId = null;
}

document.addEventListener('DOMContentLoaded', () => {
  document.getElementById('modal-confirm-btn').addEventListener('click', () => {
    if (!_pendingDeleteId) return;
    const p = findById(_pendingDeleteId);
    const name = p ? p.name : _pendingDeleteId;
    const deleted = deleteProduct(_pendingDeleteId);
    closeModal();
    if (deleted) {
      toast(`Product '${esc(name)}' deleted.`, 'success');
      renderProductsTable(document.getElementById('product-filter')?.value || '');
      renderDashboard();
      handleSearch();
      renderAlertsSection();
    } else {
      toast('Failed to delete product.', 'error');
    }
  });
});

// ============================================================
// EDIT
// ============================================================

let _editingId = null;

function openEditModal(id) {
  const p = findById(id);
  if (!p) { toast('Product not found.', 'error'); return; }

  _editingId = id;
  document.getElementById('edit-id').value        = p.id;
  document.getElementById('edit-name').value      = p.name;
  document.getElementById('edit-price').value     = p.price;
  document.getElementById('edit-threshold').value = p.threshold;
  document.getElementById('edit-modal-overlay').style.display = 'flex';
}

function closeEditModal() {
  document.getElementById('edit-modal-overlay').style.display = 'none';
  _editingId = null;
}

function handleEditSubmit(e) {
  e.preventDefault();
  if (!_editingId) return;

  const name      = document.getElementById('edit-name').value;
  const price     = document.getElementById('edit-price').value;
  const threshold = document.getElementById('edit-threshold').value;

  const result = editProduct(_editingId, name, price, threshold);
  if (!result.ok) { toast(result.msg, 'error'); return; }

  toast(`Product '${esc(name)}' updated.`, 'success');
  closeEditModal();
  renderProductsTable(document.getElementById('product-filter')?.value || '');
  renderDashboard();
  handleSearch();
  renderAlertsSection();
}

// ============================================================
// CLOSE MODALS ON OVERLAY CLICK
// ============================================================

document.addEventListener('DOMContentLoaded', () => {
  document.getElementById('modal-overlay').addEventListener('click', function(e) {
    if (e.target === this) closeModal();
  });
  document.getElementById('edit-modal-overlay').addEventListener('click', function(e) {
    if (e.target === this) closeEditModal();
  });
});

// ============================================================
// ESCAPE HTML (XSS prevention)
// ============================================================

function esc(str) {
  if (!str) return '';
  return str.replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;').replace(/"/g,'&quot;');
}

// ============================================================
// SIDEBAR NAVIGATION EVENT BINDING
// ============================================================

document.addEventListener('DOMContentLoaded', () => {
  document.querySelectorAll('.nav-item').forEach(item => {
    item.addEventListener('click', e => {
      e.preventDefault();
      navigate(item.dataset.section);
    });
  });

  // Seed data on first load
  seedIfEmpty();

  // Init dashboard
  renderDashboard();
  navigate('dashboard');

  // Clock
  updateClock();
  setInterval(updateClock, 1000);
});
