/* =========================================================
   STAFF 공통 화면 제어 스크립트
========================================================= */

/* ==================== 공통 유틸 ==================== */
function $(id) { return document.getElementById(id); }

/* ==================== 내부 탭 제어 ==================== */
function subTab(scope, tabName, btn) {
  const prefix = scope === 'base' ? 'b' : 'c';
  const target = $(`tab-${prefix}-${tabName}`);
  if (!target) return;
  const root = target.closest('.main') || document;
  root.querySelectorAll(`.sub-page[id^="tab-${prefix}-"]`).forEach(page => page.classList.remove('active'));
  const tabBox = target.parentElement ? root.querySelector('.tabs') : null;
  if (tabBox) { tabBox.querySelectorAll('button').forEach(button => button.classList.remove('active')); }
  target.classList.add('active');
  if (btn) btn.classList.add('active');
}

function nav(el, page) {
  document.querySelectorAll('.nav-link').forEach(link => link.classList.remove('active'));
  if (el) el.classList.add('active');
  document.querySelectorAll('.page').forEach(pageEl => pageEl.classList.remove('active'));
  const target = $(`p-${page}`);
  if (target) target.classList.add('active');
  window.scrollTo(0, 0);
}

/* ==================== 토스트 알림 ==================== */
function toast(message) {
  const wrap = $('toasts');
  if (!wrap) { 
      alert(message); // toast 컨테이너가 없을 경우 기본 alert로 대체
      return; 
  }
  const toastEl = document.createElement('div');
  toastEl.className = 'toast';
  toastEl.innerHTML = `<span class="material-symbols-outlined" style="color:var(--ok)">check_circle</span> ${message}`;
  wrap.appendChild(toastEl);
  setTimeout(() => {
    toastEl.style.opacity = '0';
    toastEl.style.transition = 'opacity .2s';
    setTimeout(() => toastEl.remove(), 200);
  }, 2200);
}

/* =========================================================
   지연/결항 안내 (임시 대기열 및 서버 전송)
========================================================= */
let stagedNotices = []; 

function toggleNoticeField() {
  const typeSelect = document.getElementById('noticeType');
  const delayWrapper = document.getElementById('noticeDelayWrapper');
  if (!typeSelect || !delayWrapper) return;
  delayWrapper.style.display = typeSelect.value === 'DELAY' ? 'block' : 'none';
}

function openFlightSearchModal() {
    document.getElementById('flightSearchModal').style.display = 'flex';
}
function closeFlightSearchModal() {
    document.getElementById('flightSearchModal').style.display = 'none';
}

function filterModalFlights() {
    const dateStr = document.getElementById('modalSearchDate').value; 
    const keyword = document.getElementById('modalSearchKeyword').value.toLowerCase();
    const rows = document.querySelectorAll('.modal-flight-row');

    rows.forEach(row => {
        const flightNo = row.querySelector('.flight-no').textContent.toLowerCase();
        const route = row.querySelector('.flight-route').textContent.toLowerCase();
        const time = row.querySelector('.flight-time').textContent; 

        let matchKeyword = flightNo.includes(keyword) || route.includes(keyword);
        let matchDate = dateStr === '' || time.startsWith(dateStr);

        row.style.display = (matchKeyword && matchDate) ? '' : 'none';
    });
}

function selectFlightFromModal(btn) {
    document.getElementById('noticeFlightId').value = btn.getAttribute('data-id');
    document.getElementById('noticeFlightText').value = btn.getAttribute('data-text');
    closeFlightSearchModal(); 
}

