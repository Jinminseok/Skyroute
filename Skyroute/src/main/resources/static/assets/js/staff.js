/* =========================================================
   STAFF 공통 화면 제어 스크립트
   - 현재 단계: DB 연동 전 하드코딩 데이터 기반 화면 뼈대
   - 사용 화면:
     1) 오늘 항공편: schedule_today.html
     2) 운항 스케줄: schedule_list.html
     3) 탑승 관리: passenger_list.html
     4) 지연/결항: delay_list.html
     5) 운영 기준 정보: base_list.html
========================================================= */

/* ==================== 하드코딩 데이터 스토어 ==================== */
const state = {
  tickets: [
    { id: 101, flight_no: 'HJ-1207', booking_no: 'HJ7K2P', name: '홍길동', seat_no: '12A', booking_status: 'CONFIRMED', payment_status: 'PAID', checkin_status: 'BOARDED', checked_in_by: 1002 },
    { id: 102, flight_no: 'HJ-1207', booking_no: 'HJ4M9X', name: '김영희', seat_no: '12B', booking_status: 'CONFIRMED', payment_status: 'PAID', checkin_status: 'CHECKED_IN', checked_in_by: 1002 },
    { id: 103, flight_no: 'HJ-1207', booking_no: 'HJ2B5C', name: '이철수', seat_no: '14C', booking_status: 'CONFIRMED', payment_status: 'PAID', checkin_status: 'NOT_CHECKED_IN', checked_in_by: null },
    { id: 104, flight_no: 'HJ-8821', booking_no: 'NK381A', name: '박민수', seat_no: '02B', booking_status: 'CONFIRMED', payment_status: 'PAID', checkin_status: 'NOT_CHECKED_IN', checked_in_by: null }
  ],
  base: {
    airport: [
      { id: 1, iata_code: 'GMP', name: '서울 김포공항', country: '대한민국', timezone: 'Asia/Seoul', region_id: 1, flight_type: 'DOM', is_active: 'Y' },
      { id: 2, iata_code: 'NRT', name: '도쿄 나리타공항', country: '일본', timezone: 'Asia/Tokyo', region_id: 2, flight_type: 'INT', is_active: 'Y' },
      { id: 3, iata_code: 'CJU', name: '제주공항', country: '대한민국', timezone: 'Asia/Seoul', region_id: 1, flight_type: 'DOM', is_active: 'Y' }
    ],
    gate: [
      { id: 1, airport_id: 1, gate_code: 'G11', gate_area_id: 101, flight_type: 'DOM', is_active: 'Y' },
      { id: 2, airport_id: 2, gate_code: 'A23', gate_area_id: 204, flight_type: 'INT', is_active: 'Y' }
    ],
    route: [
      { id: 1, departure_airport_id: 1, arrival_airport_id: 3, flight_type: 'DOM', route_type_id: 11, is_active: 'Y' },
      { id: 2, departure_airport_id: 1, arrival_airport_id: 2, flight_type: 'INT', route_type_id: 12, is_active: 'Y' }
    ],
    aircraft: [
      { id: 1, no: 'HL7231', model_name: 'A320-Neo', total_seats: 180, aircraft_status_id: 1, is_active: 'Y' },
      { id: 2, no: 'HL8842', model_name: 'B737-Max8', total_seats: 162, aircraft_status_id: 1, is_active: 'Y' }
    ],
    seat: [
      { id: 1, aircraft_id: 1, seat_no: '1A', seat_class_id: 2, is_active: 'Y' },
      { id: 2, aircraft_id: 1, seat_no: '1B', seat_class_id: 2, is_active: 'Y' },
      { id: 3, aircraft_id: 1, seat_no: '12A', seat_class_id: 1, is_active: 'Y' }
    ],
    fare: [
      { id: 1, route_id: 1, seat_class_id: 1, season_id: 1, price: 145000, is_active: 'Y' }
    ]
  }
};

