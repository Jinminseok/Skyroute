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
  /*flights: [
    {
      id: 1,
      flight_no: 'HJ-1207',
      route_id: 1,
      route_name: '서울김포(GMP) → 제주공항(CJU)',
      aircraft_id: 1,
      aircraft_model: 'A320',
      departure_gate_id: 11,
      arrival_gate_id: 3,
      departure_time: '2026-06-25 08:20',
      arrival_time: '2026-06-25 09:35',
      flight_status: 'BOARDING',
      delay_minutes: 0,
      is_deleted: 'N'
    },
    {
      id: 2,
      flight_no: 'HJ-8821',
      route_id: 2,
      route_name: '인천공항(ICN) → 도쿄나리타(NRT)',
      aircraft_id: 2,
      aircraft_model: 'B737',
      departure_gate_id: 23,
      arrival_gate_id: 1,
      departure_time: '2026-06-25 10:15',
      arrival_time: '2026-06-25 12:40',
      flight_status: 'SCHEDULED',
      delay_minutes: 0,
      is_deleted: 'N'
    },
    {
      id: 3,
      flight_no: 'HJ-3340',
      route_id: 3,
      route_name: '김해공항(PUS) → 제주공항(CJU)',
      aircraft_id: 1,
      aircraft_model: 'A320',
      departure_gate_id: 7,
      arrival_gate_id: 5,
      departure_time: '2026-06-25 14:40',
      arrival_time: '2026-06-25 15:55',
      flight_status: 'SCHEDULED',
      delay_minutes: 0,
      is_deleted: 'N'
    },
    {
      id: 4,
      flight_no: 'HJ-5012',
      route_id: 4,
      route_name: '서울김포(GMP) → 김해공항(PUS)',
      aircraft_id: 2,
      aircraft_model: 'B737',
      departure_gate_id: 5,
      arrival_gate_id: 2,
      departure_time: '2026-06-25 16:50',
      arrival_time: '2026-06-25 17:55',
      flight_status: 'CANCELLED',
      delay_minutes: 0,
      is_deleted: 'N'
    }
  ],*/

  tickets: [
    {
      id: 101,
      flight_no: 'HJ-1207',
      booking_no: 'HJ7K2P',
      name: '홍길동',
      seat_no: '12A',
      booking_status: 'CONFIRMED',
      payment_status: 'PAID',
      checkin_status: 'BOARDED',
      checked_in_by: 1002
    },
    {
      id: 102,
      flight_no: 'HJ-1207',
      booking_no: 'HJ4M9X',
      name: '김영희',
      seat_no: '12B',
      booking_status: 'CONFIRMED',
      payment_status: 'PAID',
      checkin_status: 'CHECKED_IN',
      checked_in_by: 1002
    },
    {
      id: 103,
      flight_no: 'HJ-1207',
      booking_no: 'HJ2B5C',
      name: '이철수',
      seat_no: '14C',
      booking_status: 'CONFIRMED',
      payment_status: 'PAID',
      checkin_status: 'NOT_CHECKED_IN',
      checked_in_by: null
    },
    {
      id: 104,
      flight_no: 'HJ-8821',
      booking_no: 'NK381A',
      name: '박민수',
      seat_no: '02B',
      booking_status: 'CONFIRMED',
      payment_status: 'PAID',
      checkin_status: 'NOT_CHECKED_IN',
      checked_in_by: null
    }
  ],

  notices: [
    {
      id: 1,
      flight_no: 'HJ-8821',
      notice_type: 'DELAY',
      delay_minutes: 40,
      reason: '기상 악화 및 동풍 시정 저하로 인한 순연',
      created_by: 1002,
      created_at: '2026-06-25 09:12'
    },
    {
      id: 2,
      flight_no: 'HJ-5012',
      notice_type: 'CANCEL',
      delay_minutes: 0,
      reason: '항공기 기재 정비(엔진 압력 계통 고장 보수)',
      created_by: 1001,
      created_at: '2026-06-25 08:40'
    }
  ],

  base: {
    airport: [
      {
        id: 1,
        iata_code: 'GMP',
        name: '서울 김포공항',
        country: '대한민국',
        timezone: 'Asia/Seoul',
        region_id: 1,
        flight_type: 'DOM',
        is_active: 'Y'
      },
      {
        id: 2,
        iata_code: 'NRT',
        name: '도쿄 나리타공항',
        country: '일본',
        timezone: 'Asia/Tokyo',
        region_id: 2,
        flight_type: 'INT',
        is_active: 'Y'
      },
      {
        id: 3,
        iata_code: 'CJU',
        name: '제주공항',
        country: '대한민국',
        timezone: 'Asia/Seoul',
        region_id: 1,
        flight_type: 'DOM',
        is_active: 'Y'
      }
    ],

    gate: [
      {
        id: 1,
        airport_id: 1,
        gate_code: 'G11',
        gate_area_id: 101,
        flight_type: 'DOM',
        is_active: 'Y'
      },
      {
        id: 2,
        airport_id: 2,
        gate_code: 'A23',
        gate_area_id: 204,
        flight_type: 'INT',
        is_active: 'Y'
      }
    ],

    route: [
      {
        id: 1,
        departure_airport_id: 1,
        arrival_airport_id: 3,
        flight_type: 'DOM',
        route_type_id: 11,
        is_active: 'Y'
      },
      {
        id: 2,
        departure_airport_id: 1,
        arrival_airport_id: 2,
        flight_type: 'INT',
        route_type_id: 12,
        is_active: 'Y'
      }
    ],

    aircraft: [
      {
        id: 1,
        no: 'HL7231',
        model_name: 'A320-Neo',
        total_seats: 180,
        aircraft_status_id: 1,
        is_active: 'Y'
      },
      {
        id: 2,
        no: 'HL8842',
        model_name: 'B737-Max8',
        total_seats: 162,
        aircraft_status_id: 1,
        is_active: 'Y'
      }
    ],

    seat: [
      {
        id: 1,
        aircraft_id: 1,
        seat_no: '1A',
        seat_class_id: 2,
        is_active: 'Y'
      },
      {
        id: 2,
        aircraft_id: 1,
        seat_no: '1B',
        seat_class_id: 2,
        is_active: 'Y'
      },
      {
        id: 3,
        aircraft_id: 1,
        seat_no: '12A',
        seat_class_id: 1,
        is_active: 'Y'
      }
    ],

    fare: [
      {
        id: 1,
        route_id: 1,
        seat_class_id: 1,
        season_id: 1,
        price: 145000,
        is_active: 'Y'
      }
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
function $(id) {
  return document.getElementById(id);
}

function setHtml(id, html) {
  const target = $(id);
  if (!target) return;
  target.innerHTML = html;
}

function getFlightStatusMeta(status) {
  return mapper.flightStatus[status] || {
    text: status || '-',
    cls: 'badge-muted'
  };
}

function getPaxStatusMeta(status) {
  return mapper.paxStatus[status] || {
    text: status || '-',
    cls: 'badge-muted'
  };
}

function toTime(datetimeText) {
  if (!datetimeText) return '-';
  const parts = datetimeText.split(' ');
  return parts.length > 1 ? parts[1] : datetimeText;
}

function shortRouteName(routeName) {
  if (!routeName) return '-';

  const matched = routeName.match(/\((.*?)\).*?\((.*?)\)/);
  if (matched && matched.length >= 3) {
    return `${matched[1]} → ${matched[2]}`;
  }

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
  if (summary) {
    summary.textContent = `${selectedFlight} 항공편 수속 확정자: 총 ${filteredTickets.length}명`;
  }

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
        <td>
          <span class="badge badge-scheduled">${ticket.booking_status}</span>
        </td>
        <td>
          <span class="badge badge-ok">${ticket.payment_status}</span>
        </td>
        <td>
          <span class="badge ${paxStatus.cls}">${paxStatus.text}</span>
        </td>
        <td>
          ${ticket.checked_in_by ? `STAFF_${ticket.checked_in_by}` : '<span style="color:var(--text-light)">-</span>'}
        </td>
        <td>
          <div class="btn-action-group">
            <button class="btn btn-ghost btn-sm" ${checkinDisabled} onclick="updatePaxStatus(${ticket.id}, 'CHECKED_IN')">
              체크인 완료
            </button>
            <button class="btn btn-primary btn-sm" ${boardingDisabled} onclick="updatePaxStatus(${ticket.id}, 'BOARDED')">
              탑승 확정
            </button>
            <button class="btn btn-danger btn-sm" ${noShowDisabled} onclick="updatePaxStatus(${ticket.id}, 'NO_SHOW')">
              노쇼 처리
            </button>
          </div>
        </td>
      </tr>
    `;
  }).join('');
}

function updatePaxStatus(ticketId, nextStatus) {
  const ticket = state.tickets.find(item => item.id === Number(ticketId));
  if (!ticket) {
    toast('대상 승객 정보를 찾을 수 없습니다.');
    return;
  }

  if (nextStatus === 'CHECKED_IN' && ticket.checkin_status !== 'NOT_CHECKED_IN') {
    toast('체크인 대기 상태인 승객만 체크인 처리할 수 있습니다.');
    return;
  }

  if (nextStatus === 'BOARDED' && ticket.checkin_status !== 'CHECKED_IN') {
    toast('체크인 완료 승객만 탑승 확정 처리할 수 있습니다.');
    return;
  }

  if (nextStatus === 'NO_SHOW' && ticket.checkin_status !== 'NOT_CHECKED_IN') {
    toast('체크인 전 승객만 노쇼 처리할 수 있습니다.');
    return;
  }

  ticket.checkin_status = nextStatus;

  if (nextStatus === 'CHECKED_IN') {
    ticket.checked_in_by = 1002;
  }

  toast(`${ticket.name} 승객의 수속 상태가 변경되었습니다.`);

  loadPaxData();
}

/* ==================== 4. 지연/결항 안내 ==================== */
function renderNoticeHistory() {
  const container = $('noticeHistoryTable');
  if (!container) return;

  container.innerHTML = state.notices.map(notice => {
    const isDelay = notice.notice_type === 'DELAY';

    return `
      <tr>
        <td class="mono">${notice.id}</td>
        <td class="mono">${notice.flight_no}</td>
        <td>
          <span class="badge ${isDelay ? 'badge-delayed' : 'badge-cancelled'}">
            ${isDelay ? '지연' : '결항'}
          </span>
        </td>
        <td class="mono">${notice.delay_minutes ? `${notice.delay_minutes}분` : '-'}</td>
        <td style="max-width:220px; overflow:hidden; text-overflow:ellipsis; white-space:nowrap;">
          ${notice.reason}
        </td>
        <td class="mono">${notice.created_by}</td>
        <td class="mono">${notice.created_at}</td>
      </tr>
    `;
  }).join('');
}

function toggleNoticeField() {
  const typeSelect = $('noticeType');
  const delayWrapper = $('noticeDelayWrapper');

  if (!typeSelect || !delayWrapper) return;

  delayWrapper.style.display = typeSelect.value === 'DELAY' ? 'block' : 'none';
}

function submitFlightNotice() {
  const flightIdEl = $('noticeFlightId');
  const noticeTypeEl = $('noticeType');
  const delayMinutesEl = $('noticeDelayMinutes');
  const reasonEl = $('noticeReason');

  if (!flightIdEl || !noticeTypeEl || !reasonEl) return;

  const flightId = Number(flightIdEl.value);
  const noticeType = noticeTypeEl.value;
  const delayMinutes = Number(delayMinutesEl ? delayMinutesEl.value : 0) || 0;
  const reason = reasonEl.value.trim();

  if (!reason) {
    alert('상세 발생 사유를 입력해야 합니다.');
    reasonEl.focus();
    return;
  }

  if (noticeType === 'DELAY' && delayMinutes <= 0) {
    alert('지연 안내는 예상 지연 시간을 1분 이상 입력해야 합니다.');
    if (delayMinutesEl) delayMinutesEl.focus();
    return;
  }

  const flight = state.flights.find(item => item.id === flightId);
  if (!flight) {
    toast('대상 항공편을 찾을 수 없습니다.');
    return;
  }

  flight.flight_status = noticeType === 'DELAY' ? 'DELAYED' : 'CANCELLED';
  flight.delay_minutes = noticeType === 'DELAY' ? delayMinutes : 0;

  state.notices.push({
    id: nextId(state.notices),
    flight_no: flight.flight_no,
    notice_type: noticeType,
    delay_minutes: noticeType === 'DELAY' ? delayMinutes : 0,
    reason,
    created_by: 1002,
    created_at: nowText()
  });

  reasonEl.value = '';

  toast('지연/결항 안내가 등록되었습니다. 추후 SSE 알림과 연결할 예정입니다.');

  renderNoticeHistory();
  renderTodayBoard();
  renderFlightSchedule();
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
      <td>
        <span class="badge badge-scheduled">${airport.flight_type}</span>
      </td>
      <td>
        <div class="toggle ${airport.is_active === 'Y' ? 'on' : ''}" onclick="toggleActive('airport', ${airport.id})"></div>
      </td>
      <td>
        <button class="btn btn-ghost btn-sm" onclick="openBaseModal('airport', '수정', ${airport.id})">
          <span class="material-symbols-outlined" style="font-size:14px">edit</span>
          수정
        </button>
      </td>
    </tr>
  `).join(''));

  setHtml('baseGateTable', state.base.gate.map(gate => `
    <tr>
      <td class="mono">${gate.id}</td>
      <td class="mono">공항 ID: ${gate.airport_id}</td>
      <td class="mono"><strong>${gate.gate_code}</strong></td>
      <td class="mono">${gate.gate_area_id}구역</td>
      <td>
        <span class="badge badge-scheduled">${gate.flight_type}</span>
      </td>
      <td>
        <div class="toggle ${gate.is_active === 'Y' ? 'on' : ''}" onclick="toggleActive('gate', ${gate.id})"></div>
      </td>
      <td>
        <button class="btn btn-ghost btn-sm" onclick="openBaseModal('gate', '수정', ${gate.id})">
          <span class="material-symbols-outlined" style="font-size:14px">edit</span>
          수정
        </button>
      </td>
    </tr>
  `).join(''));

  setHtml('baseRouteTable', state.base.route.map(route => `
    <tr>
      <td class="mono">${route.id}</td>
      <td class="mono">출발: ${route.departure_airport_id}</td>
      <td class="mono">도착: ${route.arrival_airport_id}</td>
      <td>
        <span class="badge badge-scheduled">${route.flight_type}</span>
      </td>
      <td class="mono">유형: ${route.route_type_id}</td>
      <td>
        <div class="toggle ${route.is_active === 'Y' ? 'on' : ''}" onclick="toggleActive('route', ${route.id})"></div>
      </td>
      <td>
        <button class="btn btn-ghost btn-sm" onclick="openBaseModal('route', '수정', ${route.id})">
          <span class="material-symbols-outlined" style="font-size:14px">edit</span>
          수정
        </button>
      </td>
    </tr>
  `).join(''));

  setHtml('baseAircraftTable', state.base.aircraft.map(aircraft => `
    <tr>
      <td class="mono">${aircraft.id}</td>
      <td class="mono"><strong>${aircraft.no}</strong></td>
      <td>${aircraft.model_name}</td>
      <td class="mono">${aircraft.total_seats}석</td>
      <td class="mono">상태코드 ${aircraft.aircraft_status_id}</td>
      <td>
        <div class="toggle ${aircraft.is_active === 'Y' ? 'on' : ''}" onclick="toggleActive('aircraft', ${aircraft.id})"></div>
      </td>
      <td>
        <button class="btn btn-ghost btn-sm" onclick="openBaseModal('aircraft', '수정', ${aircraft.id})">
          <span class="material-symbols-outlined" style="font-size:14px">edit</span>
          수정
        </button>
      </td>
    </tr>
  `).join(''));

  setHtml('baseSeatTable', state.base.seat.map(seat => `
    <tr>
      <td class="mono">${seat.id}</td>
      <td class="mono">기재 ID: ${seat.aircraft_id}</td>
      <td class="mono"><strong>${seat.seat_no}</strong></td>
      <td class="mono">등급 ID: ${seat.seat_class_id}</td>
      <td>
        <div class="toggle ${seat.is_active === 'Y' ? 'on' : ''}" onclick="toggleActive('seat', ${seat.id})"></div>
      </td>
      <td>
        <button class="btn btn-ghost btn-sm" onclick="openBaseModal('seat', '수정', ${seat.id})">
          <span class="material-symbols-outlined" style="font-size:14px">edit</span>
          수정
        </button>
      </td>
    </tr>
  `).join(''));

  setHtml('baseFareTable', state.base.fare.map(fare => `
    <tr>
      <td class="mono">${fare.id}</td>
      <td class="mono">노선 ID: ${fare.route_id}</td>
      <td class="mono">등급 ID: ${fare.seat_class_id}</td>
      <td class="mono">시즌 ID: ${fare.season_id}</td>
      <td class="mono" style="font-weight:700; color:var(--text-dark)">
        ${fare.price.toLocaleString()} 원
      </td>
      <td>
        <div class="toggle ${fare.is_active === 'Y' ? 'on' : ''}" onclick="toggleActive('fare', ${fare.id})"></div>
      </td>
      <td>
        <button class="btn btn-ghost btn-sm" onclick="openBaseModal('fare', '수정', ${fare.id})">
          <span class="material-symbols-outlined" style="font-size:14px">edit</span>
          수정
        </button>
      </td>
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
  if (!list) {
    toast('지원하지 않는 기준정보입니다.');
    return;
  }

  const item = action === '수정'
    ? list.find(row => row.id === Number(id))
    : null;

  if (action === '수정' && !item) {
    toast('수정할 데이터를 찾을 수 없습니다.');
    return;
  }

  activeContext = {
    type: `base_${table}`,
    action,
    id: id ? Number(id) : null
  };

  setHtml('globalModalTitle', `운영 기준 정보 ${table.toUpperCase()} ${action}`);

  let html = '';

  if (table === 'airport') {
    html = `
      <div class="form-grid">
        <div class="field">
          <label>IATA 코드 *</label>
          <input class="input" id="m_b_air_code" value="${item ? item.iata_code : ''}" ${action === '수정' ? 'disabled' : ''}>
        </div>

        <div class="field">
          <label>공항명 *</label>
          <input class="input" id="m_b_air_name" value="${item ? item.name : ''}">
        </div>

        <div class="field">
          <label>국가 *</label>
          <input class="input" id="m_b_air_country" value="${item ? item.country : '대한민국'}">
        </div>

        <div class="field">
          <label>타임존 *</label>
          <input class="input" id="m_b_air_tz" value="${item ? item.timezone : 'Asia/Seoul'}">
        </div>

        <div class="field">
          <label>권역 ID *</label>
          <input class="input" type="number" id="m_b_air_reg" value="${item ? item.region_id : 1}">
        </div>

        <div class="field">
          <label>국내/국제선 구분 *</label>
          <input class="input" id="m_b_air_type" value="${item ? item.flight_type : 'DOM'}">
        </div>
      </div>
    `;
  }

  if (table === 'gate') {
    html = `
      <div class="form-grid">
        <div class="field">
          <label>소속 공항 ID *</label>
          <input class="input" type="number" id="m_b_g_pid" value="${item ? item.airport_id : 1}" ${action === '수정' ? 'disabled' : ''}>
        </div>

        <div class="field">
          <label>게이트 코드 *</label>
          <input class="input" id="m_b_g_code" value="${item ? item.gate_code : ''}" ${action === '수정' ? 'disabled' : ''}>
        </div>

        <div class="field">
          <label>게이트 구역 ID *</label>
          <input class="input" type="number" id="m_b_g_area" value="${item ? item.gate_area_id : 101}">
        </div>

        <div class="field">
          <label>국내/국제선 구분 *</label>
          <input class="input" id="m_b_g_type" value="${item ? item.flight_type : 'DOM'}">
        </div>
      </div>
    `;
  }

  if (table === 'route') {
    html = `
      <div class="form-grid">
        <div class="field">
          <label>출발 공항 ID *</label>
          <input class="input" type="number" id="m_b_r_dep" value="${item ? item.departure_airport_id : 1}" ${action === '수정' ? 'disabled' : ''}>
        </div>

        <div class="field">
          <label>도착 공항 ID *</label>
          <input class="input" type="number" id="m_b_r_arr" value="${item ? item.arrival_airport_id : 3}" ${action === '수정' ? 'disabled' : ''}>
        </div>

        <div class="field">
          <label>국내/국제선 구분 *</label>
          <input class="input" id="m_b_r_type" value="${item ? item.flight_type : 'DOM'}">
        </div>

        <div class="field">
          <label>노선 유형 ID *</label>
          <input class="input" type="number" id="m_b_r_tid" value="${item ? item.route_type_id : 11}">
        </div>
      </div>
    `;
  }

  if (table === 'aircraft') {
    html = `
      <div class="form-grid">
        <div class="field">
          <label>항공기 등록번호 *</label>
          <input class="input" id="m_b_ac_no" value="${item ? item.no : ''}" ${action === '수정' ? 'disabled' : ''}>
        </div>

        <div class="field">
          <label>모델명 *</label>
          <input class="input" id="m_b_ac_model" value="${item ? item.model_name : ''}">
        </div>

        <div class="field">
          <label>총 좌석 수 *</label>
          <input class="input" type="number" id="m_b_ac_seats" value="${item ? item.total_seats : 180}">
        </div>

        <div class="field">
          <label>운영 상태 ID *</label>
          <input class="input" type="number" id="m_b_ac_status" value="${item ? item.aircraft_status_id : 1}">
        </div>
      </div>
    `;
  }

  if (table === 'seat') {
    if (action === '일괄등록') {
      html = `
        <div class="form-grid">
          <div class="field full">
            <label>대상 항공기 ID *</label>
            <input class="input" type="number" id="m_b_s_bulk_id" value="1">
          </div>

          <div class="field">
            <label>이코노미 생성 좌석 수 *</label>
            <input class="input" type="number" id="m_b_s_bulk_eco" value="150">
          </div>

          <div class="field">
            <label>비즈니스 생성 좌석 수 *</label>
            <input class="input" type="number" id="m_b_s_bulk_biz" value="12">
          </div>
        </div>
      `;
    } else {
      html = `
        <div class="form-grid">
          <div class="field">
            <label>항공기 ID</label>
            <input class="input" type="number" id="m_b_s_pid" value="${item ? item.aircraft_id : 1}" disabled>
          </div>

          <div class="field">
            <label>좌석 번호</label>
            <input class="input" id="m_b_s_no" value="${item ? item.seat_no : ''}" disabled>
          </div>

          <div class="field full">
            <label>좌석 등급 ID *</label>
            <input class="input" type="number" id="m_b_s_class" value="${item ? item.seat_class_id : 1}">
          </div>
        </div>
      `;
    }
  }

  if (table === 'fare') {
    html = `
      <div class="form-grid">
        <div class="field">
          <label>노선 ID *</label>
          <input class="input" type="number" id="m_b_f_rid" value="${item ? item.route_id : 1}" ${action === '수정' ? 'disabled' : ''}>
        </div>

        <div class="field">
          <label>좌석 등급 ID *</label>
          <input class="input" type="number" id="m_b_f_sid" value="${item ? item.seat_class_id : 1}" ${action === '수정' ? 'disabled' : ''}>
        </div>

        <div class="field">
          <label>시즌 ID *</label>
          <input class="input" type="number" id="m_b_f_season" value="${item ? item.season_id : 1}" ${action === '수정' ? 'disabled' : ''}>
        </div>

        <div class="field full">
          <label>운임 금액 *</label>
          <input class="input" type="number" id="m_b_f_price" value="${item ? item.price : 100000}">
        </div>
      </div>
    `;
  }

  setHtml('globalModalBody', html);
  openModal();
}

/* ==================== 통합 모달 저장 ==================== */
function saveGlobalModalData() {
  const ctx = activeContext;

  /*if (ctx.type === 'flight') {
    saveFlightFromModal(ctx);
    closeModal();
    return;
  }*/

  if (ctx.type && ctx.type.startsWith('base_')) {
    saveBaseFromModal(ctx);
    closeModal();
    return;
  }

  toast('저장할 작업 정보를 찾을 수 없습니다.');
}

/*function saveFlightFromModal(ctx) {
  const flightNo = $('m_f_no') ? $('m_f_no').value.trim() : '';
  const routeId = Number($('m_f_route') ? $('m_f_route').value : 0);
  const aircraftId = Number($('m_f_aircraft') ? $('m_f_aircraft').value : 0);
  const departureGateId = Number($('m_f_dgate') ? $('m_f_dgate').value : 0);
  const arrivalGateId = Number($('m_f_agate') ? $('m_f_agate').value : 0);
  const departureTime = $('m_f_dtime') ? $('m_f_dtime').value.trim() : '';
  const arrivalTime = $('m_f_atime') ? $('m_f_atime').value.trim() : '';

  if (ctx.action === '등록' && !flightNo) {
    alert('항공편 번호를 입력해야 합니다.');
    return;
  }

  if (!routeId || !aircraftId || !departureTime || !arrivalTime) {
    alert('노선, 항공기, 출발/도착 일시는 필수입니다.');
    return;
  }

  if (ctx.action === '등록') {
    state.flights.push({
      id: nextId(state.flights),
      flight_no: flightNo,
      route_id: routeId,
      route_name: getRouteName(routeId),
      aircraft_id: aircraftId,
      aircraft_model: getAircraftModel(aircraftId),
      departure_gate_id: departureGateId,
      arrival_gate_id: arrivalGateId,
      departure_time: departureTime,
      arrival_time: arrivalTime,
      flight_status: 'SCHEDULED',
      delay_minutes: 0,
      is_deleted: 'N'
    });

    toast('신규 운항 스케줄이 등록되었습니다.');
  } else {
    const flight = state.flights.find(item => item.id === Number(ctx.id));
    if (!flight) {
      toast('수정할 스케줄을 찾을 수 없습니다.');
      return;
    }

    flight.route_id = routeId;
    flight.route_name = getRouteName(routeId);
    flight.aircraft_id = aircraftId;
    flight.aircraft_model = getAircraftModel(aircraftId);
    flight.departure_gate_id = departureGateId;
    flight.arrival_gate_id = arrivalGateId;
    flight.departure_time = departureTime;
    flight.arrival_time = arrivalTime;

    toast('운항 스케줄이 수정되었습니다.');
  }

  renderFlightSchedule();
  renderTodayBoard();
}*/

function saveBaseFromModal(ctx) {
  const table = ctx.type.replace('base_', '');
  const list = state.base[table];

  if (!list) {
    toast('기준정보 저장 대상을 찾을 수 없습니다.');
    return;
  }

  if (ctx.action === '등록' || ctx.action === '일괄등록') {
    insertBaseItem(table, list, ctx.action);
  } else {
    updateBaseItem(table, list, ctx.id);
  }

  renderBaseTables();
}

function insertBaseItem(table, list, action) {
  const id = nextId(list);

  if (table === 'airport') {
    list.push({
      id,
      iata_code: $('m_b_air_code').value.trim().toUpperCase(),
      name: $('m_b_air_name').value.trim(),
      country: $('m_b_air_country').value.trim(),
      timezone: $('m_b_air_tz').value.trim(),
      region_id: Number($('m_b_air_reg').value),
      flight_type: $('m_b_air_type').value.trim().toUpperCase(),
      is_active: 'Y'
    });
  }

  if (table === 'gate') {
    list.push({
      id,
      airport_id: Number($('m_b_g_pid').value),
      gate_code: $('m_b_g_code').value.trim().toUpperCase(),
      gate_area_id: Number($('m_b_g_area').value),
      flight_type: $('m_b_g_type').value.trim().toUpperCase(),
      is_active: 'Y'
    });
  }

  if (table === 'route') {
    list.push({
      id,
      departure_airport_id: Number($('m_b_r_dep').value),
      arrival_airport_id: Number($('m_b_r_arr').value),
      flight_type: $('m_b_r_type').value.trim().toUpperCase(),
      route_type_id: Number($('m_b_r_tid').value),
      is_active: 'Y'
    });
  }

  if (table === 'aircraft') {
    list.push({
      id,
      no: $('m_b_ac_no').value.trim().toUpperCase(),
      model_name: $('m_b_ac_model').value.trim(),
      total_seats: Number($('m_b_ac_seats').value),
      aircraft_status_id: Number($('m_b_ac_status').value),
      is_active: 'Y'
    });
  }

  if (table === 'seat' && action === '일괄등록') {
    const aircraftId = Number($('m_b_s_bulk_id').value);
    const ecoCount = Number($('m_b_s_bulk_eco').value);
    const bizCount = Number($('m_b_s_bulk_biz').value);

    const startId = nextId(state.base.seat);

    state.base.seat.push({
      id: startId,
      aircraft_id: aircraftId,
      seat_no: '1A',
      seat_class_id: 2,
      is_active: 'Y'
    });

    state.base.seat.push({
      id: startId + 1,
      aircraft_id: aircraftId,
      seat_no: '1B',
      seat_class_id: 2,
      is_active: 'Y'
    });

    state.base.seat.push({
      id: startId + 2,
      aircraft_id: aircraftId,
      seat_no: '12A',
      seat_class_id: 1,
      is_active: 'Y'
    });

    toast(`좌석 샘플이 일괄 생성되었습니다. 이코노미 ${ecoCount}석, 비즈니스 ${bizCount}석 기준입니다.`);
    return;
  }

  if (table === 'fare') {
    list.push({
      id,
      route_id: Number($('m_b_f_rid').value),
      seat_class_id: Number($('m_b_f_sid').value),
      season_id: Number($('m_b_f_season').value),
      price: Number($('m_b_f_price').value),
      is_active: 'Y'
    });
  }

  toast('운영 기준 정보가 등록되었습니다.');
}

function updateBaseItem(table, list, id) {
  const item = list.find(row => row.id === Number(id));
  if (!item) {
    toast('수정할 기준정보를 찾을 수 없습니다.');
    return;
  }

  if (table === 'airport') {
    item.name = $('m_b_air_name').value.trim();
    item.country = $('m_b_air_country').value.trim();
    item.timezone = $('m_b_air_tz').value.trim();
    item.region_id = Number($('m_b_air_reg').value);
    item.flight_type = $('m_b_air_type').value.trim().toUpperCase();
  }

  if (table === 'gate') {
    item.gate_area_id = Number($('m_b_g_area').value);
    item.flight_type = $('m_b_g_type').value.trim().toUpperCase();
  }

  if (table === 'route') {
    item.flight_type = $('m_b_r_type').value.trim().toUpperCase();
    item.route_type_id = Number($('m_b_r_tid').value);
  }

  if (table === 'aircraft') {
    item.model_name = $('m_b_ac_model').value.trim();
    item.total_seats = Number($('m_b_ac_seats').value);
    item.aircraft_status_id = Number($('m_b_ac_status').value);
  }

  if (table === 'seat') {
    item.seat_class_id = Number($('m_b_s_class').value);
  }

  if (table === 'fare') {
    item.price = Number($('m_b_f_price').value);
  }

  toast('운영 기준 정보가 수정되었습니다.');
}

/* ==================== 내부 탭 제어 ==================== */
function subTab(scope, tabName, btn) {
  const prefix = scope === 'base' ? 'b' : 'c';
  const target = $(`tab-${prefix}-${tabName}`);

  if (!target) return;

  const root = target.closest('.main') || document;

  root.querySelectorAll(`.sub-page[id^="tab-${prefix}-"]`).forEach(page => {
    page.classList.remove('active');
  });

  const tabBox = target.parentElement ? root.querySelector('.tabs') : null;
  if (tabBox) {
    tabBox.querySelectorAll('button').forEach(button => {
      button.classList.remove('active');
    });
  }

  target.classList.add('active');

  const clickedButton = btn || (typeof event !== 'undefined' ? event.currentTarget : null);
  if (clickedButton) {
    clickedButton.classList.add('active');
  }
}

/* 기존 통합 staff.html을 잠깐 테스트할 때를 위한 안전용 함수 */
function nav(el, page) {
  document.querySelectorAll('.nav-link').forEach(link => {
    link.classList.remove('active');
  });

  if (el) {
    el.classList.add('active');
  }

  document.querySelectorAll('.page').forEach(pageEl => {
    pageEl.classList.remove('active');
  });

  const target = $(`p-${page}`);
  if (target) {
    target.classList.add('active');
  }

  window.scrollTo(0, 0);
}

/* ==================== 토스트 ==================== */
function toast(message) {
  const wrap = $('toasts');

  if (!wrap) {
    console.log(message);
    return;
  }

  const toastEl = document.createElement('div');
  toastEl.className = 'toast';
  toastEl.innerHTML = `
    <span class="material-symbols-outlined" style="color:var(--ok)">check_circle</span>
    ${message}
  `;

  wrap.appendChild(toastEl);

  setTimeout(() => {
    toastEl.style.opacity = '0';
    toastEl.style.transition = 'opacity .2s';

    setTimeout(() => {
      toastEl.remove();
    }, 200);
  }, 2200);
}

/* ==================== 초기 실행 ==================== */
function initStaffPage() {
  /*renderTodayBoard();
  renderFlightSchedule();*/
  loadPaxData();
  renderNoticeHistory();
  renderBaseTables();
  toggleNoticeField();

  const clock = $('clock');
  if (clock) {
    clock.textContent = new Date().toTimeString().slice(0, 8);
  }
}

setInterval(() => {
  const clock = $('clock');
  if (clock) {
    clock.textContent = new Date().toTimeString().slice(0, 8);
  }
}, 1000);

document.addEventListener('DOMContentLoaded', initStaffPage);

/* =========================================================
   고객 문의 관리 화면 추가 기능
   - inquiry_list.html
   - CHAT_INQUIRY / CHAT_MESSAGE 하드코딩 뼈대
========================================================= */

/* 문의 데이터 추가 */
state.inquiries = [
  {
    id: 3021,
    member_id: 401,
    name: '홍길동',
    staff_id: 1002,
    status: 'IN_PROGRESS',
    created_at: '2026-06-25 10:24'
  },
  {
    id: 3018,
    member_id: 405,
    name: '김영희',
    staff_id: 1001,
    status: 'RESOLVED',
    created_at: '2026-06-25 09:50'
  }
];

/* 채팅 메시지 샘플 */
state.chatMessages = {
  3021: [
    {
      sender: 'USER',
      message: '항공권 예약 변경 방법을 알려주세요.',
      sent_at: '2026-06-25 10:24'
    },
    {
      sender: 'STAFF',
      message: '예약 변경은 마이페이지 예약 상세에서 가능하며, 운임 규정에 따라 수수료가 발생할 수 있습니다.',
      sent_at: '2026-06-25 10:27'
    }
  ],
  3018: [
    {
      sender: 'USER',
      message: '결항 항공편 환불은 자동으로 처리되나요?',
      sent_at: '2026-06-25 09:50'
    },
    {
      sender: 'STAFF',
      message: '결항 항공편은 전액 자동 환불 대상으로 처리됩니다.',
      sent_at: '2026-06-25 09:55'
    }
  ]
};

/* 문의 상태 매핑 */
mapper.inquiryStatus = {
  IN_PROGRESS: {
    text: '처리중',
    cls: 'badge-warn'
  },
  RESOLVED: {
    text: '해결 완료',
    cls: 'badge-ok'
  }
};

/* 고객 문의 목록 렌더링 */
function renderInquiryList() {
  const container = document.getElementById('inquiryTableBody');
  if (!container) return;

  container.innerHTML = state.inquiries.map(inquiry => {
    const status = mapper.inquiryStatus[inquiry.status] || {
      text: inquiry.status,
      cls: 'badge-muted'
    };

    return `
      <tr>
        <td class="mono">${inquiry.id}</td>
        <td>
          <strong>${inquiry.name}</strong>
          <span style="color:var(--text-light); font-size:12px;">
            (회원 ID: ${inquiry.member_id})
          </span>
        </td>
        <td class="mono">STAFF_${inquiry.staff_id}</td>
        <td>
          <span class="badge ${status.cls}">
            ${status.text}
          </span>
        </td>
        <td class="mono">${inquiry.created_at}</td>
        <td>
          <div class="btn-action-group">
            <button type="button"
                    class="btn btn-ghost btn-sm"
                    onclick="openChatModal(${inquiry.id})">
              <span class="material-symbols-outlined" style="font-size:14px">chat</span>
              1:1 채팅 답변
            </button>

            <button type="button"
                    class="btn btn-primary btn-sm"
                    onclick="toggleInquiryStatus(${inquiry.id})">
              상태 변경
            </button>
          </div>
        </td>
      </tr>
    `;
  }).join('');
}

/* 문의 상태 변경 */
function toggleInquiryStatus(id) {
  const inquiry = state.inquiries.find(item => item.id === Number(id));

  if (!inquiry) {
    toast('대상 문의를 찾을 수 없습니다.');
    return;
  }

  inquiry.status = inquiry.status === 'IN_PROGRESS'
    ? 'RESOLVED'
    : 'IN_PROGRESS';

  toast(`문의 ID ${id}의 처리 상태가 변경되었습니다.`);

  renderInquiryList();
}

/* 채팅 답변 모달 */
function openChatModal(id) {
  const inquiry = state.inquiries.find(item => item.id === Number(id));

  if (!inquiry) {
    toast('대상 문의를 찾을 수 없습니다.');
    return;
  }

  activeContext = {
    type: 'chat',
    action: '답변',
    id: Number(id)
  };

  const messages = state.chatMessages[id] || [];

  const messageHtml = messages.map(message => {
    const isStaff = message.sender === 'STAFF';

    return `
      <div style="margin-bottom:10px; text-align:${isStaff ? 'right' : 'left'};">
        <div style="
          display:inline-block;
          max-width:80%;
          padding:10px 12px;
          border-radius:10px;
          background:${isStaff ? 'rgba(2, 132, 199, 0.12)' : '#F1F5F9'};
          color:var(--text-dark);
          font-size:13px;
          text-align:left;
        ">
          <div style="font-weight:700; margin-bottom:4px; color:${isStaff ? 'var(--primary)' : 'var(--text-gray)'};">
            ${isStaff ? '지상직' : '고객'}
          </div>
          <div>${message.message}</div>
          <div class="mono" style="font-size:11px; color:var(--text-light); margin-top:6px;">
            ${message.sent_at}
          </div>
        </div>
      </div>
    `;
  }).join('');

  setHtml('globalModalTitle', `1:1 채팅 답변 - ${inquiry.name} 고객`);

  setHtml('globalModalBody', `
    <div style="
      background:#FFFFFF;
      border:1px solid var(--border);
      border-radius:8px;
      padding:14px;
      height:260px;
      overflow-y:auto;
      margin-bottom:16px;
    ">
      ${messageHtml}
    </div>

    <div class="field full">
      <label style="font-size:12px; font-weight:600; color:var(--text-gray); display:block; margin-bottom:6px;">
        지상직 공식 답변 메시지
      </label>

      <textarea class="textarea"
                id="chatReplyMsg"
                rows="4"
                placeholder="고객에게 전송할 답변 내용을 입력하세요."></textarea>

      <p style="font-size:12px; color:var(--text-light); margin-top:8px;">
        현재는 하드코딩 화면 테스트 단계입니다. 추후 WebSocket/STOMP 전송 로직과 연결합니다.
      </p>
    </div>
  `);

  openModal();
}

/* 채팅 답변 저장 */
function saveChatReply(ctx) {
  const inquiryId = Number(ctx.id);
  const input = document.getElementById('chatReplyMsg');

  if (!input) {
    toast('답변 입력창을 찾을 수 없습니다.');
    return;
  }

  const message = input.value.trim();

  if (!message) {
    alert('답변 메시지를 입력해야 합니다.');
    input.focus();
    return;
  }

  if (!state.chatMessages[inquiryId]) {
    state.chatMessages[inquiryId] = [];
  }

  state.chatMessages[inquiryId].push({
    sender: 'STAFF',
    message,
    sent_at: nowText()
  });

  const inquiry = state.inquiries.find(item => item.id === inquiryId);

  if (inquiry) {
    inquiry.status = 'IN_PROGRESS';
  }

  toast('지상직 답변 메시지가 저장되었습니다. 추후 WebSocket/STOMP 전송과 연결합니다.');

  renderInquiryList();
}

/* 기존 saveGlobalModalData 함수 덮어쓰기 */
function saveGlobalModalData() {
  const ctx = activeContext;

  /*if (ctx.type === 'flight') {
    saveFlightFromModal(ctx);
    closeModal();
    return;
  }*/

  if (ctx.type && ctx.type.startsWith('base_')) {
    saveBaseFromModal(ctx);
    closeModal();
    return;
  }

  if (ctx.type === 'chat') {
    saveChatReply(ctx);
    closeModal();
    return;
  }

  toast('저장할 작업 정보를 찾을 수 없습니다.');
}

/* inquiry 화면 초기 렌더링 */
document.addEventListener('DOMContentLoaded', function() {
  renderInquiryList();
});
/* =========================================================
   공지·콘텐츠 관리 화면 추가 기능
   - content_list.html
   - NOTICE / EVENT / FAQ 하드코딩 뼈대
========================================================= */

/* 콘텐츠 데이터 추가 */
state.cms = {
  notice: [
    {
      id: 12,
      title: '하계 운항 정기 스케줄 변경 안내',
      content: '하계 기간 노선 확충에 따른 타임테이블 변경 내역입니다.',
      is_public: 'Y',
      created_by: 1002,
      created_at: '2026-06-10 10:00'
    }
  ],

  event: [
    {
      id: 1,
      title: '제주 노선 7월 얼리버드 특가',
      content: '7월 평일 출발 승객 대상 특별 운임 프로모션',
      start_date: '2026-07-01',
      end_date: '2026-07-15',
      image_url: 'img/jeju_event.png',
      is_visible: 'Y',
      is_ended: 'N'
    }
  ],

  faq: [
    {
      id: 1,
      category: '예약/결제',
      question: '예약 변경은 어디에서 하나요?',
      answer: '마이페이지 예약 상세 화면에서 예약 변경 가능 여부를 확인할 수 있습니다.',
      is_visible: 'Y'
    }
  ]
};

/* 콘텐츠 목록 렌더링 */
function renderCmsTables() {
  const noticeTable = document.getElementById('noticeContentTable');
  const eventTable = document.getElementById('eventContentTable');
  const faqTable = document.getElementById('faqContentTable');

  if (noticeTable) {
    noticeTable.innerHTML = state.cms.notice.map(notice => `
      <tr>
        <td class="mono">${notice.id}</td>
        <td><strong>${notice.title}</strong></td>
        <td style="max-width:260px; overflow:hidden; text-overflow:ellipsis; white-space:nowrap;">
          ${notice.content}
        </td>
        <td>
          <div class="toggle ${notice.is_public === 'Y' ? 'on' : ''}"
               onclick="toggleCmsFlag('notice', ${notice.id}, 'is_public')"></div>
        </td>
        <td class="mono">STAFF_${notice.created_by}</td>
        <td class="mono">${notice.created_at}</td>
        <td>
          <div class="btn-action-group">
            <button type="button"
                    class="btn btn-ghost btn-sm"
                    onclick="openContentModal('notice', '수정', ${notice.id})">
              수정
            </button>

            <button type="button"
                    class="btn btn-danger btn-sm"
                    onclick="deleteCmsItem('notice', ${notice.id})">
              삭제
            </button>
          </div>
        </td>
      </tr>
    `).join('');
  }

  if (eventTable) {
    eventTable.innerHTML = state.cms.event.map(event => `
      <tr>
        <td class="mono">${event.id}</td>
        <td><strong>${event.title}</strong></td>
        <td class="mono">${event.start_date}</td>
        <td class="mono">${event.end_date}</td>
        <td class="mono" style="font-size:11px;">
          ${event.image_url || '-'}
        </td>
        <td>
          <div class="toggle ${event.is_visible === 'Y' ? 'on' : ''}"
               onclick="toggleCmsFlag('event', ${event.id}, 'is_visible')"></div>
        </td>
        <td>
          <span class="badge ${event.is_ended === 'Y' ? 'badge-muted' : 'badge-ok'}">
            ${event.is_ended === 'Y' ? '종료됨' : '진행중'}
          </span>
        </td>
        <td>
          <div class="btn-action-group">
            <button type="button"
                    class="btn btn-ghost btn-sm"
                    onclick="openContentModal('event', '수정', ${event.id})">
              수정
            </button>

            <button type="button"
                    class="btn btn-primary btn-sm"
                    onclick="forceEndEvent(${event.id})">
              수동 종료
            </button>
          </div>
        </td>
      </tr>
    `).join('');
  }

  if (faqTable) {
    faqTable.innerHTML = state.cms.faq.map(faq => `
      <tr>
        <td class="mono">${faq.id}</td>
        <td>
          <span class="badge badge-scheduled">${faq.category}</span>
        </td>
        <td><strong>${faq.question}</strong></td>
        <td style="max-width:320px; overflow:hidden; text-overflow:ellipsis; white-space:nowrap;">
          ${faq.answer}
        </td>
        <td>
          <div class="toggle ${faq.is_visible === 'Y' ? 'on' : ''}"
               onclick="toggleCmsFlag('faq', ${faq.id}, 'is_visible')"></div>
        </td>
        <td>
          <div class="btn-action-group">
            <button type="button"
                    class="btn btn-ghost btn-sm"
                    onclick="openContentModal('faq', '수정', ${faq.id})">
              수정
            </button>

            <button type="button"
                    class="btn btn-danger btn-sm"
                    onclick="deleteCmsItem('faq', ${faq.id})">
              삭제
            </button>
          </div>
        </td>
      </tr>
    `).join('');
  }
}

/* 공개/노출 여부 토글 */
function toggleCmsFlag(category, id, field) {
  const list = state.cms[category];

  if (!list) {
    toast('지원하지 않는 콘텐츠 유형입니다.');
    return;
  }

  const item = list.find(row => row.id === Number(id));

  if (!item) {
    toast('대상 콘텐츠를 찾을 수 없습니다.');
    return;
  }

  item[field] = item[field] === 'Y' ? 'N' : 'Y';

  toast('콘텐츠 노출 상태가 변경되었습니다.');

  renderCmsTables();
}

/* 공지/이벤트/FAQ 등록·수정 모달 */
function openContentModal(category, action, id = null) {
  const list = state.cms[category];

  if (!list) {
    toast('지원하지 않는 콘텐츠 유형입니다.');
    return;
  }

  const item = action === '수정'
    ? list.find(row => row.id === Number(id))
    : null;

  if (action === '수정' && !item) {
    toast('수정할 콘텐츠를 찾을 수 없습니다.');
    return;
  }

  activeContext = {
    type: `cms_${category}`,
    action,
    id: id ? Number(id) : null
  };

  let titleText = '';

  if (category === 'notice') {
    titleText = `공지사항 ${action}`;
  }

  if (category === 'event') {
    titleText = `이벤트 ${action}`;
  }

  if (category === 'faq') {
    titleText = `FAQ ${action}`;
  }

  setHtml('globalModalTitle', titleText);

  let html = '';

  if (category === 'notice') {
    html = `
      <div class="form-grid">
        <div class="field full">
          <label>공지사항 제목 *</label>
          <input class="input"
                 id="m_c_notice_title"
                 value="${item ? item.title : ''}"
                 placeholder="예: 하계 운항 정기 스케줄 변경 안내">
        </div>

        <div class="field full">
          <label>공지사항 본문 내용 *</label>
          <textarea class="textarea"
                    id="m_c_notice_content"
                    rows="6"
                    placeholder="공지사항 본문을 입력하세요.">${item ? item.content : ''}</textarea>
        </div>
      </div>
    `;
  }

  if (category === 'event') {
    html = `
      <div class="form-grid">
        <div class="field full">
          <label>이벤트명 *</label>
          <input class="input"
                 id="m_c_event_title"
                 value="${item ? item.title : ''}"
                 placeholder="예: 제주 노선 얼리버드 특가">
        </div>

        <div class="field full">
          <label>이벤트 본문 내용 *</label>
          <textarea class="textarea"
                    id="m_c_event_content"
                    rows="4"
                    placeholder="이벤트 상세 내용을 입력하세요.">${item ? item.content : ''}</textarea>
        </div>

        <div class="field">
          <label>이벤트 시작일 *</label>
          <input class="input"
                 id="m_c_event_start"
                 value="${item ? item.start_date : ''}"
                 placeholder="YYYY-MM-DD">
        </div>

        <div class="field">
          <label>이벤트 종료일 *</label>
          <input class="input"
                 id="m_c_event_end"
                 value="${item ? item.end_date : ''}"
                 placeholder="YYYY-MM-DD">
        </div>

        <div class="field full">
          <label>배너 이미지 URL</label>
          <input class="input"
                 id="m_c_event_image"
                 value="${item ? item.image_url : ''}"
                 placeholder="예: img/event_banner.png">
        </div>
      </div>
    `;
  }

  if (category === 'faq') {
    html = `
      <div class="form-grid">
        <div class="field full">
          <label>문의 카테고리 *</label>
          <input class="input"
                 id="m_c_faq_category"
                 value="${item ? item.category : ''}"
                 placeholder="예: 예약/결제">
        </div>

        <div class="field full">
          <label>질문 내용 *</label>
          <input class="input"
                 id="m_c_faq_question"
                 value="${item ? item.question : ''}"
                 placeholder="예: 예약 변경은 어디에서 하나요?">
        </div>

        <div class="field full">
          <label>답변 내용 *</label>
          <textarea class="textarea"
                    id="m_c_faq_answer"
                    rows="5"
                    placeholder="FAQ 답변 내용을 입력하세요.">${item ? item.answer : ''}</textarea>
        </div>
      </div>
    `;
  }

  setHtml('globalModalBody', html);

  openModal();
}

/* 콘텐츠 모달 저장 */
function saveCmsFromModal(ctx) {
  const category = ctx.type.replace('cms_', '');

  if (ctx.action === '등록') {
    insertCmsItem(category);
  } else {
    updateCmsItem(category, ctx.id);
  }

  renderCmsTables();
}

/* 콘텐츠 등록 */
function insertCmsItem(category) {
  const list = state.cms[category];

  if (!list) {
    toast('등록 대상 콘텐츠 유형을 찾을 수 없습니다.');
    return;
  }

  const id = nextId(list);
  const createdAt = typeof nowText === 'function'
    ? nowText()
    : new Date().toISOString().replace('T', ' ').slice(0, 16);

  if (category === 'notice') {
    const title = document.getElementById('m_c_notice_title').value.trim();
    const content = document.getElementById('m_c_notice_content').value.trim();

    if (!title || !content) {
      alert('공지사항 제목과 본문은 필수입니다.');
      return;
    }

    list.push({
      id,
      title,
      content,
      is_public: 'Y',
      created_by: 1002,
      created_at: createdAt
    });
  }

  if (category === 'event') {
    const title = document.getElementById('m_c_event_title').value.trim();
    const content = document.getElementById('m_c_event_content').value.trim();
    const startDate = document.getElementById('m_c_event_start').value.trim();
    const endDate = document.getElementById('m_c_event_end').value.trim();
    const imageUrl = document.getElementById('m_c_event_image').value.trim();

    if (!title || !content || !startDate || !endDate) {
      alert('이벤트명, 본문, 시작일, 종료일은 필수입니다.');
      return;
    }

    list.push({
      id,
      title,
      content,
      start_date: startDate,
      end_date: endDate,
      image_url: imageUrl,
      is_visible: 'Y',
      is_ended: 'N'
    });
  }

  if (category === 'faq') {
    const categoryName = document.getElementById('m_c_faq_category').value.trim();
    const question = document.getElementById('m_c_faq_question').value.trim();
    const answer = document.getElementById('m_c_faq_answer').value.trim();

    if (!categoryName || !question || !answer) {
      alert('FAQ 카테고리, 질문, 답변은 필수입니다.');
      return;
    }

    list.push({
      id,
      category: categoryName,
      question,
      answer,
      is_visible: 'Y'
    });
  }

  toast('신규 콘텐츠가 등록되었습니다.');
}

/* 콘텐츠 수정 */
function updateCmsItem(category, id) {
  const list = state.cms[category];

  if (!list) {
    toast('수정 대상 콘텐츠 유형을 찾을 수 없습니다.');
    return;
  }

  const item = list.find(row => row.id === Number(id));

  if (!item) {
    toast('수정할 콘텐츠를 찾을 수 없습니다.');
    return;
  }

  if (category === 'notice') {
    const title = document.getElementById('m_c_notice_title').value.trim();
    const content = document.getElementById('m_c_notice_content').value.trim();

    if (!title || !content) {
      alert('공지사항 제목과 본문은 필수입니다.');
      return;
    }

    item.title = title;
    item.content = content;
  }

  if (category === 'event') {
    const title = document.getElementById('m_c_event_title').value.trim();
    const content = document.getElementById('m_c_event_content').value.trim();
    const startDate = document.getElementById('m_c_event_start').value.trim();
    const endDate = document.getElementById('m_c_event_end').value.trim();
    const imageUrl = document.getElementById('m_c_event_image').value.trim();

    if (!title || !content || !startDate || !endDate) {
      alert('이벤트명, 본문, 시작일, 종료일은 필수입니다.');
      return;
    }

    item.title = title;
    item.content = content;
    item.start_date = startDate;
    item.end_date = endDate;
    item.image_url = imageUrl;
  }

  if (category === 'faq') {
    const categoryName = document.getElementById('m_c_faq_category').value.trim();
    const question = document.getElementById('m_c_faq_question').value.trim();
    const answer = document.getElementById('m_c_faq_answer').value.trim();

    if (!categoryName || !question || !answer) {
      alert('FAQ 카테고리, 질문, 답변은 필수입니다.');
      return;
    }

    item.category = categoryName;
    item.question = question;
    item.answer = answer;
  }

  toast('콘텐츠가 수정되었습니다.');
}

/* 콘텐츠 삭제 */
function deleteCmsItem(category, id) {
  const list = state.cms[category];

  if (!list) {
    toast('삭제 대상 콘텐츠 유형을 찾을 수 없습니다.');
    return;
  }

  const ok = confirm('삭제 시 복구할 수 없습니다. 삭제하시겠습니까?');

  if (!ok) {
    return;
  }

  state.cms[category] = list.filter(item => item.id !== Number(id));

  toast('콘텐츠가 삭제되었습니다.');

  renderCmsTables();
}

/* 이벤트 수동 종료 */
function forceEndEvent(id) {
  const eventItem = state.cms.event.find(item => item.id === Number(id));

  if (!eventItem) {
    toast('대상 이벤트를 찾을 수 없습니다.');
    return;
  }

  eventItem.is_ended = 'Y';

  toast('해당 이벤트가 수동 종료 처리되었습니다.');

  renderCmsTables();
}

/* 기존 saveGlobalModalData 함수 최종 덮어쓰기 */
function saveGlobalModalData() {
  const ctx = activeContext;

  if (!ctx || !ctx.type) {
    toast('저장할 작업 정보를 찾을 수 없습니다.');
    return;
  }

  /*if (ctx.type === 'flight') {
    saveFlightFromModal(ctx);
    closeModal();
    return;
  }*/

  if (ctx.type.startsWith('base_')) {
    saveBaseFromModal(ctx);
    closeModal();
    return;
  }

  if (ctx.type.startsWith('cms_')) {
    saveCmsFromModal(ctx);
    closeModal();
    return;
  }

  if (ctx.type === 'chat' && typeof saveChatReply === 'function') {
    saveChatReply(ctx);
    closeModal();
    return;
  }

  toast('저장할 작업 정보를 찾을 수 없습니다.');
}

/* content 화면 초기 렌더링 */
document.addEventListener('DOMContentLoaded', function() {
  renderCmsTables();
});



/* =========================================================
   운항 스케줄, 오늘 항공편
========================================================= */

// 1. 등록 모달
function openFlightAddModal() {
    document.getElementById('flightForm').reset();
    document.getElementById('modalFlightId').value = '';
    document.getElementById('flightModalTitle').innerText = '새 운항 스케줄 등록';
	filterGatesByRoute();
    document.getElementById('flightModal').style.display = 'flex';
}

// 2. 수정 모달 
function openFlightEditModal(btn) {
    document.getElementById('flightForm').reset();
    document.getElementById('modalFlightId').value = btn.getAttribute('data-id');
    document.getElementById('flightModalTitle').innerText = '운항 스케줄 수정';
    
    document.getElementById('modalFlightNo').value = btn.getAttribute('data-no');
    document.getElementById('modalRouteId').value = btn.getAttribute('data-route');
    document.getElementById('modalAircraftId').value = btn.getAttribute('data-ac');
	// 목록 필터링
	filterGatesByRoute();
    document.getElementById('modalDepGateId').value = btn.getAttribute('data-dgate') || 0;
    document.getElementById('modalArrGateId').value = btn.getAttribute('data-agate') || 0;
    document.getElementById('modalDepTime').value = btn.getAttribute('data-dtime');
    document.getElementById('modalArrTime').value = btn.getAttribute('data-atime');

    document.getElementById('flightModal').style.display = 'flex';
}

// 노선에 맞는 출발/도착 공항 게이트
function filterGatesByRoute() {
    const routeSelect = document.getElementById('modalRouteId');
    const selectedOption = routeSelect.options[routeSelect.selectedIndex];

    const depAirportId = selectedOption.getAttribute('data-dep-id');
    const arrAirportId = selectedOption.getAttribute('data-arr-id');

    const depGateSelect = document.getElementById('modalDepGateId');
    const arrGateSelect = document.getElementById('modalArrGateId');

    // 출발 게이트 필터링 (숨김/표시)
    Array.from(depGateSelect.options).forEach(opt => {
        if (opt.value === "0") return;
        const isMatch = opt.getAttribute('data-airport-id') === depAirportId;
        opt.hidden = !isMatch;     // 옵션 숨기기
        opt.disabled = !isMatch;
    });

    // 도착 게이트 필터링 (숨김/표시)
    Array.from(arrGateSelect.options).forEach(opt => {
        if (opt.value === "0") return;
        const isMatch = opt.getAttribute('data-airport-id') === arrAirportId;
        opt.hidden = !isMatch;
        opt.disabled = !isMatch;
    });

    // 만약 현재 선택되어 있던 게이트가 숨김 처리되었다면 미정으로 초기화
    if (depGateSelect.options[depGateSelect.selectedIndex]?.hidden) depGateSelect.value = "0";
    if (arrGateSelect.options[arrGateSelect.selectedIndex]?.hidden) arrGateSelect.value = "0";
}

// 3. 모달 닫기
function closeFlightModal() {
    document.getElementById('flightModal').style.display = 'none';
}

// 4. 스케줄 등록/수정
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

    if(!requestData.flight_no || !requestData.route_id || !requestData.aircraft_id || !requestData.departure_time || !requestData.arrival_time) {
        alert('필수 항목(*)을 모두 입력해주세요.');
        return;
    }

    const url = flightId ? '/staff/schedule/api/update' : '/staff/schedule/api/insert';
    
    const csrfMeta = document.querySelector('meta[name="_csrf"]');
    const csrfHeaderMeta = document.querySelector('meta[name="_csrf_header"]');
    
    if (!csrfMeta || !csrfHeaderMeta) {
        alert('보안 토큰(CSRF)을 찾을 수 없습니다. HTML의 <head> 영역을 확인해주세요.');
        return;
    }

    const headers = { 
        'Content-Type': 'application/json',
        'X-Requested-With': 'XMLHttpRequest' // 서버가 AJAX임을 인식하도록 추가!
    };
    headers[csrfHeaderMeta.getAttribute('content')] = csrfMeta.getAttribute('content');

    fetch(url, {
        method: 'POST',
        headers: headers,
        body: JSON.stringify(requestData)
    })
    .then(res => res.text())
    .then(result => {
        if(result === 'success') {
            alert('저장되었습니다.');
            location.reload(); 
        } else {
            alert('저장에 실패했습니다.');
        }
    })
    .catch(err => {
        console.error(err);
        alert('서버 통신 중 오류가 발생했습니다.');
    });
}

// 5. 스케줄 삭제
function deleteFlight(flightId) {
    if(!confirm('이 운항 스케줄을 정말 취소(삭제)하시겠습니까?')) return;
    
    const csrfMeta = document.querySelector('meta[name="_csrf"]');
    const csrfHeaderMeta = document.querySelector('meta[name="_csrf_header"]');
    
    if (!csrfMeta || !csrfHeaderMeta) {
        alert('보안 토큰(CSRF)을 찾을 수 없습니다.');
        return;
    }

    const headers = {
        'X-Requested-With': 'XMLHttpRequest'
    };
    headers[csrfHeaderMeta.getAttribute('content')] = csrfMeta.getAttribute('content');

    fetch('/staff/schedule/api/delete?flightId=' + flightId, {
        method: 'POST',
        headers: headers
    })
    .then(res => res.text())
    .then(result => {
        if(result === 'success') {
            alert('삭제되었습니다.');
            location.reload();
        } else {
            alert('삭제에 실패했습니다.');
        }
    })
    .catch(err => {
        console.error(err);
        alert('서버 통신 중 오류가 발생했습니다.');
    });
}

// 6. 오늘 항공편 상태(정상/지연/결항) 변경
function changeFlightStatus(flightId, newStatus) {
    if(!newStatus) return;
    
    if(!confirm('해당 항공편의 상태를 변경하시겠습니까?')) {
        location.reload();
        return;
    }

    const csrfMeta = document.querySelector('meta[name="_csrf"]');
    const csrfHeaderMeta = document.querySelector('meta[name="_csrf_header"]');
    
    if (!csrfMeta || !csrfHeaderMeta) {
        alert('보안 토큰(CSRF)을 찾을 수 없습니다.');
        return;
    }

    const headers = { 
        'Content-Type': 'application/json',
        'X-Requested-With': 'XMLHttpRequest' // 서버가 AJAX임을 인식하도록 추가!
    };
    headers[csrfHeaderMeta.getAttribute('content')] = csrfMeta.getAttribute('content');

    fetch('/staff/schedule/api/status', {
        method: 'POST',
        headers: headers,
        body: JSON.stringify({
            flight_id: parseInt(flightId),
            flight_status: newStatus
        })
    })
    .then(res => res.text())
    .then(result => {
        if(result === 'success') {
            alert('상태가 정상적으로 변경되었습니다.');
            location.reload();
        } else {
            alert('상태 변경에 실패했습니다.');
        }
    })
    .catch(err => {
        console.error(err);
        alert('서버 통신 중 오류가 발생했습니다.');
    });
}