function stageFlightNotice() {
    const flightIdEl = document.getElementById('noticeFlightId');
    const flightTextEl = document.getElementById('noticeFlightText');
    const noticeTypeEl = document.getElementById('noticeType');
    const delayMinutesEl = document.getElementById('noticeDelayMinutes');
    const reasonEl = document.getElementById('noticeReason');

    if (!flightIdEl || !noticeTypeEl || !reasonEl) return;

    const flightId = flightIdEl.value;
    const flightText = flightTextEl.value ? flightTextEl.value.split(' ')[0] : '';
    const noticeType = noticeTypeEl.value;
    
    // 결항일 때 무조건 숫자 0을 세팅
    const delayMinutes = noticeType === 'DELAY' ? (Number(delayMinutesEl.value) || 0) : 0; 
    const reason = reasonEl.value.trim();

    if (!flightId) { alert('대상 항공편을 먼저 검색하여 선택해 주세요.'); return; }
    if (!reason) { alert('상세 발생 사유를 입력해야 합니다.'); reasonEl.focus(); return; }
    if (noticeType === 'DELAY' && delayMinutes <= 0) { alert('지연 시간은 1분 이상이어야 합니다.'); delayMinutesEl.focus(); return; }

    const stagedItem = {
        id: Date.now(), 
        flight_id: flightId,
        flight_no: flightText,
        notice_type: noticeType,
        delay_minutes: delayMinutes,
        reason: reason
    };

    stagedNotices.push(stagedItem);
    renderStagedNotices(); 

    flightIdEl.value = '';
    if(flightTextEl) flightTextEl.value = '';
    reasonEl.value = '';
    if(delayMinutesEl) delayMinutesEl.value = '';
}

function renderStagedNotices() {
    const tbody = document.getElementById('stagedNoticeTable');
    if(!tbody) return;

    tbody.innerHTML = stagedNotices.map(item => `
        <tr style="background-color: #fefce8; border-bottom: 2px solid #fde047;"> 
            <td class="mono">-</td>
            <td style="font-weight: bold; color: var(--primary);" class="mono">${item.flight_no}</td>
            <td>
                <span class="badge ${item.notice_type === 'DELAY' ? 'badge-delayed' : 'badge-cancelled'}">
                    ${item.notice_type === 'DELAY' ? '지연' : '결항'}
                </span>
            </td>
            <td class="mono">${item.delay_minutes > 0 ? item.delay_minutes + '분' : '-'}</td>
            <td style="max-width:220px; overflow:hidden; text-overflow:ellipsis; white-space:nowrap;">${item.reason}</td>
            <td class="mono" style="color:var(--warn)">대기중</td>
            <td class="mono" style="color:var(--warn)">대기중</td>
            <td>
                <div class="btn-action-group">
                    <button class="btn btn-primary btn-sm" onclick="confirmNotice(${item.id})">확인</button>
                    <button class="btn btn-ghost btn-sm" style="color:var(--danger)" onclick="cancelNotice(${item.id})">삭제</button>
                </div>
            </td>
        </tr>
    `).join('');
}

function cancelNotice(tempId) {
    stagedNotices = stagedNotices.filter(item => item.id !== tempId);
    renderStagedNotices();
}

// 실제 서버로 지연/결항 데이터를 전송하는 함수
function confirmNotice(tempId) {
    const item = stagedNotices.find(i => i.id === tempId);
    if(!item) return;

    if(!confirm('해당 지연/결항 안내를 확정하고 승객들에게 알림을 발송하시겠습니까?')) return;

    const requestData = {
        flight_id: item.flight_id,
        notice_type: item.notice_type,
        delay_minutes: item.delay_minutes, 
        reason: item.reason
    };

    const csrfMeta = document.querySelector('meta[name="_csrf"]');
    const csrfHeaderMeta = document.querySelector('meta[name="_csrf_header"]');
    if (!csrfMeta || !csrfHeaderMeta) { alert('보안 토큰(CSRF)을 찾을 수 없습니다.'); return; }

    const headers = { 
        'Content-Type': 'application/json',
        'X-Requested-With': 'XMLHttpRequest' 
    };
    headers[csrfHeaderMeta.getAttribute('content')] = csrfMeta.getAttribute('content');

    fetch('/staff/delay/api/insert', {
        method: 'POST',
        headers: headers,
        body: JSON.stringify(requestData)
    })
    .then(res => res.text())
    .then(result => {
        if(result === 'success') {
            alert('성공적으로 등록 및 알림이 발송되었습니다.');
            location.reload(); 
        } else {
            alert('등록에 실패했습니다.');
        }
    })
    .catch(err => {
        console.error(err);
        alert('서버 통신 중 오류가 발생했습니다.');
    });
}