/* ==================== 상태값 매핑 ==================== */
const mapper = {
  flightStatus: {
    SCHEDULED: { text: '운항예정', cls: 'badge-scheduled' },
    BOARDING: { text: '탑승중', cls: 'badge-boarding' },
    DEPARTED: { text: '출발완료', cls: 'badge-departed' },
    ARRIVED: { text: '도착완료', cls: 'badge-arrived' },
    DELAYED: { text: '지연', cls: 'badge-delayed' },
    CANCELLED: { text: '결항', cls: 'badge-cancelled' },
    COMPLETED: { text: '운항종료', cls: 'badge-completed' }
  },
  paxStatus: {
    NOT_CHECKED_IN: { text: '체크인 대기', cls: 'badge-muted' },
    CHECKED_IN: { text: '체크인 완료', cls: 'badge-info' },
    BOARDED: { text: '탑승 완료', cls: 'badge-ok' },
    NO_SHOW: { text: '미탑승(NO_SHOW)', cls: 'badge-cancelled' }
  }
};

/* ==================== 공통 유틸 ==================== */
function $(id) { return document.getElementById(id); }

function setHtml(id, html) {
  const target = $(id);
  if (!target) return;
  target.innerHTML = html;
}

function getFlightStatusMeta(status) { return mapper.flightStatus[status] || { text: status || '-', cls: 'badge-muted' }; }
function getPaxStatusMeta(status) { return mapper.paxStatus[status] || { text: status || '-', cls: 'badge-muted' }; }

function toTime(datetimeText) {
  if (!datetimeText) return '-';
  const parts = datetimeText.split(' ');
  return parts.length > 1 ? parts[1] : datetimeText;
}

function shortRouteName(routeName) {
  if (!routeName) return '-';
  const matched = routeName.match(/\((.*?)\).*?\((.*?)\)/);
  if (matched && matched.length >= 3) { return `${matched[1]} → ${matched[2]}`; }
  return routeName;
}

function getAirportCode(airportId) {
  const airport = state.base.airport.find(item => item.id === Number(airportId));
  return airport ? airport.iata_code : `공항${airportId}`;
}

function getRouteName(routeId) {
  const route = state.base.route.find(item => item.id === Number(routeId));
  if (!route) return `노선 ${routeId}`;
  const dep = getAirportCode(route.departure_airport_id);
  const arr = getAirportCode(route.arrival_airport_id);
  return `${dep} → ${arr}`;
}

function getAircraftModel(aircraftId) {
  const aircraft = state.base.aircraft.find(item => item.id === Number(aircraftId));
  return aircraft ? aircraft.model_name : `기재 ${aircraftId}`;
}

function nextId(list) {
  if (!Array.isArray(list) || list.length === 0) return 1;
  return Math.max(...list.map(item => Number(item.id) || 0)) + 1;
}

function nowText() {
  return new Date().toISOString().replace('T', ' ').slice(0, 16);
}

/* ==================== 3. 승객/체크인/탑승/노쇼 ==================== */
function loadPaxData() {
  const selector = $('paxFlightSelector');
  const container = $('paxListTable');
  if (!selector || !container) return;

  const selectedFlight = selector.value;
  const filteredTickets = state.tickets.filter(ticket => ticket.flight_no === selectedFlight);

  const summary = $('paxSummaryLabel');
  if (summary) { summary.textContent = `${selectedFlight} 항공편 수속 확정자: 총 ${filteredTickets.length}명`; }

  container.innerHTML = filteredTickets.map(ticket => {
    const paxStatus = getPaxStatusMeta(ticket.checkin_status);
    const checkinDisabled = ticket.checkin_status !== 'NOT_CHECKED_IN' ? 'disabled' : '';
    const boardingDisabled = ticket.checkin_status !== 'CHECKED_IN' ? 'disabled' : '';
    const noShowDisabled = ticket.checkin_status !== 'NOT_CHECKED_IN' ? 'disabled' : '';

    return `
      <tr>
        <td class="mono">${ticket.booking_no}</td>
        <td><strong>${ticket.name}</strong></td>
        <td class="mono">${ticket.seat_no}</td>
        <td><span class="badge badge-scheduled">${ticket.booking_status}</span></td>
        <td><span class="badge badge-ok">${ticket.payment_status}</span></td>
        <td><span class="badge ${paxStatus.cls}">${paxStatus.text}</span></td>
        <td>${ticket.checked_in_by ? `STAFF_${ticket.checked_in_by}` : '<span style="color:var(--text-light)">-</span>'}</td>
        <td>
          <div class="btn-action-group">
            <button class="btn btn-ghost btn-sm" ${checkinDisabled} onclick="updatePaxStatus(${ticket.id}, 'CHECKED_IN')">체크인 완료</button>
            <button class="btn btn-primary btn-sm" ${boardingDisabled} onclick="updatePaxStatus(${ticket.id}, 'BOARDED')">탑승 확정</button>
            <button class="btn btn-danger btn-sm" ${noShowDisabled} onclick="updatePaxStatus(${ticket.id}, 'NO_SHOW')">노쇼 처리</button>
          </div>
        </td>
      </tr>
    `;
  }).join('');
}

