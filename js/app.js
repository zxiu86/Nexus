/* =========================================================
   Nexus — app.js
   - قراءة works.json وعرض 15 عملاً في الصفحة مع Pagination
   - تنزيل مباشر لملف nexus.apk فور الضغط على أي زر تنزيل
   - صفحات تفاصيل وفصول ديناميكية لتقوية محركات بحث جوجل (SEO)
   - إتاحة زر التنزيل المباشر في جميع الصفحات (الرئيسية والتفاصيل)
========================================================= */
(function () {
  'use strict';

  const $ = (s, c) => (c || document).querySelector(s);
  const $$ = (s, c) => Array.from((c || document).querySelectorAll(s));

  /* إعدادات التقسيم (Pagination) والتنزيل المباشر */
  const ITEMS_PER_PAGE = 15;
  const CHAPTERS_WINDOW = 9;
  const DIRECT_APK_URL = 'https://github.com/zxiu86/Nexusap/releases/latest/download/nexus.apk';
  const RELEASES_PAGE = 'https://github.com/zxiu86/Nexusap/releases/latest';

  let allWorks = [];
  let filteredWorks = [];
  let currentPage = 1;
  let searchQuery = '';

  const slugify = (str) =>
    String(str || '').toLowerCase().trim()
      .replace(/[^\w\u0600-\u06FF\s-]/g, '')
      .replace(/\s+/g, '-').replace(/-+/g, '-');

  function normalizeWorks(data) {
    let arr = [];
    if (Array.isArray(data)) arr = data;
    else if (data && Array.isArray(data.works)) arr = data.works;
    else if (data && Array.isArray(data.data)) arr = data.data;
    else if (data && Array.isArray(data.list)) arr = data.list;
    else if (data && typeof data === 'object') arr = Object.values(data);
    return arr.map((w) => {
      if (!w || typeof w !== 'object') return null;
      const cover = w.cavar || w.cover || w.image || w.img || w.poster || w.thumbnail || '';
      const name = w.name || w.title || w.Name || w.Title || '';
      if (!name) return null;
      const last = parseInt(w.last_chapter ?? w.lastChapter ?? w.last ?? 0, 10);
      return {
        name: String(name),
        cover: String(cover),
        slug: slugify(name),
        lastChapter: Number.isFinite(last) && last > 0 ? last : 0,
      };
    }).filter(Boolean);
  }

  function chapterRange(lastChapter) {
    if (!lastChapter || lastChapter <= 0) return [];
    const from = Math.max(1, lastChapter - CHAPTERS_WINDOW);
    const out = [];
    for (let n = from; n >= 1; n--) out.push(n);
    return out;
  }

  const ICONS = {
    download: '<svg viewBox="0 0 24 24" width="20" height="20" fill="currentColor"><path fill-rule="evenodd" clip-rule="evenodd" d="M12 2.9c.5 0 .9.4.9.9v8.5l2.9-2.9a.9.9 0 1 1 1.3 1.3l-4.4 4.4a.9.9 0 0 1-1.3 0l-4.4-4.4a.9.9 0 1 1 1.3-1.3l2.9 2.9V3.8c0-.5.4-.9.9-.9ZM4.5 16.2c0-.5.4-.9.9-.9h13.2c.5 0 .9.4.9.9v2.4c0 1.3-1.1 2.4-2.4 2.4H6.9a2.4 2.4 0 0 1-2.4-2.4v-2.4Z"/></svg>',
    check: '<svg viewBox="0 0 24 24" width="15" height="15" fill="currentColor"><path d="M19.3 5.9a1 1 0 0 1 1.4 1.4l-9.6 9.6a1 1 0 0 1-1.4 0l-4.6-4.6a1 1 0 1 1 1.4-1.4l3.9 3.8 8.9-8.8Z"/></svg>',
    bolt: '<svg viewBox="0 0 24 24" width="15" height="15" fill="currentColor"><path d="M13 2 4.7 13.2c-.4.5 0 1.3.7 1.3H11l-1 7.3c-.1.8.9 1.2 1.4.6l8.3-11.2c.4-.5 0-1.3-.7-1.3H13l1-7.2c.1-.8-.9-1.3-1-.7Z"/></svg>',
    book: '<svg viewBox="0 0 24 24" width="15" height="15" fill="currentColor"><path d="M11.2 16.9c.5-.3 1.1-.3 1.7 0l5.1 2.6c.1 0 .1-.1.1-.1V5.3c0-.8-.7-1.5-1.5-1.5H7.4c-.8 0-1.5.7-1.5 1.5v14.1c0 .1.1.2.1.1l5.2-2.6Z"/></svg>',
    moon: '<svg viewBox="0 0 24 24" width="15" height="15" fill="currentColor"><path d="M20.4 15.6A8.8 8.8 0 0 1 8.4 3.6a.9.9 0 0 0-1.3-1A10.6 10.6 0 1 0 22.4 16.9a.9.9 0 0 0-1.9-1.3Z"/></svg>',
    wifi: '<svg viewBox="0 0 24 24" width="15" height="15" fill="currentColor"><path d="M12 18.2a1.9 1.9 0 1 0 0 3.8 1.9 1.9 0 0 0 0-3.8ZM12 13c-2 0-3.9.7-5.4 2a.9.9 0 1 0 1.2 1.3 8.9 8.9 0 0 1 8.4 0 .9.9 0 1 0 1.2-1.3A8.6 8.6 0 0 0 12 13Zm0-4.7c-3.2 0-6.2 1.2-8.5 3.3a.9.9 0 0 0 1.2 1.3A11.6 11.6 0 0 1 12 10c3.1 0 6.1 1.1 8.3 2.9a.9.9 0 1 0 1.2-1.3A13.4 13.4 0 0 0 12 8.3Z"/></svg>',
    back: '<svg viewBox="0 0 24 24" width="16" height="16" fill="currentColor"><path d="M9.4 6.4a.9.9 0 0 1 1.3 1.3L6.4 12l4.3 4.3a.9.9 0 1 1-1.3 1.3l-5-5a.9.9 0 0 1 0-1.3l5-5Z"/></svg>',
    list: '<svg viewBox="0 0 24 24" width="16" height="16" fill="currentColor"><path d="M8 6.5a.9.9 0 0 1 .9-.9h10.2a.9.9 0 1 1 0 1.8H8.9a.9.9 0 0 1-.9-.9Zm0 5.5a.9.9 0 0 1 .9-.9h10.2a.9.9 0 1 1 0 1.8H8.9A.9.9 0 0 1 8 12Zm0 5.5a.9.9 0 0 1 .9-.9h10.2a.9.9 0 1 1 0 1.8H8.9a.9.9 0 0 1-.9-.9ZM4.9 6.5a.9.9 0 1 0-1.8 0 .9.9 0 0 0 1.8 0Zm0 5.5a.9.9 0 1 0-1.8 0 .9.9 0 0 0 1.8 0Zm0 5.5a.9.9 0 1 0-1.8 0 .9.9 0 0 0 1.8 0Z"/></svg>',
  };

  const APP_FEATURES = [
    { icon: 'bolt', text: 'تحديثات فورية ومباشرة' },
    { icon: 'moon', text: 'وضع القراءة الليلي المريح' },
    { icon: 'book', text: 'مكتبة وحفظ التقدم تلقائياً' },
    { icon: 'wifi', text: 'تحميل وقراءة بدون إنترنت' },
    { icon: 'check', text: 'قراءة نقية بدون إعلانات' },
  ];

  const logoSVG = (size) =>
    `<svg viewBox="0 0 24 24" width="${size}" height="${size}" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round"><path d="M6 3v18l6-4 6 4V3l-6 4-6-4z"/></svg>`;

  function escHtml(s) {
    return String(s || '').replace(/[&<>"']/g, c => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[c]));
  }

  function setMeta(prop, content) {
    let tag = document.querySelector(`meta[name="${prop}"], meta[property="${prop}"]`);
    if (!tag) {
      tag = document.createElement('meta');
      if (prop.startsWith('og:')) tag.setAttribute('property', prop);
      else tag.setAttribute('name', prop);
      document.head.appendChild(tag);
    }
    tag.setAttribute('content', content);
  }

  function injectJSONLD(obj) {
    const s = document.createElement('script');
    s.type = 'application/ld+json';
    s.textContent = JSON.stringify(obj);
    document.head.appendChild(s);
  }

  /* =========================================================
     نظام التنزيل المباشر (Direct Download Engine)
     يقوم بتحميل ملف nexus.apk مباشرة للجهاز دون توقف
  ========================================================= */
  function triggerDirectDownload() {
    // 1. بدء تنزيل الملف فوراً عبر الرابط المباشر
    const downloadLink = document.createElement('a');
    downloadLink.href = DIRECT_APK_URL;
    downloadLink.setAttribute('download', 'nexus.apk');
    downloadLink.style.display = 'none';
    document.body.appendChild(downloadLink);
    downloadLink.click();
    setTimeout(() => downloadLink.remove(), 1000);

    // 2. إظهار نافذة التأكيد وإرشادات التثبيت
    openDownloadModal();
  }

  function openDownloadModal() {
    const modal = $('#downloadModal');
    if (modal) {
      modal.classList.add('open');
      modal.setAttribute('aria-hidden', 'false');
    }
  }

  function closeDownloadModal() {
    const modal = $('#downloadModal');
    if (modal) {
      modal.classList.remove('open');
      modal.setAttribute('aria-hidden', 'true');
    }
  }

  function bindDownloadModal() {
    // ربط جميع أزرار التنزيل في الصفحة بالتحميل المباشر
    $$('[data-download]').forEach((btn) => {
      btn.onclick = (e) => {
        e.preventDefault();
        triggerDirectDownload();
      };
    });

    const modal = $('#downloadModal');
    if (!modal) return;

    const closeBtn = $('#modalClose');
    if (closeBtn) closeBtn.onclick = closeDownloadModal;

    modal.onclick = (e) => {
      if (e.target === modal) closeDownloadModal();
    };

    const directBtn = $('#directDownloadBtn');
    if (directBtn) {
      directBtn.onclick = () => {
        // تأكيد إضافي للتنزيل المباشر
        triggerDirectDownload();
      };
    }
  }

  /* قالب نافذة التنزيل المباشر المشتركة */
  function downloadModalTemplate() {
    return `
      <div class="modal" id="downloadModal" aria-hidden="true">
        <div class="modal-card">
          <button class="modal-close" id="modalClose" aria-label="إغلاق">✕</button>
          <div class="modal-logo-wrap">
            <img src="assets/icon-192.png" alt="Nexus Fox Logo" class="modal-fox-icon" width="68" height="68" />
          </div>
          <div class="modal-badge-status">
            <span class="pulse-dot"></span> جاري بدء التنزيل المباشر...
          </div>
          <h3>تحميل تطبيق Nexus الرسمي</h3>
          <p class="modal-instruction">
            تم إرسال أمر تنزيل ملف <b>nexus.apk</b> تلقائياً إلى جهازك. إذا لم يبدأ التنزيل تلقائياً، اضغط الزر أدناه:
          </p>

          <div class="store-buttons">
            <a class="btn btn-primary btn-lg store-btn-single" href="${DIRECT_APK_URL}" id="directDownloadBtn" download="nexus.apk">
              ${ICONS.download}
              <span><b>اضغط هنا للتحميل المباشر (nexus.apk)</b><small>حجم الحزمة ~66MB • خالي من الإعلانات</small></span>
            </a>
          </div>

          <div class="install-steps">
            <div class="install-step">
              <span class="step-num">1</span>
              <span>افتح ملف <b>nexus.apk</b> بعد اكتمال التنزيل</span>
            </div>
            <div class="install-step">
              <span class="step-num">2</span>
              <span>اضغط "تثبيت" (اسمح بالتثبيت إذا طُلب منك)</span>
            </div>
            <div class="install-step">
              <span class="step-num">3</span>
              <span>افتح التطبيق واستمتع بقراءة آلاف الأعمال!</span>
            </div>
          </div>

          <div class="modal-footer-link">
            <a href="${RELEASES_PAGE}" target="_blank" rel="noopener noreferrer">
              عرض تفاصيل أحدث إصدار على GitHub Releases ←
            </a>
          </div>
        </div>
      </div>`;
  }

  /* =========================================================
     صفحة الفصل: ?work=slug&ch=n
  ========================================================= */
  function renderChapterPage(work, chNum) {
    const chTitle = `الفصل ${chNum}`;
    document.title = `${chTitle} — ${work.name} | اقرأ على تطبيق Nexus`;
    setMeta('description', `اقرأ ${chTitle} من ${work.name} مترجماً بالكامل على تطبيق Nexus. حمّل التطبيق مجاناً بصيغة APK واستمتع بأحدث الفصول فور صدورها بجودة فائقة.`);
    setMeta('og:title', `${chTitle} — ${work.name} | Nexus`);
    setMeta('og:description', `حمّل تطبيق Nexus واقرأ ${chTitle} من ${work.name} بجودة فائقة وبدون إعلانات.`);

    injectJSONLD({
      '@context': 'https://schema.org',
      '@type': 'Book',
      name: work.name,
      image: work.cover || undefined,
      inLanguage: 'ar',
      hasPart: { '@type': 'Chapter', position: chNum, name: `${work.name} — ${chTitle}` },
      offers: { '@type': 'Offer', price: '0', priceCurrency: 'USD', availability: 'https://schema.org/InStock' },
    });

    const others = chapterRange(work.lastChapter)
      .filter(n => n !== chNum)
      .slice(0, 14);

    document.body.innerHTML = `
      <div class="aurora" aria-hidden="true"></div>
      <main class="work-page">
        <a class="work-back" href="./?work=${encodeURIComponent(work.slug)}">${ICONS.back} العودة إلى ${escHtml(work.name)}</a>
        ${work.cover ? `<img class="work-cover" src="${escHtml(work.cover)}" alt="${escHtml(work.name)}" loading="eager" />` : `<div class="work-logo">${logoSVG(44)}</div>`}
        <h1 class="work-name">${escHtml(work.name)}</h1>
        <div class="chap-badge">${escHtml(chTitle)}</div>
        <p class="work-msg">للاستمتاع بقراءة <b>${escHtml(chTitle)}</b> من <b>${escHtml(work.name)}</b> بجودة فائقة وبدون إعلانات،<br>يرجى تنزيل تطبيق <b>Nexus</b> — مجاني ومتاح بصيغة APK مباشرة.</p>
        <button class="btn btn-primary btn-lg" data-download>
          ${ICONS.download} تنزيل التطبيق المباشر (APK)
        </button>
        ${others.length ? `
        <div class="chap-list">
          <h3>${ICONS.list} فصول أخرى من ${escHtml(work.name)}</h3>
          <div class="chap-links">
            ${others.map(n => `<a class="chap-link" href="?work=${encodeURIComponent(work.slug)}&ch=${n}">الفصل ${n}</a>`).join('')}
          </div>
        </div>` : ''}
        <div class="work-feats">
          ${APP_FEATURES.map(f => `<div class="work-feat">${ICONS[f.icon]} ${f.text}</div>`).join('')}
        </div>
      </main>
      ${downloadModalTemplate()}`;

    bindDownloadModal();
  }

  /* =========================================================
     صفحة العمل: ?work=slug
  ========================================================= */
  function renderWorkPage(work) {
    document.title = `${work.name} — اقرأ جميع الفصول على تطبيق Nexus | حمّل التطبيق`;
    setMeta('description', `اقرأ جميع فصول ${work.name} مترجمة بالكامل على تطبيق Nexus. حمّل التطبيق مجاناً بصيغة APK واستمتع بقراءة نقية بجودة فائقة.`);
    setMeta('og:title', `${work.name} — Nexus`);
    setMeta('og:description', `حمّل تطبيق Nexus واقرأ ${work.name} بجودة فائقة وبدون إعلانات.`);

    const chapters = chapterRange(work.lastChapter);

    injectJSONLD({
      '@context': 'https://schema.org',
      '@type': 'Book',
      name: work.name,
      image: work.cover || undefined,
      inLanguage: 'ar',
      offers: { '@type': 'Offer', price: '0', priceCurrency: 'USD', availability: 'https://schema.org/InStock' },
    });

    document.body.innerHTML = `
      <div class="aurora" aria-hidden="true"></div>
      <main class="work-page">
        <a class="work-back" href="./">${ICONS.back} العودة للرئيسية</a>
        ${work.cover ? `<img class="work-cover" src="${escHtml(work.cover)}" alt="${escHtml(work.name)}" loading="eager" />` : `<div class="work-logo">${logoSVG(44)}</div>`}
        <h1 class="work-name">${escHtml(work.name)}</h1>
        ${work.lastChapter > 0 ? `<div class="chap-badge">متوفر حتى الفصل ${work.lastChapter}</div>` : ''}
        <p class="work-msg">للاستمتاع بقراءة <b>${escHtml(work.name)}</b> بجودة فائقة وبدون إعلانات،<br>يرجى تنزيل تطبيق <b>Nexus</b> — مجاني ومتاح بصيغة APK مباشرة.</p>
        <button class="btn btn-primary btn-lg" data-download>
          ${ICONS.download} تنزيل التطبيق المباشر (APK)
        </button>
        ${chapters.length ? `
        <div class="chap-list">
          <h3>${ICONS.list} الفصول المتاحة</h3>
          <div class="chap-links">
            ${chapters.map(n => `<a class="chap-link" href="?work=${encodeURIComponent(work.slug)}&ch=${n}">الفصل ${n}</a>`).join('')}
          </div>
        </div>` : ''}
        <div class="work-feats">
          ${APP_FEATURES.map(f => `<div class="work-feat">${ICONS[f.icon]} ${f.text}</div>`).join('')}
        </div>
      </main>
      ${downloadModalTemplate()}`;

    bindDownloadModal();
  }

  /* =========================================================
     عرض بطاقة العمل الفردية
  ========================================================= */
  function cardHTML(w) {
    return `
      <a class="card" href="?work=${encodeURIComponent(w.slug)}" title="${escHtml(w.name)}">
        <div class="card-cover">
          ${w.cover ? `<img src="${escHtml(w.cover)}" alt="${escHtml(w.name)}" loading="lazy" decoding="async" />` : ''}
          ${w.lastChapter > 0 ? `<span class="card-badge">فصل ${w.lastChapter}</span>` : ''}
        </div>
        <div class="card-name">${escHtml(w.name)}</div>
      </a>`;
  }

  /* =========================================================
     نظام عرض الـ 15 عملاً والتنقل بين الصفحات (Pagination)
  ========================================================= */
  function renderWorksPage() {
    const grid = $('#worksGrid');
    const paginationWrap = $('#paginationWrap');
    const paginationEl = $('#pagination');
    const statsEl = $('#libraryStats');
    if (!grid) return;

    const total = filteredWorks.length;
    const totalPages = Math.max(1, Math.ceil(total / ITEMS_PER_PAGE));

    if (currentPage > totalPages) currentPage = 1;

    const startIdx = (currentPage - 1) * ITEMS_PER_PAGE;
    const endIdx = Math.min(startIdx + ITEMS_PER_PAGE, total);
    const pageItems = filteredWorks.slice(startIdx, endIdx);

    // تحديث إحصائيات المعروض
    if (statsEl) {
      if (total === 0) {
        statsEl.textContent = 'لا توجد أعمال مطابقة';
      } else {
        statsEl.textContent = `عرض ${startIdx + 1}–${endIdx} من أصل ${total} عمل (صفحة ${currentPage} من ${totalPages})`;
      }
    }

    // عرض البطاقات
    if (pageItems.length === 0) {
      grid.innerHTML = `
        <div class="no-results">
          <svg viewBox="0 0 24 24" width="48" height="48" fill="none" stroke="currentColor" stroke-width="1.5"><circle cx="11" cy="11" r="8"/><path d="m21 21-4.3-4.3"/></svg>
          <h3>لم نجد أي عمل بهذا الاسم</h3>
          <p>جرّب البحث باسم آخر أو تأكد من صحة الكلمات المكتوبة.</p>
        </div>`;
      if (paginationWrap) paginationWrap.style.display = 'none';
      return;
    }

    grid.innerHTML = pageItems.map(cardHTML).join('');

    // بناء أزرار التنقل (Pagination)
    if (paginationWrap && paginationEl) {
      if (totalPages <= 1) {
        paginationWrap.style.display = 'none';
      } else {
        paginationWrap.style.display = 'flex';
        paginationEl.innerHTML = buildPaginationHTML(currentPage, totalPages);
        bindPaginationEvents(totalPages);
      }
    }
  }

  /* توليد أرقام الصفحات بذكاء (مع النقاط ...) */
  function buildPaginationHTML(current, total) {
    let html = '';

    // زر السابق
    html += `<button class="page-btn page-nav" id="prevPageBtn" ${current === 1 ? 'disabled' : ''} aria-label="السابق">« السابق</button>`;

    const getPageNumbers = () => {
      if (total <= 7) {
        return Array.from({ length: total }, (_, i) => i + 1);
      }
      const pages = [];
      pages.push(1);
      if (current > 3) pages.push('...');
      const start = Math.max(2, current - 1);
      const end = Math.min(total - 1, current + 1);
      for (let i = start; i <= end; i++) {
        pages.push(i);
      }
      if (current < total - 2) pages.push('...');
      pages.push(total);
      return pages;
    };

    const pages = getPageNumbers();
    pages.forEach(p => {
      if (p === '...') {
        html += `<span class="page-dots">…</span>`;
      } else {
        html += `<button class="page-btn ${p === current ? 'active' : ''}" data-page="${p}">${p}</button>`;
      }
    });

    // زر التالي
    html += `<button class="page-btn page-nav" id="nextPageBtn" ${current === total ? 'disabled' : ''} aria-label="التالي">التالي »</button>`;

    return html;
  }

  function bindPaginationEvents(totalPages) {
    const prevBtn = $('#prevPageBtn');
    if (prevBtn) {
      prevBtn.onclick = () => {
        if (currentPage > 1) {
          currentPage--;
          renderWorksPage();
          scrollToLibrary();
        }
      };
    }

    const nextBtn = $('#nextPageBtn');
    if (nextBtn) {
      nextBtn.onclick = () => {
        if (currentPage < totalPages) {
          currentPage++;
          renderWorksPage();
          scrollToLibrary();
        }
      };
    }

    $$('.page-btn[data-page]').forEach(btn => {
      btn.onclick = () => {
        const pageNum = parseInt(btn.dataset.page, 10);
        if (pageNum && pageNum !== currentPage) {
          currentPage = pageNum;
          renderWorksPage();
          scrollToLibrary();
        }
      };
    });
  }

  function scrollToLibrary() {
    const lib = $('#library');
    if (lib) {
      lib.scrollIntoView({ behavior: 'smooth', block: 'start' });
    }
  }

  /* =========================================================
     الصفحة الرئيسية
  ========================================================= */
  function renderHomePage(works) {
    allWorks = works;
    filteredWorks = works;

    renderWorksPage();

    // البحث مع دعم مسح البحث
    const searchInput = $('#searchInput');
    const searchClear = $('#searchClearBtn');

    if (searchInput) {
      searchInput.oninput = () => {
        searchQuery = searchInput.value.trim().toLowerCase();
        if (searchClear) {
          searchClear.style.display = searchQuery ? 'grid' : 'none';
        }
        filteredWorks = searchQuery
          ? allWorks.filter(w => w.name.toLowerCase().includes(searchQuery))
          : allWorks;
        currentPage = 1;
        renderWorksPage();
      };
    }

    if (searchClear && searchInput) {
      searchClear.onclick = () => {
        searchInput.value = '';
        searchQuery = '';
        searchClear.style.display = 'none';
        filteredWorks = allWorks;
        currentPage = 1;
        renderWorksPage();
        searchInput.focus();
      };
    }

    // إضافة ItemList JSON-LD لأول 30 عملاً لدعم أرشفة جوجل
    const items = [];
    works.slice(0, 30).forEach((w) => {
      items.push({
        '@type': 'ListItem',
        position: items.length + 1,
        item: {
          '@type': 'Book',
          name: w.name,
          image: w.cover || undefined,
          url: `${location.origin}${location.pathname}?work=${encodeURIComponent(w.slug)}`
        },
      });
    });
    injectJSONLD({ '@context': 'https://schema.org', '@type': 'ItemList', itemListElement: items });
  }

  function animateCounters() {
    $$('[data-count]').forEach(el => {
      const target = parseInt(el.dataset.count, 10);
      const dur = 1600;
      const t0 = performance.now();
      const fmt = n => n >= 1000 ? (n / 1000).toFixed(n % 1000 === 0 ? 0 : 1).replace(/\.0$/, '') + 'K' : n;
      (function tick(t) {
        const p = Math.min((t - t0) / dur, 1);
        el.textContent = fmt(Math.round(target * (1 - Math.pow(1 - p, 3))));
        if (p < 1) requestAnimationFrame(tick);
      })(t0);
    });
  }

  /* =========================================================
     نقطة البداية (Main)
  ========================================================= */
  async function main() {
    let works = [];
    try {
      const res = await fetch('works.json');
      if (res.ok) works = normalizeWorks(await res.json());
    } catch (_) {}

    const params = new URLSearchParams(location.search);
    const workSlug = params.get('work');
    const chNum = parseInt(params.get('ch'), 10);

    if (workSlug) {
      const work = works.find(w => w.slug === workSlug) ||
        { name: decodeURIComponent(workSlug).replace(/-/g, ' '), cover: '', slug: workSlug, lastChapter: 0 };
      if (chNum >= 1) renderChapterPage(work, chNum);
      else renderWorkPage(work);
      return;
    }

    renderHomePage(works);
    bindDownloadModal();
    animateCounters();
  }

  document.addEventListener('DOMContentLoaded', main);
})();