/* =========================================================
   스케줄 공통 통신 (운항 스케줄 추가/수정/삭제/상태변경)
========================================================= */
function openFlightAddModal() {
    document.getElementById('flightForm').reset();
    document.getElementById('modalFlightId').value = '';
    document.getElementById('flightModalTitle').innerText = '새 운항 스케줄 등록';
    filterGatesByRoute();
    document.getElementById('flightModal').style.display = 'flex';
}

function openFlightEditModal(btn) {
    document.getElementById('flightForm').reset();
    document.getElementById('modalFlightId').value = btn.getAttribute('data-id');
    document.getElementById('flightModalTitle').innerText = '운항 스케줄 수정';
    document.getElementById('modalFlightNo').value = btn.getAttribute('data-no');
    document.getElementById('modalRouteId').value = btn.getAttribute('data-route');
    document.getElementById('modalAircraftId').value = btn.getAttribute('data-ac');
    filterGatesByRoute();
    document.getElementById('modalDepGateId').value = btn.getAttribute('data-dgate') || 0;
    document.getElementById('modalArrGateId').value = btn.getAttribute('data-agate') || 0;
    document.getElementById('modalDepTime').value = btn.getAttribute('data-dtime');
    document.getElementById('modalArrTime').value = btn.getAttribute('data-atime');
    document.getElementById('flightModal').style.display = 'flex';
}

function filterGatesByRoute() {
    const routeSelect = document.getElementById('modalRouteId');
    if(!routeSelect.value) return;
    const selectedOption = routeSelect.options[routeSelect.selectedIndex];
    const depAirportId = selectedOption.getAttribute('data-dep-id');
    const arrAirportId = selectedOption.getAttribute('data-arr-id');
    const depGateSelect = document.getElementById('modalDepGateId');
    const arrGateSelect = document.getElementById('modalArrGateId');
    Array.from(depGateSelect.options).forEach(opt => {
        if (opt.value === "0") return;
        const isMatch = opt.getAttribute('data-airport-id') === depAirportId;
        opt.hidden = !isMatch; opt.disabled = !isMatch;
    });
    Array.from(arrGateSelect.options).forEach(opt => {
        if (opt.value === "0") return;
        const isMatch = opt.getAttribute('data-airport-id') === arrAirportId;
        opt.hidden = !isMatch; opt.disabled = !isMatch;
    });
    if (depGateSelect.options[depGateSelect.selectedIndex]?.hidden) depGateSelect.value = "0";
    if (arrGateSelect.options[arrGateSelect.selectedIndex]?.hidden) arrGateSelect.value = "0";
}

function closeFlightModal() { 
    document.getElementById('flightModal').style.display = 'none'; 
}