function updatePaxStatus(ticketId, nextStatus) {
  const ticket = state.tickets.find(item => item.id === Number(ticketId));
  if (!ticket) { toast('대상 승객 정보를 찾을 수 없습니다.'); return; }
  if (nextStatus === 'CHECKED_IN' && ticket.checkin_status !== 'NOT_CHECKED_IN') { toast('체크인 대기 상태인 승객만 체크인 처리할 수 있습니다.'); return; }
  if (nextStatus === 'BOARDED' && ticket.checkin_status !== 'CHECKED_IN') { toast('체크인 완료 승객만 탑승 확정 처리할 수 있습니다.'); return; }
  if (nextStatus === 'NO_SHOW' && ticket.checkin_status !== 'NOT_CHECKED_IN') { toast('체크인 전 승객만 노쇼 처리할 수 있습니다.'); return; }

  ticket.checkin_status = nextStatus;
  if (nextStatus === 'CHECKED_IN') { ticket.checked_in_by = 1002; }
  toast(`${ticket.name} 승객의 수속 상태가 변경되었습니다.`);
  loadPaxData();
}

// =========================================================
//                       4. 지연/결항 안내
// =========================================================

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

/* ==================== 5. 운영 기준 정보 ==================== */
function renderBaseTables() {
  setHtml('baseAirportTable', state.base.airport.map(airport => `
    <tr>
      <td class="mono">${airport.id}</td>
      <td class="mono">${airport.iata_code}</td>
      <td><strong>${airport.name}</strong></td>
      <td>${airport.country}</td>
      <td class="mono">${airport.timezone}</td>
      <td class="mono">${airport.region_id}</td>
      <td><span class="badge badge-scheduled">${airport.flight_type}</span></td>
      <td><div class="toggle ${airport.is_active === 'Y' ? 'on' : ''}" onclick="toggleActive('airport', ${airport.id})"></div></td>
      <td><button class="btn btn-ghost btn-sm" onclick="openBaseModal('airport', '수정', ${airport.id})"><span class="material-symbols-outlined" style="font-size:14px">edit</span>수정</button></td>
    </tr>
  `).join(''));

  setHtml('baseGateTable', state.base.gate.map(gate => `
    <tr>
      <td class="mono">${gate.id}</td>
      <td class="mono">공항 ID: ${gate.airport_id}</td>
      <td class="mono"><strong>${gate.gate_code}</strong></td>
      <td class="mono">${gate.gate_area_id}구역</td>
      <td><span class="badge badge-scheduled">${gate.flight_type}</span></td>
      <td><div class="toggle ${gate.is_active === 'Y' ? 'on' : ''}" onclick="toggleActive('gate', ${gate.id})"></div></td>
      <td><button class="btn btn-ghost btn-sm" onclick="openBaseModal('gate', '수정', ${gate.id})"><span class="material-symbols-outlined" style="font-size:14px">edit</span>수정</button></td>
    </tr>
  `).join(''));

  setHtml('baseRouteTable', state.base.route.map(route => `
    <tr>
      <td class="mono">${route.id}</td>
      <td class="mono">출발: ${route.departure_airport_id}</td>
      <td class="mono">도착: ${route.arrival_airport_id}</td>
      <td><span class="badge badge-scheduled">${route.flight_type}</span></td>
      <td class="mono">유형: ${route.route_type_id}</td>
      <td><div class="toggle ${route.is_active === 'Y' ? 'on' : ''}" onclick="toggleActive('route', ${route.id})"></div></td>
      <td><button class="btn btn-ghost btn-sm" onclick="openBaseModal('route', '수정', ${route.id})"><span class="material-symbols-outlined" style="font-size:14px">edit</span>수정</button></td>
    </tr>
  `).join(''));

  setHtml('baseAircraftTable', state.base.aircraft.map(aircraft => `
    <tr>
      <td class="mono">${aircraft.id}</td>
      <td class="mono"><strong>${aircraft.no}</strong></td>
      <td>${aircraft.model_name}</td>
      <td class="mono">${aircraft.total_seats}석</td>
      <td class="mono">상태코드 ${aircraft.aircraft_status_id}</td>
      <td><div class="toggle ${aircraft.is_active === 'Y' ? 'on' : ''}" onclick="toggleActive('aircraft', ${aircraft.id})"></div></td>
      <td><button class="btn btn-ghost btn-sm" onclick="openBaseModal('aircraft', '수정', ${aircraft.id})"><span class="material-symbols-outlined" style="font-size:14px">edit</span>수정</button></td>
    </tr>
  `).join(''));

  setHtml('baseSeatTable', state.base.seat.map(seat => `
    <tr>
      <td class="mono">${seat.id}</td>
      <td class="mono">기재 ID: ${seat.aircraft_id}</td>
      <td class="mono"><strong>${seat.seat_no}</strong></td>
      <td class="mono">등급 ID: ${seat.seat_class_id}</td>
      <td><div class="toggle ${seat.is_active === 'Y' ? 'on' : ''}" onclick="toggleActive('seat', ${seat.id})"></div></td>
      <td><button class="btn btn-ghost btn-sm" onclick="openBaseModal('seat', '수정', ${seat.id})"><span class="material-symbols-outlined" style="font-size:14px">edit</span>수정</button></td>
    </tr>
  `).join(''));

  setHtml('baseFareTable', state.base.fare.map(fare => `
    <tr>
      <td class="mono">${fare.id}</td>
      <td class="mono">노선 ID: ${fare.route_id}</td>
      <td class="mono">등급 ID: ${fare.seat_class_id}</td>
      <td class="mono">시즌 ID: ${fare.season_id}</td>
      <td class="mono" style="font-weight:700; color:var(--text-dark)">${fare.price.toLocaleString()} 원</td>
      <td><div class="toggle ${fare.is_active === 'Y' ? 'on' : ''}" onclick="toggleActive('fare', ${fare.id})"></div></td>
      <td><button class="btn btn-ghost btn-sm" onclick="openBaseModal('fare', '수정', ${fare.id})"><span class="material-symbols-outlined" style="font-size:14px">edit</span>수정</button></td>
    </tr>
  `).join(''));
}

