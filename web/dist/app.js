const DEFAULT_API = 'https://6ad796ea95e98f36aef2ab6b.mockapi.io/api/v1';
const $ = (s, root = document) => root.querySelector(s);
const app = $('#app'), modal = $('#modal'), notice = $('#notice');
let base = localStorage.getItem('course-api') || DEFAULT_API;
let categories = [], courses = [], search = '', filter = '', generation = 0, saving = false;
const escape = value => String(value ?? '').replace(/[&<>"']/g, c => ({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[c]));
const money = n => new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND', maximumFractionDigits: 0 }).format(n);
const safeUrl = value => { try { const u = new URL(value); return u.protocol === 'https:' && !u.username && !u.password ? u.href : ''; } catch { return ''; } };
const categoryName = id => categories.find(c => c.id === id)?.name || 'Danh mục không còn tồn tại';
function notify(message = '', error = false) { notice.textContent = message; notice.className = error ? 'error' : ''; }
async function api(path, method = 'GET', body) {
  let response;
  try {
    response = await fetch(`${base}/${path}`, { method, headers: { 'Content-Type': 'application/json' },
      body: body === undefined ? undefined : JSON.stringify(body), signal: AbortSignal.timeout(25000) });
  } catch { throw new Error('Không thể kết nối API. Kiểm tra Internet và địa chỉ MockAPI rồi thử lại.'); }
  if (!response.ok) throw new Error(`API trả lỗi ${response.status}. ${response.status === 404 ? 'Kiểm tra resource hoặc bản ghi đã bị xóa.' : 'Vui lòng thử lại.'}`);
  if (method === 'DELETE' || response.status === 204) return;
  return response.json();
}
async function loadData() {
  const [nextCategories, nextCourses] = await Promise.all([api('categories'), api('courses')]);
  if (!Array.isArray(nextCategories) || !Array.isArray(nextCourses)) throw new Error('API phải trả về danh sách JSON.');
  categories = nextCategories; courses = nextCourses;
}
const intro = (eyebrow, title, subtitle, actions = '') => `<section class="intro"><div><span class="eyebrow">${eyebrow}</span><h1>${title}</h1><p>${subtitle}</p></div><div class="actions">${actions}</div></section>`;
const options = (selected = '', placeholder = 'Chọn danh mục…') => `<option value="">${placeholder}</option>${categories.map(c => `<option value="${escape(c.id)}" ${c.id === selected ? 'selected' : ''}>${escape(c.name)}</option>`).join('')}`;
function bindImages(root = app) {
  root.querySelectorAll('img').forEach(img => img.addEventListener('error', () => {
    img.removeAttribute('src'); img.alt = 'Không tải được ảnh bìa';
  }, { once: true }));
}
function listPage() {
  app.innerHTML = intro('KHÁM PHÁ & QUẢN LÝ', 'Thư viện khóa học', 'Mỗi khóa học là một bước tiến mới trên hành trình tri thức.', '<button id="add-course">＋ Thêm khóa học</button>') +
    `<div class="stats"><div class="stat"><span class="stat-icon">▦</span><div><small>Tổng số khóa học</small><strong>${courses.length.toString().padStart(2,'0')}</strong></div></div><div class="stat"><span class="stat-icon">▤</span><div><small>Danh mục học tập</small><strong>${categories.length.toString().padStart(2,'0')}</strong></div></div><div class="stat"><span class="stat-icon">◷</span><div><small>Tổng giờ học</small><strong>${courses.reduce((sum,c) => sum + (Number(c.duration) || 0),0)}</strong></div></div></div>
    <div class="filters"><input id="search" type="search" placeholder="Tìm khóa học bạn quan tâm…" aria-label="Tìm theo tên khóa học" value="${escape(search)}"><select id="filter" aria-label="Lọc danh mục">${options(filter,'Tất cả danh mục')}</select><button class="secondary" id="refresh">↻ Tải lại</button></div><div class="result-heading"><span>Khóa học của bạn <small id="count"></small></span><small>Không ngừng học hỏi, không ngừng tiến bộ</small></div><div id="results"></div>`;
  $('#add-course').onclick = () => location.hash = '#new';
  $('#refresh').onclick = route;
  $('#search').oninput = e => { search = e.target.value; renderCourses(); };
  $('#filter').onchange = e => { filter = e.target.value; renderCourses(); };
  if (!categories.some(c => c.id === filter)) filter = '';
  renderCourses();
}
function renderCourses() {
  const shown = courses.filter(c => String(c.title).toLocaleLowerCase('vi').includes(search.trim().toLocaleLowerCase('vi')) && (!filter || c.categoryId === filter));
  $('#count').textContent = `(${shown.length})`;
  $('#results').innerHTML = shown.length ? `<div class="grid">${shown.map(c => `<article class="course-card"><a class="card-link" href="#course/${encodeURIComponent(c.id)}" aria-label="${escape(c.title)}"><div class="cover"><img src="${escape(safeUrl(c.thumbnail))}" alt="Ảnh bìa ${escape(c.title)}" loading="lazy"><span class="badge">${escape(categoryName(c.categoryId))}</span></div><div class="card-body"><h2>${escape(c.title)}</h2><div class="instructor">◉ &nbsp; ${escape(c.instructor)}</div><div class="card-bottom"><span class="price">${money(c.price)}</span><span class="hours">◷ ${escape(c.duration)} giờ &nbsp; ↗</span></div></div></a></article>`).join('')}</div>` : '<div class="empty">Chưa có khóa học phù hợp.<p>Thử thay đổi bộ lọc hoặc thêm khóa học đầu tiên.</p></div>';
  bindImages();
}
function detailPage(c) {
  app.innerHTML = `<a class="back" href="#courses">← Trở về thư viện</a>` + intro(escape(categoryName(c.categoryId)), escape(c.title), `Giảng viên: ${escape(c.instructor)}`, `<button class="secondary" id="edit-course">Sửa khóa học</button><button class="danger" id="delete-course">Xóa</button>`) +
    `<div class="detail-layout"><div class="panel"><img class="detail-cover" src="${escape(safeUrl(c.thumbnail))}" alt="Ảnh bìa khóa học"><h2>Về khóa học này</h2><p class="description">${escape(c.description)}</p></div><div class="panel"><span class="eyebrow">ĐẦU TƯ CHO TRI THỨC</span><h1 class="price" style="font-size:30px">${money(c.price)}</h1><dl class="facts"><div><dt>Danh mục</dt><dd>${escape(categoryName(c.categoryId))}</dd></div><div><dt>Giảng viên</dt><dd>${escape(c.instructor)}</dd></div><div><dt>Thời lượng</dt><dd>${escape(c.duration)} giờ</dd></div><div><dt>Ngày tạo</dt><dd>${escape(c.createdAt)}</dd></div><div><dt>Mã khóa học</dt><dd>#${escape(c.id)}</dd></div></dl><button id="trailer" style="width:100%">▶ Xem trailer khóa học</button><p class="form-help">Video được phát ngay trong ứng dụng. Bạn có thể tạm dừng, tua và mở toàn màn hình.</p></div></div>`;
  bindImages();
  $('#edit-course').onclick = () => location.hash = `#edit/${encodeURIComponent(c.id)}`;
  $('#delete-course').onclick = () => confirmDelete(`Xóa “${c.title}”?`, 'Khóa học sẽ bị xóa khỏi cả bản web và ứng dụng Android.', async () => { await api(`courses/${encodeURIComponent(c.id)}`, 'DELETE'); location.hash = '#courses'; });
  $('#trailer').onclick = () => {
    showModal(`<h2>Trailer khóa học</h2><video controls autoplay playsinline src="${escape(safeUrl(c.trailerUrl))}"></video><p class="dialog-error" role="alert"></p><div class="form-actions"><button class="secondary" data-close>Đóng</button></div>`);
    $('video',modal).onerror = () => $('.dialog-error',modal).textContent = 'Không phát được trailer. Kiểm tra kết nối hoặc dùng URL MP4 trực tiếp.';
  };
}
function field(name, title, value = '', type = 'text', attrs = '') { return `<label>${title}<input name="${name}" type="${type}" value="${escape(value)}" required ${attrs}></label>`; }
function formPage(c = {}) {
  const editing = Boolean(c.id);
  app.innerHTML = `<a class="back" href="${editing ? `#course/${encodeURIComponent(c.id)}` : '#courses'}">← Quay lại</a>` + intro('THIẾT KẾ HÀNH TRÌNH HỌC TẬP', editing ? 'Chỉnh sửa khóa học' : 'Thêm khóa học mới', 'Điền thông tin bên dưới. Các trường có dấu * là bắt buộc.') +
    `<form id="course-form" class="panel"><div class="form-grid"><div class="full">${field('title','Tên khóa học *',c.title)}</div><label class="full">Mô tả *<textarea name="description" required>${escape(c.description)}</textarea></label><label>Danh mục *<select name="categoryId" required>${options(c.categoryId)}</select></label>${field('instructor','Giảng viên *',c.instructor)}${field('price','Học phí (VNĐ) *',c.price,'number','min="0" step="any"')}${field('duration','Thời lượng (giờ) *',c.duration,'number','min="1" step="1"')}
    <div class="full">${field('thumbnail','URL ảnh bìa HTTPS *',c.thumbnail,'url','placeholder="https://…"')}</div><img class="form-preview full" id="preview" alt="Ảnh xem trước" ${safeUrl(c.thumbnail) ? `src="${escape(safeUrl(c.thumbnail))}"` : 'hidden'}><div class="full">${field('trailerUrl','URL trailer HTTPS (MP4 trực tiếp) *',c.trailerUrl,'url','placeholder="https://…/video.mp4"')}</div>${field('createdAt','Ngày tạo *',c.createdAt || new Date().toLocaleDateString('en-CA'),'date')}</div><p class="form-help">Danh mục được lấy trực tiếp từ API. Chưa có danh mục? <a href="#categories">Đến quản lý danh mục ↗</a></p><p class="dialog-error" id="form-error" role="alert"></p><div class="form-actions"><button class="secondary" type="button" id="cancel-form">Hủy</button><button type="submit">${editing ? 'Lưu thay đổi' : 'Tạo khóa học'}</button></div></form>`;
  const form = $('#course-form');
  $('#cancel-form').onclick = () => location.hash = editing ? `#course/${encodeURIComponent(c.id)}` : '#courses';
  let timer;
  form.elements.thumbnail.oninput = e => {
    clearTimeout(timer); timer = setTimeout(() => { const img = $('#preview'); if (!img) return; const url = safeUrl(e.target.value); img.hidden = !url; if(url) img.src = url; }, 350);
  };
  $('#preview').onerror = e => { e.target.hidden = true; };
  form.onsubmit = async e => {
    e.preventDefault(); if(saving) return;
    const data = Object.fromEntries([...new FormData(form)].map(([k,v]) => [k,v.trim()]));
    data.price = Number(data.price); data.duration = Number(data.duration);
    await saveAction(form, $('#form-error'), async () => {
      if (!data.title || !data.description || !data.instructor) throw new Error('Vui lòng điền đủ trường bắt buộc.');
      if(!Number.isFinite(data.price) || data.price < 0) throw new Error('Học phí phải là số không âm.');
      if(!Number.isInteger(data.duration) || data.duration <= 0) throw new Error('Thời lượng phải là số nguyên dương.');
      if(!safeUrl(data.thumbnail) || !safeUrl(data.trailerUrl)) throw new Error('Ảnh và trailer phải dùng URL HTTPS hợp lệ.');
      if(!/^\d{4}-\d{2}-\d{2}$/.test(data.createdAt)) throw new Error('Ngày tạo không hợp lệ.');
      const fresh = await api('categories');
      if(!fresh.some(cat => cat.id === data.categoryId)) throw new Error('Danh mục không tồn tại hoặc đã bị xóa. Vui lòng chọn lại danh mục.');
      const saved = await api(editing ? `courses/${encodeURIComponent(c.id)}` : 'courses', editing ? 'PUT' : 'POST', data);
      location.hash = `#course/${encodeURIComponent(saved.id)}`;
      notify('Đã lưu khóa học thành công.');
    });
  };
}
function categoryPage() {
  app.innerHTML = intro('SẮP XẾP NỘI DUNG', 'Danh mục học tập', 'Tổ chức khóa học theo từng lĩnh vực, dễ tìm kiếm và quản lý.', '<button id="new-category">＋ Thêm danh mục</button><button class="secondary" id="refresh">↻</button>') +
    (categories.length ? `<div class="grid">${categories.map((c,i) => `<article class="panel category-card"><span class="stat-icon">${['▦','⌘','✧'][i%3]}</span><h2>${escape(c.name)}</h2><p>${escape(c.description)}</p><span class="badge">${courses.filter(course => course.categoryId === c.id).length} khóa học</span><div class="actions"><button class="secondary" data-edit="${escape(c.id)}">Chỉnh sửa</button><button class="danger" data-delete="${escape(c.id)}">Xóa</button></div></article>`).join('')}</div>` : '<div class="empty">Chưa có danh mục. Hãy thêm danh mục đầu tiên.</div>');
  $('#new-category').onclick = () => categoryDialog(); $('#refresh').onclick = route;
  app.querySelectorAll('[data-edit]').forEach(b => b.onclick = () => categoryDialog(categories.find(c => c.id === b.dataset.edit)));
  app.querySelectorAll('[data-delete]').forEach(b => b.onclick = () => {
    const c = categories.find(c => c.id === b.dataset.delete);
    confirmDelete(`Xóa “${c.name}”?`, 'Chỉ có thể xóa danh mục không chứa khóa học.', async () => {
      const freshCourses = await api('courses');
      if(freshCourses.some(course => course.categoryId === c.id)) throw new Error('Không thể xóa danh mục đang có khóa học.');
      await api(`categories/${encodeURIComponent(c.id)}`, 'DELETE'); await route();
    });
  });
}
function showModal(html) { $('#modal-content').innerHTML = html; modal.showModal(); modal.querySelectorAll('[data-close]').forEach(b => b.onclick = () => { if(!saving) modal.close(); }); }
modal.addEventListener('cancel', e => { if(saving) e.preventDefault(); });
modal.addEventListener('close', () => { const video = $('video', modal); if(video) { video.pause(); video.removeAttribute('src'); video.load(); } });
async function saveAction(container, error, action) {
  if(saving) return;
  saving = true; error.textContent = '';
  const buttons = [...container.querySelectorAll('button')]; buttons.forEach(b => b.disabled = true);
  try { await action(); } catch(e) { error.textContent = e.message; }
  finally { saving = false; buttons.forEach(b => b.disabled = false); }
}
function confirmDelete(title, description, action) {
  showModal(`<h2>${escape(title)}</h2><p>${escape(description)}</p><p class="dialog-error" role="alert"></p><div class="form-actions"><button class="secondary" data-close>Hủy</button><button class="danger" id="confirm-delete">Xác nhận xóa</button></div>`);
  $('#confirm-delete').onclick = () => saveAction(modal, $('.dialog-error',modal), async () => { await action(); modal.close(); notify('Đã xóa thành công.'); });
}
function categoryDialog(c = {}) {
  showModal(`<h2>${c.id ? 'Chỉnh sửa' : 'Thêm'} danh mục</h2><form id="category-form">${field('name','Tên danh mục *',c.name)}<label>Mô tả *<textarea name="description" required>${escape(c.description)}</textarea></label><p class="dialog-error" role="alert"></p><div class="form-actions"><button type="button" class="secondary" data-close>Hủy</button><button>Lưu danh mục</button></div></form>`);
  $('#category-form').onsubmit = e => { e.preventDefault(); const data = Object.fromEntries([...new FormData(e.target)].map(([k,v]) => [k,v.trim()]));
    saveAction(modal, $('.dialog-error',modal), async () => {
      if(!data.name || !data.description) throw new Error('Nhập đầy đủ tên và mô tả danh mục.');
      await api(c.id ? `categories/${encodeURIComponent(c.id)}` : 'categories', c.id ? 'PUT' : 'POST', data);
      modal.close(); await route(); notify('Đã lưu danh mục.');
    });
  };
}
$('#settings').onclick = () => {
  showModal(`<h2>Cấu hình MockAPI</h2><form id="api-form">${field('base','Base URL HTTPS',base,'url')}<p class="form-help">Ví dụ: https://PROJECT_ID.mockapi.io/api/v1<br>Không thêm /courses hoặc /categories vào cuối URL.</p><p class="dialog-error" role="alert"></p><div class="form-actions"><button class="secondary" type="button" data-close>Hủy</button><button>Lưu cấu hình</button></div></form>`);
  $('#api-form').onsubmit = e => {
    e.preventDefault();
    try {
      const value = e.target.elements.base.value.trim().replace(/\/+$/,''); const u = new URL(value);
      if(u.protocol !== 'https:' || !u.hostname.endsWith('.mockapi.io') || u.username || u.password || u.search || u.hash || /\/(courses|categories)$/.test(u.pathname)) throw new Error('Nhập Base URL HTTPS của MockAPI, không kèm tên resource.');
      base = value; localStorage.setItem('course-api',base); modal.close(); route();
    } catch(e) { $('.dialog-error',modal).textContent = e.message; }
  };
};
async function route() {
  const token = ++generation;
  const [page,id] = location.hash.slice(1).split('/');
  $('#nav-courses').classList.toggle('active',page !== 'categories');
  $('#nav-categories').classList.toggle('active',page === 'categories');
  $('#breadcrumb').textContent = page === 'categories' ? 'Danh mục' : 'Khóa học';
  app.innerHTML = '<div class="loading"><span class="spinner"></span>Đang tải dữ liệu…</div>';
  try {
    await loadData(); if(token !== generation) return;
    if(page === 'categories') categoryPage();
    else if(page === 'new') formPage();
    else if(page === 'course' || page === 'edit') {
      const course = await api(`courses/${encodeURIComponent(decodeURIComponent(id || ''))}`);
      if(token !== generation) return;
      page === 'course' ? detailPage(course) : formPage(course);
    } else listPage();
  } catch(e) {
    if(token !== generation) return;
    app.innerHTML = `<div class="empty"><h2>Chưa tải được dữ liệu</h2><p>${escape(e.message)}</p><button id="retry">Thử lại</button> <button class="secondary" id="configure-error">Cấu hình API</button></div>`;
    $('#retry').onclick = route; $('#configure-error').onclick = () => $('#settings').click();
  }
}
window.addEventListener('hashchange', () => { notify(); route(); window.scrollTo(0,0); });
route();
