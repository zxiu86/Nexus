/* =========================================================
   Nexus — app.js
   - يقرأ works.json: cavar (الكافر) + name + last_chapter
   - يولّد صفحات SEO ديناميكية:
       ?work=<slug>            → صفحة العمل
       ?work=<slug>&ch=<n>     → صفحة الفصل n (n من 1 إلى last_chapter-9)
   - زر التنزيل يجلب أحدث إصدار تلقائياً من صفحة الإصدارات
========================================================= */
(function () {
  'use strict';

  const $ = (s, c) => (c || document).querySelector(s);
  const $$ = (s, c) => Array.from((c || document).querySelectorAll(s));

  /* عدد الفصول التي تُولّد قبل الحد الأقصى */
  const CHAPTERS_WINDOW = 9;

  /* روابط الإصدارات — تُحدَّث تلقائياً عند كل Release جديد */
  const RELEASES_PAGE = 'https://github.com/zxiu86/Nexus/releases/latest';

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

  /* الفصول المتاحة SEO: من (last_chapter - 9) إلى 1 */
  function chapterRange(lastChapter) {
    if (!lastChapter || lastChapter <= 0) return [];
    const from = Math.max(1, lastChapter - CHAPTERS_WINDOW);
    const out = [];
    for (let n = from; n >= 1; n--) out.push(n);
    return out;
  }

  const ICONS = {
    download: '<svg viewBox="0 0 24 24" width="18" height="18" fill="currentColor"><path fill-rule="evenodd" clip-rule="evenodd" d="M12 2.9c.5 0 .9.4.9.9v8.5l2.9-2.9a.9.9 0 1 1 1.3 1.3l-4.4 4.4a.9.9 0 0 1-1.3 0l-4.4-4.4a.9.9 0 1 1 1.3-1.3l2.9 2.9V3.8c0-.5.4-.9.9-.9ZM4.5 16.2c0-.5.4-.9.9-.9h13.2c.5 0 .9.4.9.9v2.4c0 1.3-1.1 2.4-2.4 2.4H6.9a2.4 2.4 0 0 1-2.4-2.4v-2.4Z"/></svg>',
    check: '<svg viewBox="0 0 24 24" width="15" height="15" fill="currentColor"><path d="M19.3 5.9a1 1 0 0 1 1.4 1.4l-9.6 9.6a1 1 0 0 1-1.4 0l-4.6-4.6a1 1 0 1 1 1.4-1.4l3.9 3.8 8.9-8.8Z"/></svg>',
    bolt: '<svg viewBox="0 0 24 24" width="15" height="15" fill="currentColor"><path d="M13 2 4.7 13.2c-.4.5 0 1.3.7 1.3H11l-1 7.3c-.1.8.9 1.2 1.4.6l8.3-11.2c.4-.5 0-1.3-.7-1.3H13l1-7.2c.1-.8-.9-1.3-1-.7Z"/></svg>',
    book: '<svg viewBox="0 0 24 24" width="15" height="15" fill="currentColor"><path d="M11.2 16.9c.5-.3 1.1-.3 1.7 0l5.1 2.6c.1 0 .1-.1.1-.1V5.3c0-.8-.7-1.5-1.5-1.5H7.4c-.8 0-1.5.7-1.5 1.5v14.1c0 .1.1.2.1.1l5.2-2.6Z"/></svg>',
    moon: '<svg viewBox="0 0 24 24" width="15" height="15" fill="currentColor"><path d="M20.4 15.6A8.8 8.8 0 0 1 8.4 3.6a.9.9 0 0 0-1.3-1A10.6 10.6 0 1 0 22.4 16.9a.9.9 0 0 0-1.9-1.3Z"/></svg>',
    wifi: '<svg viewBox="0 0 24 24" width="15" height="15" fill="currentColor"><path d="M12 18.2a1.9 1.9 0 1 0 0 3.8 1.9 1.9 0 0 0 0-3.8ZM12 13c-2 0-3.9.7-5.4 2a.9.9 0 1 0 1.2 1.3 8.9 8.9 0 0 1 8.4 0 .9.9 0 1 0 1.2-1.3A8.6 8.6 0 0 0 12 13Zm0-4.7c-3.2 0-6.2 1.2-8.5 3.3a.9.9 0 0 0 1.2 1.3A11.6 11.6 0 0 1 12 10c3.1 0 6.1 1.1 8.3 2.9a.9.9 0 1 0 1.2-1.3A13.4 13.4 0 0 0 12 8.3Z"/></svg>',
    back: '<svg viewBox="0 0 24 24" width="15" height="15" fill="currentColor"><path d="M9.4 6.4a.9.9 0 0 1 1.3 1.3L6.4 12l4.3 4.3a.9.9 0 1 1-1.3 1.3l-5-5a.9.9 0 0 1 0-1.3l5-5Z"/></svg>',
    list: '<svg viewBox="0 0 24 24" width="15" height="15" fill="currentColor"><path d="M8 6.5a.9.9 0 0 1 .9-.9h10.2a.9.9 0 1 1 0 1.8H8.9a.9.9 0 0 1-.9-.9Zm0 5.5a.9.9 0 0 1 .9-.9h10.2a.9.9 0 1 1 0 1.8H8.9A.9.9 0 0 1 8 12Zm0 5.5a.9.9 0 0 1 .9-.9h10.2a.9.9 0 1 1 0 1.8H8.9a.9.9 0 0 1-.9-.9ZM4.9 6.5a.9.9 0 1 0-1.8 0 .9.9 0 0 0 1.8 0Zm0 5.5a.9.9 0 1 0-1.8 0 .9.9 0 0 0 1.8 0Zm0 5.5a.9.9 0 1 0-1.8 0 .9.9 0 0 0 1.8 0Z"/></svg>',
  };

  const APP_FEATURES = [
    { icon: 'bolt',  text: 'تحديثات فورية لأحدث الفصول' },
    { icon: 'moon',  text: 'وضع القراءة الليلي المريح' },
    { icon: 'book',  text: 'مكتبة شخصية ومفضلات' },
    { icon: 'wifi',  text: 'تحميل وقراءة بدون إنترنت' },
    { icon: 'check', text: 'بدون إعلانات مزعجة' },
  ];

  const logoSVG = (size) =>
    `<svg viewBox="0 0 24 24" width="${size}" height="${size}" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round"><path d="M6 3v18l6-4 6 4V3l-6 4-6-4z"/></svg>`;

  function escHtml(s) { return String(s).replace(/[&<>"']/g, c => ({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[c])); }

  function setMeta(prop, content) {
    let tag = document.querySelector(`meta[name="${prop}"], meta[property="${prop}"]`);
    if (!tag) {
      tag = document.createElement('meta');
      if (prop.startsWith('og:')) tag.setAttribute('property', prop); else tag.setAttribute('name', prop);
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
     صفحة الفصل: ?work=slug&ch=n
  ========================================================= */
  function renderChapterPage(work, chNum, works) {
    const chTitle = `الفصل ${chNum}`;
    document.title = `${chTitle} — ${work.name} | اقرأ على تطبيق Nexus`;
    setMeta('description', `اقرأ ${chTitle} من ${work.name} مترجماً بالكامل على تطبيق Nexus. حمّل التطبيق مجاناً واستمتع بأحدث الفصول فور صدورها بجودة فائقة.`);
    setMeta('og:title', `${chTitle} — ${work.name} | Nexus`);
    setMeta('og:description', `حمّل تطبيق Nexus واقرأ ${chTitle} من ${work.name} بجودة فائقة.`);

    injectJSONLD({
      '@context': 'https://schema.org',
      '@type': 'Book',
      name: work.name,
      image: work.cover || undefined,
      inLanguage: 'ar',
      hasPart: { '@type': 'Chapter', position: chNum, name: `${work.name} — ${chTitle}` },
      offers: { '@type': 'Offer', price: '0', priceCurrency: 'USD', availability: 'https://schema.org/InStock' },
    });

    /* فصول أخرى للعمل نفسه (روابط داخلية — تقوية SEO) */
    const others = chapterRange(work.lastChapter)
      .filter(n => n !== chNum)
      .slice(0, 12);

    document.body.innerHTML = `
      <div class="aurora"></div>
      <div class="grid-overlay"></div>
      <main class="work-page">
        <a class="work-back" href="./?work=${encodeURIComponent(work.slug)}">${ICONS.back} العودة إلى ${escHtml(work.name)}</a>
        ${work.cover ? `<img class="work-cover" src="${escHtml(work.cover)}" alt="${escHtml(work.name)}" loading="eager" />` : `<div class="work-logo">${logoSVG(44)}</div>`}
        <h1 class="work-name">${escHtml(work.name)}</h1>
        <div class="chap-badge">${escHtml(chTitle)}</div>
        <div class="work-logo">${logoSVG(36)}</div>
        <p class="work-msg">للاستمتاع بقراءة <b>${escHtml(chTitle)}</b> من <b>${escHtml(work.name)}</b> بجودة فائقة وبدون إعلانات،<br>يرجى تنزيل تطبيق <b>Nexus</b> — مجاناً ومتاح على جميع الأجهزة.</p>
        <button class="btn btn-primary btn-lg" data-download>${ICONS.download} تنزيل التطبيق</button>
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
      </main>`;
    bindDownloadModal();
  }

  /* =========================================================
     صفحة العمل: ?work=slug
  ========================================================= */
  function renderWorkPage(work) {
    document.title = `${work.name} — اقرأ جميع الفصول على تطبيق Nexus | حمّل التطبيق`;
    setMeta('description', `اقرأ ${work.name} مترجمة بالكامل على تطبيق Nexus. حمّل التطبيق مجاناً واستمتع بأحدث الفصول فور صدورها بجودة فائقة.`);
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
      <div class="aurora"></div>
      <div class="grid-overlay"></div>
      <main class="work-page">
        <a class="work-back" href="./">${ICONS.back} العودة للرئيسية</a>
        ${work.cover ? `<img class="work-cover" src="${escHtml(work.cover)}" alt="${escHtml(work.name)}" loading="eager" />` : `<div class="work-logo">${logoSVG(44)}</div>`}
        <h1 class="work-name">${escHtml(work.name)}</h1>
        <div class="work-logo">${logoSVG(36)}</div>
        <p class="work-msg">للاستمتاع بقراءة <b>${escHtml(work.name)}</b> بجودة فائقة وبدون إعلانات،<br>يرجى تنزيل تطبيق <b>Nexus</b> — مجاناً ومتاح على جميع الأجهزة.</p>
        <button class="btn btn-primary btn-lg" data-download>${ICONS.download} تنزيل التطبيق</button>
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
      </main>`;
    bindDownloadModal();
  }

  /* =========================================================
     الصفحة الرئيسية
  ========================================================= */
  function cardHTML(w) {
    return `
      <a class="card" href="?work=${encodeURIComponent(w.slug)}" title="${escHtml(w.name)}">
        <div class="card-cover">
          ${w.cover ? `<img src="${escHtml(w.cover)}" alt="${escHtml(w.name)}" loading="lazy" />` : ''}
        </div>
        <div class="card-name">${escHtml(w.name)}</div>
      </a>`;
  }

  function renderHomePage(works) {
    const grid = $('#worksGrid');
    const gridAll = $('#worksGridAll');
    if (!grid) return;

    grid.innerHTML = works.slice(0, 8).map(cardHTML).join('');
    gridAll.innerHTML = works.slice(8).map(cardHTML).join('');

    const input = $('#searchInput');
    if (input) {
      input.addEventListener('input', () => {
        const q = input.value.trim().toLowerCase();
        const pool = q ? works.filter(w => w.name.toLowerCase().includes(q)) : works;
        grid.innerHTML = pool.slice(0, 8).map(cardHTML).join('');
        gridAll.innerHTML = pool.slice(8).map(cardHTML).join('');
      });
    }

    /* ItemList JSON-LD — أعمال + فصولها (روابط داخلية كثيرة لـ SEO) */
    const items = [];
    works.slice(0, 20).forEach((w) => {
      items.push({
        '@type': 'ListItem', position: items.length + 1,
        item: { '@type': 'Book', name: w.name, image: w.cover || undefined,
                url: `${location.origin}${location.pathname}?work=${w.slug}` },
      });
      chapterRange(w.lastChapter).slice(0, 5).forEach((n) => {
        items.push({
          '@type': 'ListItem', position: items.length + 1,
          item: { '@type': 'Chapter', name: `${w.name} — الفصل ${n}`,
                  url: `${location.origin}${location.pathname}?work=${w.slug}&ch=${n}` },
        });
      });
    });
    injectJSONLD({ '@context': 'https://schema.org', '@type': 'ItemList', itemListElement: items });
  }

  /* =========================================================
     مودال التنزيل — يجلب أحدث إصدار تلقائياً
  ========================================================= */
  function bindDownloadModal() {
    const modal = $('#downloadModal');
    if (!modal) return;
    $$('[data-download]').forEach(b => b.addEventListener('click', () => modal.classList.add('open')));
    $('#modalClose').addEventListener('click', () => modal.classList.remove('open'));
    modal.addEventListener('click', e => { if (e.target === modal) modal.classList.remove('open'); });

    const btn = $('#directDownloadBtn');
    if (btn) {
      btn.addEventListener('click', (e) => {
        e.preventDefault();
        /* يحوّل تلقائياً لأحدث إصدار من صفحة الإصدارات */
        window.location.href = RELEASES_PAGE;
      });
    }
  }

  function animateCounters() {
    $$('[data-count]').forEach(el => {
      const target = parseInt(el.dataset.count, 10);
      const dur = 1600, t0 = performance.now();
      const fmt = n => n >= 1000 ? (n / 1000).toFixed(n % 1000 === 0 ? 0 : 1).replace(/\.0$/, '') + 'K' : n;
      (function tick(t) {
        const p = Math.min((t - t0) / dur, 1);
        el.textContent = fmt(Math.round(target * (1 - Math.pow(1 - p, 3))));
        if (p < 1) requestAnimationFrame(tick);
      })(t0);
    });
  }

  /* =========================================================
     التشغيل
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
      if (chNum >= 1) renderChapterPage(work, chNum, works);
      else renderWorkPage(work);
      return;
    }

    renderHomePage(works);
    bindDownloadModal();
    animateCounters();
  }

  document.addEventListener('DOMContentLoaded', main);
})();