function toggleActive(table, id) {
  const list = state.base[table];
  if (!list) return;
  const item = list.find(row => row.id === Number(id));
  if (!item) return;
  item.is_active = item.is_active === 'Y' ? 'N' : 'Y';
  toast(`${table.toUpperCase()} ID ${id}의 사용 여부가 변경되었습니다.`);
  renderBaseTables();
}

function openBaseModal(table, action, id = null) {
  const list = state.base[table];
  if (!list) { toast('지원하지 않는 기준정보입니다.'); return; }
  const item = action === '수정' ? list.find(row => row.id === Number(id)) : null;
  if (action === '수정' && !item) { toast('수정할 데이터를 찾을 수 없습니다.'); return; }
  activeContext = { type: `base_${table}`, action, id: id ? Number(id) : null };
  setHtml('globalModalTitle', `운영 기준 정보 ${table.toUpperCase()} ${action}`);
  let html = '';
  setHtml('globalModalBody', html);
  openModal();
}

/* ==================== 통합 모달 저장 ==================== */
function saveGlobalModalData() {
  const ctx = activeContext;
  if (ctx.type && ctx.type.startsWith('base_')) { saveBaseFromModal(ctx); closeModal(); return; }
  if (ctx.type === 'chat') { saveChatReply(ctx); closeModal(); return; }
  toast('저장할 작업 정보를 찾을 수 없습니다.');
}

function saveBaseFromModal(ctx) {
  const table = ctx.type.replace('base_', '');
  const list = state.base[table];
  if (!list) { toast('기준정보 저장 대상을 찾을 수 없습니다.'); return; }
  if (ctx.action === '등록' || ctx.action === '일괄등록') { insertBaseItem(table, list, ctx.action); } 
  else { updateBaseItem(table, list, ctx.id); }
  renderBaseTables();
}

