/*
 * SkyRoute static demo runtime.
 * Every page of this site is a snapshot rendered by the real Spring Boot app.
 * This script stands in for the server: it routes links/forms to the saved pages
 * and answers the page's fetch/XHR calls from recorded responses.
 */
(function () {
  'use strict';
  var D = window.__SR_DATA || { pages: {}, api: {}, posts: {}, role: 'guest' };
  var BASE = window.__SR_BASE || '';
  var ROLE = D.role;
  var ROLES = {
    guest: { label: '비회원', home: '/main/home' },
    user: { label: '회원 · user01', home: '/main/home' },
    staff: { label: '지상직 · staff01', home: '/staff/schedule/today' },
    admin: { label: '관리자 · admin01', home: '/admin/base1' }
  };
  var IGNORE_PARAMS = { _csrf: 1, t: 1, _: 1 };
  var nativeFetch = window.fetch ? window.fetch.bind(window) : null;

  /* ---------- URL helpers ---------- */
  function normQuery(sp) {
    var arr = [];
    sp.forEach(function (v, k) { if (!IGNORE_PARAMS[k]) arr.push([k, v]); });
    arr.sort(function (a, b) { return (a[0] + a[1]) < (b[0] + b[1]) ? -1 : 1; });
    return arr.map(function (p) { return encodeURIComponent(p[0]) + '=' + encodeURIComponent(p[1]); }).join('&')
      .replace(/%20/g, '+');
  }
  // -> { role, path, query, key }
  function toApp(input) {
    var u = new URL(String(input), location.href);
    var p = u.pathname;
    var role = ROLE;
    if (BASE && p.indexOf(BASE + '/') === 0) {
      p = p.slice(BASE.length);
      var m = p.match(/^\/(guest|user|staff|admin)(\/.*|$)/);
      if (m) { role = m[1]; p = m[2] || '/'; }
    }
    p = p.replace(/\/_v\/[^/]+\.html$/, '').replace(/\/index\.html$/, '');
    if (p.length > 1) p = p.replace(/\/+$/, '');
    var q = normQuery(u.searchParams);
    return { role: role, path: p, query: q, key: p + (q ? '?' + q : ''), origin: u.origin };
  }
  function parseQ(q) {
    var o = {};
    if (!q) return o;
    q.split('&').forEach(function (kv) { var i = kv.indexOf('='); o[kv.slice(0, i)] = kv.slice(i + 1); });
    return o;
  }
  function dateNum(v) { var m = /^(\d{4})-(\d{2})-(\d{2})$/.exec(decodeURIComponent(v || '')); return m ? Date.UTC(+m[1], +m[2] - 1, +m[3]) / 864e5 : null; }

  // find a saved page for an app URL; returns { url, approx }
  function findPage(app) {
    var pages = D.pages;
    if (pages[app.key]) return { url: pages[app.key], approx: false };
    var want = parseQ(app.query), best = null, bestScore = -1;
    var prefix = app.path + '?';
    for (var k in pages) {
      if (k !== app.path && k.indexOf(prefix) !== 0) continue;
      var have = parseQ(k.slice(app.path.length + 1)), score = 0;
      for (var p in want) {
        if (have[p] === want[p]) score += (/airport|tripType|Id$|_id$|id$/i.test(p) ? 100 : 10);
        else if (have[p] != null) {
          var a = dateNum(have[p]), b = dateNum(want[p]);
          if (a != null && b != null) score += Math.max(0, 9 - Math.abs(a - b));
        }
      }
      for (var h in have) if (want[h] == null) score -= 1;
      if (score > bestScore) { bestScore = score; best = k; }
    }
    if (best != null) return { url: pages[best], approx: true };
    return null;
  }

  /* ---------- UI: toast, banner, fake payment ---------- */
  function el(tag, attrs, html) {
    var e = document.createElement(tag);
    for (var a in attrs || {}) e.setAttribute(a, attrs[a]);
    if (html != null) e.innerHTML = html;
    return e;
  }
  function toast(msg) {
    var box = document.getElementById('srd-toasts');
    if (!box) { box = el('div', { id: 'srd-toasts', role: 'status', 'aria-live': 'polite' }); document.body.appendChild(box); }
    var t = el('div', { 'class': 'srd-toast' }, msg);
    box.appendChild(t);
    setTimeout(function () { t.classList.add('srd-out'); }, 3200);
    setTimeout(function () { t.remove(); }, 3700);
  }
  window.__SR_TOAST = toast;

  function go(url, opts) {
    opts = opts || {};
    var app = toApp(url);
    var targetRole = app.role;
    var data = targetRole === ROLE ? D : null;
    if (!data) { // cross-role link: let the target role's router/404 handle it
      location.href = BASE + '/' + targetRole + app.key;
      return;
    }
    var hit = findPage(app);
    if (!hit) {
      toast(opts.missing || '이 화면은 데모에 포함되어 있지 않아요. (실제 서비스에서만 동작)');
      return false;
    }
    if (hit.approx) { try { sessionStorage.setItem('srd-approx', '1'); } catch (e) {} }
    if (opts.replace) location.replace(hit.url); else location.href = hit.url;
    return true;
  }
  window.__SR_GO = go;

  function landing(role) { return BASE + '/' + role + ROLES[role].home; }

  function banner() {
    if (document.getElementById('srd-bar') || window.top !== window) return;
    var bar = el('div', { id: 'srd-bar' });
    var links = Object.keys(ROLES).map(function (r) {
      return '<a href="' + landing(r) + '" class="' + (r === ROLE ? 'on' : '') + '">' + ROLES[r].label.split(' · ')[0] + '</a>';
    }).join('');
    bar.innerHTML =
      '<button type="button" class="srd-handle" aria-expanded="true" title="데모 안내 접기/펼치기">DEMO</button>' +
      '<div class="srd-body"><span class="srd-who">' + ROLES[ROLE].label + '</span>' +
      '<span class="srd-sep"></span><span class="srd-switch">' + links + '</span>' +
      '<span class="srd-sep"></span><a class="srd-home" href="' + BASE + '/">소개</a></div>';
    document.body.appendChild(bar);
    var h = bar.querySelector('.srd-handle');
    try { if (localStorage.getItem('srd-collapsed') === '1') { bar.classList.add('srd-min'); h.setAttribute('aria-expanded', 'false'); } } catch (e) {}
    h.addEventListener('click', function () {
      var min = bar.classList.toggle('srd-min');
      h.setAttribute('aria-expanded', String(!min));
      try { localStorage.setItem('srd-collapsed', min ? '1' : '0'); } catch (e) {}
    });
  }

  function fakePay(req) {
    return new Promise(function (resolve) {
      var amt = Number(req && req.totalAmount || 0).toLocaleString('ko-KR');
      var ov = el('div', { id: 'srd-pay', role: 'dialog', 'aria-modal': 'true' },
        '<div class="srd-pay-card"><p class="srd-pay-eyebrow">PortOne 결제창 (데모)</p>' +
        '<h3>' + (req && req.orderName ? req.orderName : 'SkyRoute 항공권') + '</h3>' +
        '<p class="srd-pay-amt">' + amt + '원</p>' +
        '<p class="srd-pay-note">실제 서비스에서는 PortOne(KG이니시스 · 카카오페이) 결제창이 열립니다. 데모에서는 실제 결제가 일어나지 않아요.</p>' +
        '<div class="srd-pay-actions"><button type="button" data-a="cancel">취소</button><button type="button" data-a="ok">결제 승인</button></div></div>');
      document.body.appendChild(ov);
      ov.addEventListener('click', function (e) {
        var a = e.target.getAttribute && e.target.getAttribute('data-a');
        if (!a) return;
        ov.remove();
        if (a === 'ok') resolve({ paymentId: req.paymentId, transactionType: 'PAYMENT' });
        else resolve({ code: 'FAILURE_TYPE_PG', message: '사용자가 결제를 취소했습니다.', pgMessage: '사용자가 결제를 취소했습니다.' });
      });
    });
  }
  window.PortOne = { requestPayment: fakePay };
  Object.defineProperty(window, 'PortOne', { value: window.PortOne, writable: false, configurable: false });

  /* ---------- mock responses ---------- */
  var MUTATION_MSG = '데모 모드에서는 변경 내용이 저장되지 않아요.';
  function genericOk(app) {
    return { status: 200, type: 'application/json', body: JSON.stringify({
      result: 'success', success: true, status: 'success', code: 'SUCCESS', message: MUTATION_MSG, data: [] }) };
  }
  function lookupApi(method, app, bodyText) {
    var api = D.api;
    var k = method + ' ' + app.key;
    if (api[k]) return api[k];
    if (method === 'GET') {
      // same path, other query -> closest recorded
      var best = null;
      for (var key in api) if (key.indexOf('GET ' + app.path + '?') === 0 || key === 'GET ' + app.path) { best = api[key]; break; }
      if (best) return best;
      return null;
    }
    var pk = method + ' ' + app.path;
    if (D.posts[pk]) return D.posts[pk];
    return null;
  }
  // resolves to {status, type, body}
  function respond(method, url, bodyText) {
    var app = toApp(url);
    var r = lookupApi(method, app, bodyText);
    if (r) return Promise.resolve(r);
    if (method === 'GET') {
      var hit = findPage(app);
      if (hit && nativeFetch) {
        return nativeFetch(hit.url).then(function (res) { return res.text(); })
          .then(function (t) { return { status: 200, type: 'text/html;charset=UTF-8', body: t }; });
      }
      return Promise.resolve({ status: 200, type: 'application/json', body: '{"result":"success","data":[]}' });
    }
    toast(MUTATION_MSG);
    return Promise.resolve(genericOk(app));
  }
  function isAppUrl(url) {
    try {
      var u = new URL(String(url), location.href);
      if (u.origin !== location.origin) return false;
      if (/\.(js|css|png|jpe?g|gif|svg|webp|woff2?|ico)$/i.test(u.pathname)) return false;
      if (u.pathname.indexOf(BASE + '/_demo/') === 0) return false;
      if (/\/_v\/[^/]+\.html$/.test(u.pathname)) return false;
      return true;
    } catch (e) { return false; }
  }

  if (nativeFetch) {
    window.fetch = function (input, init) {
      var url = typeof input === 'string' ? input : (input && input.url) || String(input);
      if (!isAppUrl(url)) return nativeFetch(input, init);
      var method = ((init && init.method) || (input && input.method) || 'GET').toUpperCase();
      var body = init && typeof init.body === 'string' ? init.body : null;
      return respond(method, url, body).then(function (r) {
        return new Response(r.body, { status: r.status || 200, headers: { 'Content-Type': r.type || 'application/json' } });
      });
    };
  }

  var XO = XMLHttpRequest.prototype.open, XS = XMLHttpRequest.prototype.send;
  XMLHttpRequest.prototype.open = function (method, url) {
    this.__srd = isAppUrl(url) ? { method: String(method).toUpperCase(), url: url } : null;
    return XO.apply(this, arguments);
  };
  XMLHttpRequest.prototype.setRequestHeader = (function (orig) {
    return function () { if (this.__srd) return; return orig.apply(this, arguments); };
  })(XMLHttpRequest.prototype.setRequestHeader);
  XMLHttpRequest.prototype.send = function (body) {
    var xhr = this, m = this.__srd;
    if (!m) return XS.apply(this, arguments);
    respond(m.method, m.url, typeof body === 'string' ? body : null).then(function (r) {
      var def = function (k, v) { Object.defineProperty(xhr, k, { value: v, configurable: true }); };
      def('readyState', 4); def('status', r.status || 200); def('statusText', 'OK');
      def('responseText', r.body); def('responseURL', m.url);
      var resp = r.body;
      if (xhr.responseType === 'json') { try { resp = JSON.parse(r.body); } catch (e) { resp = null; } }
      def('response', resp);
      xhr.getResponseHeader = function (h) { return /content-type/i.test(h) ? (r.type || 'application/json') : null; };
      xhr.getAllResponseHeaders = function () { return 'content-type: ' + (r.type || 'application/json') + '\r\n'; };
      ['readystatechange', 'load', 'loadend'].forEach(function (ev) {
        var fn = xhr['on' + ev];
        if (typeof fn === 'function') { try { fn.call(xhr, new Event(ev)); } catch (e) { console.error(e); } }
        xhr.dispatchEvent(new Event(ev));
      });
    });
  };

  /* ---------- navigation: links ---------- */
  document.addEventListener('click', function (e) {
    if (e.defaultPrevented || e.button !== 0 || e.metaKey || e.ctrlKey || e.shiftKey || e.altKey) return;
    var a = e.target.closest && e.target.closest('a[href]');
    if (!a || (a.target && a.target !== '_self')) return;
    var href = a.getAttribute('href');
    if (!href || href.charAt(0) === '#' || /^(javascript|mailto|tel):/i.test(href)) return;
    if (!isAppUrl(a.href)) return;
    var app = toApp(a.href);
    if (app.path === '/member/logout') { e.preventDefault(); location.href = landing('guest'); return; }
    e.preventDefault();
    go(a.href);
  }, false);

  /* ---------- navigation: forms ---------- */
  function formUrl(form, submitter) {
    var action = form.getAttribute('action') || location.href;
    var fd = new FormData(form);
    if (submitter && submitter.name) fd.append(submitter.name, submitter.value);
    return { action: new URL(action, location.href).href, fd: fd };
  }
  function handleForm(form, submitter) {
    var method = (form.getAttribute('method') || 'GET').toUpperCase();
    var f = formUrl(form, submitter);
    if (!isAppUrl(f.action)) return false;
    var app = toApp(f.action);
    if (method === 'GET') {
      var u = new URL(f.action);
      u.search = '';
      f.fd.forEach(function (v, k) { if (typeof v === 'string') u.searchParams.append(k, v); });
      go(u.href);
      return true;
    }
    if (app.path === '/member/login') {
      var id = String(f.fd.get('id') || '').toLowerCase();
      var adminPage = /Admin_login/.test(location.pathname);
      var role = adminPage ? (id.indexOf('admin') >= 0 ? 'admin' : 'staff') : 'user';
      location.href = landing(role);
      return true;
    }
    if (app.path === '/member/logout') { location.href = landing('guest'); return true; }
    var next = D.transitions[app.path];
    if (next) {
      try {
        if (ROLE === 'user' && !sessionStorage.getItem('srd-sample-told')) {
          sessionStorage.setItem('srd-sample-told', '1');
          sessionStorage.setItem('srd-sample', '1');
        }
      } catch (e) {}
      go(BASE + '/' + ROLE + next);
      return true;
    }
    toast(MUTATION_MSG);
    return true;
  }
  document.addEventListener('submit', function (e) {
    var form = e.target;
    if (e.defaultPrevented) return;
    var handled = handleForm(form, e.submitter);
    if (handled !== false) e.preventDefault();
  }, false);
  var nativeSubmit = HTMLFormElement.prototype.submit;
  HTMLFormElement.prototype.submit = function () {
    if (handleForm(this, null) === false) return nativeSubmit.call(this);
  };

  /* ---------- page boot ---------- */
  function boot() {
    banner();
    var approx = false;
    try { approx = sessionStorage.getItem('srd-approx') === '1'; sessionStorage.removeItem('srd-approx'); } catch (e) {}
    if (approx) toast('데모에 저장된 데이터 중 가장 비슷한 조건의 결과를 보여드려요.');
    var sample = false;
    try { sample = sessionStorage.getItem('srd-sample') === '1'; sessionStorage.removeItem('srd-sample'); } catch (e) {}
    if (sample) toast('데모에서는 미리 저장해 둔 예약(김포 ⇄ 제주 왕복) 정보로 다음 단계가 진행돼요.');
  }
  if (document.readyState === 'loading') document.addEventListener('DOMContentLoaded', boot); else boot();
})();