function saveFlightData() {
    const flightId = document.getElementById('modalFlightId').value;
    const requestData = {
        flight_id: flightId ? parseInt(flightId) : null,
        flight_no: document.getElementById('modalFlightNo').value,
        route_id: parseInt(document.getElementById('modalRouteId').value),
        aircraft_id: parseInt(document.getElementById('modalAircraftId').value),
        departure_gate_id: parseInt(document.getElementById('modalDepGateId').value) || null,
        arrival_gate_id: parseInt(document.getElementById('modalArrGateId').value) || null,
        departure_time: document.getElementById('modalDepTime').value,
        arrival_time: document.getElementById('modalArrTime').value
    };
    if(!requestData.flight_no || !requestData.route_id || !requestData.aircraft_id || !requestData.departure_time || !requestData.arrival_time) { alert('필수 항목(*)을 모두 입력해주세요.'); return; }
    
    const url = flightId ? '/staff/schedule/api/update' : '/staff/schedule/api/insert';
    const csrfMeta = document.querySelector('meta[name="_csrf"]');
    const csrfHeaderMeta = document.querySelector('meta[name="_csrf_header"]');
    if (!csrfMeta || !csrfHeaderMeta) return;
    
    const headers = { 'Content-Type': 'application/json', 'X-Requested-With': 'XMLHttpRequest' };
    headers[csrfHeaderMeta.getAttribute('content')] = csrfMeta.getAttribute('content');
    
	fetch(url, {
	    method: 'POST',
	    headers: headers,
	    body: JSON.stringify(requestData)
	})
	.then(async res => {

	    const message = await res.text();

	    if (!res.ok) {
	        throw new Error(
	            message || '저장에 실패했습니다.'
	        );
	    }

	    return message;
	})
	.then(result => {

	    if (result === 'success') {
	        alert('저장되었습니다.');
	        location.reload();
	    }
	})
	.catch(err => {

	    console.error(err);

	    alert(
	        err.message
	        || '서버 통신 중 오류가 발생했습니다.'
	    );
	});
}

function deleteFlight(flightId) {
    if(!confirm('이 운항 스케줄을 정말 취소(삭제)하시겠습니까?')) return;
    const csrfMeta = document.querySelector('meta[name="_csrf"]');
    const csrfHeaderMeta = document.querySelector('meta[name="_csrf_header"]');
    const headers = { 'X-Requested-With': 'XMLHttpRequest' };
    headers[csrfHeaderMeta.getAttribute('content')] = csrfMeta.getAttribute('content');
    
    fetch('/staff/schedule/api/delete?flightId=' + flightId, { method: 'POST', headers: headers })
    .then(res => res.text())
    .then(result => { 
        if(result === 'success') { alert('삭제되었습니다.'); location.reload(); } 
        else { alert('삭제에 실패했습니다.'); } 
    });
}

function changeFlightStatus(flightId, newStatus) {
    if (!newStatus) return;
    let delayMins = 0;
    if (newStatus === 'DELAYED') {
        const input = prompt("지연 시간(분)을 숫자로 입력해 주세요.\n(예: 30분 지연 시 30 입력)", "0");
        if (input === null || input.trim() === '') { location.reload(); return; }
        delayMins = parseInt(input, 10);
        if (isNaN(delayMins) || delayMins < 0) { alert("올바른 지연 시간(숫자)을 입력해 주세요."); location.reload(); return; }
    } else {
        if (!confirm('해당 항공편의 상태를 정말 변경하시겠습니까?')) { location.reload(); return; }
    }
    const csrfMeta = document.querySelector('meta[name="_csrf"]');
    const csrfHeaderMeta = document.querySelector('meta[name="_csrf_header"]');
    const headers = { 'Content-Type': 'application/json', 'X-Requested-With': 'XMLHttpRequest' };
    headers[csrfHeaderMeta.getAttribute('content')] = csrfMeta.getAttribute('content');
    
    fetch('/staff/schedule/api/status', {
        method: 'POST', headers: headers,
        body: JSON.stringify({ flight_id: parseInt(flightId), flight_status: newStatus, delay_minutes: delayMins })
    })
    .then(res => res.text())
    .then(result => { 
        if (result === 'success') { alert('상태가 정상적으로 변경되었습니다.'); location.reload(); } 
        else { alert('상태 변경에 실패했습니다.'); location.reload(); } 
    });
}

/* ==================== 초기 실행 ==================== */
function initStaffPage() {
  // 지연/결항 입력 폼 초기 제어
  toggleNoticeField();
  
  // 상단 시계 구동
  const clock = $('clock');
  if (clock) {
    clock.textContent = new Date().toTimeString().slice(0, 8);
    setInterval(() => {
        clock.textContent = new Date().toTimeString().slice(0, 8);
    }, 1000);
  }
}

document.addEventListener('DOMContentLoaded', initStaffPage);