function insertBaseItem(table, list, action) {
  const id = nextId(list);
  if (table === 'airport') { list.push({ id, iata_code: $('m_b_air_code').value.trim().toUpperCase(), name: $('m_b_air_name').value.trim(), country: $('m_b_air_country').value.trim(), timezone: $('m_b_air_tz').value.trim(), region_id: Number($('m_b_air_reg').value), flight_type: $('m_b_air_type').value.trim().toUpperCase(), is_active: 'Y' }); }
  if (table === 'gate') { list.push({ id, airport_id: Number($('m_b_g_pid').value), gate_code: $('m_b_g_code').value.trim().toUpperCase(), gate_area_id: Number($('m_b_g_area').value), flight_type: $('m_b_g_type').value.trim().toUpperCase(), is_active: 'Y' }); }
  if (table === 'route') { list.push({ id, departure_airport_id: Number($('m_b_r_dep').value), arrival_airport_id: Number($('m_b_r_arr').value), flight_type: $('m_b_r_type').value.trim().toUpperCase(), route_type_id: Number($('m_b_r_tid').value), is_active: 'Y' }); }
  if (table === 'aircraft') { list.push({ id, no: $('m_b_ac_no').value.trim().toUpperCase(), model_name: $('m_b_ac_model').value.trim(), total_seats: Number($('m_b_ac_seats').value), aircraft_status_id: Number($('m_b_ac_status').value), is_active: 'Y' }); }
  toast('운영 기준 정보가 등록되었습니다.');
}

function updateBaseItem(table, list, id) {
  const item = list.find(row => row.id === Number(id));
  if (!item) { toast('수정할 기준정보를 찾을 수 없습니다.'); return; }
  if (table === 'airport') { item.name = $('m_b_air_name').value.trim(); item.country = $('m_b_air_country').value.trim(); item.timezone = $('m_b_air_tz').value.trim(); item.region_id = Number($('m_b_air_reg').value); item.flight_type = $('m_b_air_type').value.trim().toUpperCase(); }
  if (table === 'gate') { item.gate_area_id = Number($('m_b_g_area').value); item.flight_type = $('m_b_g_type').value.trim().toUpperCase(); }
  if (table === 'route') { item.flight_type = $('m_b_r_type').value.trim().toUpperCase(); item.route_type_id = Number($('m_b_r_tid').value); }
  if (table === 'aircraft') { item.model_name = $('m_b_ac_model').value.trim(); item.total_seats = Number($('m_b_ac_seats').value); item.aircraft_status_id = Number($('m_b_ac_status').value); }
  if (table === 'seat') { item.seat_class_id = Number($('m_b_s_class').value); }
  if (table === 'fare') { item.price = Number($('m_b_f_price').value); }
  toast('운영 기준 정보가 수정되었습니다.');
}

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

/* ==================== 토스트 ==================== */
function toast(message) {
  const wrap = $('toasts');
  if (!wrap) { console.log(message); return; }
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

/* ==================== 초기 실행 ==================== */
function initStaffPage() {
  loadPaxData();
  renderBaseTables();
  toggleNoticeField();
  
  if (typeof renderCmsTables === 'function') renderCmsTables();
  if (typeof renderInquiryList === 'function') renderInquiryList();

  const clock = $('clock');
  if (clock) {
    clock.textContent = new Date().toTimeString().slice(0, 8);
  }
}

setInterval(() => {
  const clock = $('clock');
  if (clock) { clock.textContent = new Date().toTimeString().slice(0, 8); }
}, 1000);

document.addEventListener('DOMContentLoaded', initStaffPage);

/* =========================================================
   고객 문의 및 공지사항 관련 하드코딩 추가 영역
========================================================= */

// 고객 문의 데이터
state.inquiries = [
  { id: 3021, member_id: 401, name: '홍길동', staff_id: 1002, status: 'IN_PROGRESS', created_at: '2026-06-25 10:24' },
  { id: 3018, member_id: 405, name: '김영희', staff_id: 1001, status: 'RESOLVED', created_at: '2026-06-25 09:50' }
];

state.chatMessages = {
  3021: [
    { sender: 'USER', message: '항공권 예약 변경 방법을 알려주세요.', sent_at: '2026-06-25 10:24' },
    { sender: 'STAFF', message: '예약 변경은 마이페이지 예약 상세에서 가능하며, 운임 규정에 따라 수수료가 발생할 수 있습니다.', sent_at: '2026-06-25 10:27' }
  ],
  3018: [
    { sender: 'USER', message: '결항 항공편 환불은 자동으로 처리되나요?', sent_at: '2026-06-25 09:50' },
    { sender: 'STAFF', message: '결항 항공편은 전액 자동 환불 대상으로 처리됩니다.', sent_at: '2026-06-25 09:55' }
  ]
};

mapper.inquiryStatus = {
  IN_PROGRESS: { text: '처리중', cls: 'badge-warn' },
  RESOLVED: { text: '해결 완료', cls: 'badge-ok' }
};

function renderInquiryList() {
  const container = document.getElementById('inquiryTableBody');
  if (!container) return;
  container.innerHTML = state.inquiries.map(inquiry => {
    const status = mapper.inquiryStatus[inquiry.status] || { text: inquiry.status, cls: 'badge-muted' };
    return `
      <tr>
        <td class="mono">${inquiry.id}</td>
        <td><strong>${inquiry.name}</strong> <span style="color:var(--text-light); font-size:12px;">(회원 ID: ${inquiry.member_id})</span></td>
        <td class="mono">STAFF_${inquiry.staff_id}</td>
        <td><span class="badge ${status.cls}">${status.text}</span></td>
        <td class="mono">${inquiry.created_at}</td>
        <td>
          <div class="btn-action-group">
            <button type="button" class="btn btn-ghost btn-sm" onclick="openChatModal(${inquiry.id})"><span class="material-symbols-outlined" style="font-size:14px">chat</span> 1:1 채팅 답변</button>
            <button type="button" class="btn btn-primary btn-sm" onclick="toggleInquiryStatus(${inquiry.id})">상태 변경</button>
          </div>
        </td>
      </tr>
    `;
  }).join('');
}

function toggleInquiryStatus(id) {
  const inquiry = state.inquiries.find(item => item.id === Number(id));
  if (!inquiry) return;
  inquiry.status = inquiry.status === 'IN_PROGRESS' ? 'RESOLVED' : 'IN_PROGRESS';
  toast(`문의 ID ${id}의 처리 상태가 변경되었습니다.`);
  renderInquiryList();
}

function openChatModal(id) {
  const inquiry = state.inquiries.find(item => item.id === Number(id));
  if (!inquiry) return;
  activeContext = { type: 'chat', action: '답변', id: Number(id) };
  const messages = state.chatMessages[id] || [];
  const messageHtml = messages.map(message => {
    const isStaff = message.sender === 'STAFF';
    return `
      <div style="margin-bottom:10px; text-align:${isStaff ? 'right' : 'left'};">
        <div style="display:inline-block; max-width:80%; padding:10px 12px; border-radius:10px; background:${isStaff ? 'rgba(2, 132, 199, 0.12)' : '#F1F5F9'}; color:var(--text-dark); font-size:13px; text-align:left;">
          <div style="font-weight:700; margin-bottom:4px; color:${isStaff ? 'var(--primary)' : 'var(--text-gray)'};">${isStaff ? '지상직' : '고객'}</div>
          <div>${message.message}</div>
          <div class="mono" style="font-size:11px; color:var(--text-light); margin-top:6px;">${message.sent_at}</div>
        </div>
      </div>
    `;
  }).join('');

  setHtml('globalModalTitle', `1:1 채팅 답변 - ${inquiry.name} 고객`);
  setHtml('globalModalBody', `
    <div style="background:#FFFFFF; border:1px solid var(--border); border-radius:8px; padding:14px; height:260px; overflow-y:auto; margin-bottom:16px;">${messageHtml}</div>
    <div class="field full"><label style="font-size:12px; font-weight:600; color:var(--text-gray); display:block; margin-bottom:6px;">지상직 공식 답변 메시지</label><textarea class="textarea" id="chatReplyMsg" rows="4" placeholder="고객에게 전송할 답변 내용을 입력하세요."></textarea></div>
  `);
  openModal();
}

function saveChatReply(ctx) {
  const inquiryId = Number(ctx.id);
  const input = document.getElementById('chatReplyMsg');
  if (!input) return;
  const message = input.value.trim();
  if (!message) { alert('답변 메시지를 입력해야 합니다.'); return; }
  if (!state.chatMessages[inquiryId]) state.chatMessages[inquiryId] = [];
  state.chatMessages[inquiryId].push({ sender: 'STAFF', message, sent_at: nowText() });
  const inquiry = state.inquiries.find(item => item.id === inquiryId);
  if (inquiry) inquiry.status = 'IN_PROGRESS';
  toast('지상직 답변 메시지가 저장되었습니다.');
  renderInquiryList();
}

// 콘텐츠 데이터
state.cms = {
  notice: [ { id: 12, title: '하계 운항 정기 스케줄 변경 안내', content: '하계 기간 노선 확충에 따른 타임테이블 변경 내역입니다.', is_public: 'Y', created_by: 1002, created_at: '2026-06-10 10:00' } ],
  event: [ { id: 1, title: '제주 노선 7월 얼리버드 특가', content: '7월 평일 출발 승객 대상 특별 운임 프로모션', start_date: '2026-07-01', end_date: '2026-07-15', image_url: 'img/jeju_event.png', is_visible: 'Y', is_ended: 'N' } ],
  faq: [ { id: 1, category: '예약/결제', question: '예약 변경은 어디에서 하나요?', answer: '마이페이지 예약 상세 화면에서 예약 변경 가능 여부를 확인할 수 있습니다.', is_visible: 'Y' } ]
};

function renderCmsTables() {
  const noticeTable = document.getElementById('noticeContentTable');
  if (noticeTable) {
    noticeTable.innerHTML = state.cms.notice.map(notice => `
      <tr><td class="mono">${notice.id}</td><td><strong>${notice.title}</strong></td><td style="max-width:260px; overflow:hidden; text-overflow:ellipsis; white-space:nowrap;">${notice.content}</td><td><div class="toggle ${notice.is_public === 'Y' ? 'on' : ''}" onclick="toggleCmsFlag('notice', ${notice.id}, 'is_public')"></div></td><td class="mono">STAFF_${notice.created_by}</td><td class="mono">${notice.created_at}</td><td><div class="btn-action-group"><button type="button" class="btn btn-ghost btn-sm" onclick="openContentModal('notice', '수정', ${notice.id})">수정</button><button type="button" class="btn btn-danger btn-sm" onclick="deleteCmsItem('notice', ${notice.id})">삭제</button></div></td></tr>
    `).join('');
  }
}

// =========================================================
// 스케줄 공통 통신 (오늘 항공편 등)
// =========================================================
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
function closeFlightModal() { document.getElementById('flightModal').style.display = 'none'; }
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
    fetch(url, { method: 'POST', headers: headers, body: JSON.stringify(requestData) })
    .then(res => res.text())
    .then(result => { if(result === 'success') { alert('저장되었습니다.'); location.reload(); } else { alert('저장에 실패했습니다.'); } })
    .catch(err => { console.error(err); alert('서버 통신 중 오류가 발생했습니다.'); });
}
function deleteFlight(flightId) {
    if(!confirm('이 운항 스케줄을 정말 취소(삭제)하시겠습니까?')) return;
    const csrfMeta = document.querySelector('meta[name="_csrf"]');
    const csrfHeaderMeta = document.querySelector('meta[name="_csrf_header"]');
    const headers = { 'X-Requested-With': 'XMLHttpRequest' };
    headers[csrfHeaderMeta.getAttribute('content')] = csrfMeta.getAttribute('content');
    fetch('/staff/schedule/api/delete?flightId=' + flightId, { method: 'POST', headers: headers })
    .then(res => res.text())
    .then(result => { if(result === 'success') { alert('삭제되었습니다.'); location.reload(); } else { alert('삭제에 실패했습니다.'); } });
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
    .then(result => { if (result === 'success') { alert('상태가 정상적으로 변경되었습니다.'); location.reload(); } else { alert('상태 변경에 실패했습니다.'); location.reload(); } });
}