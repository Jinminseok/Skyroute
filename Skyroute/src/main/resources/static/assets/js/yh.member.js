// 탭 전환 시스템
function go(id) {
    document.querySelectorAll('.panel').forEach(p => p.classList.toggle('on', p.id === id));
    document.querySelectorAll('nav button').forEach(b => b.classList.toggle('on', b.dataset.t === id));
    window.scrollTo({ top: 0, behavior: 'smooth' });
}

// 초기 로딩 시 네비게이션 버튼 바인딩
document.addEventListener("DOMContentLoaded", function() {
    document.querySelectorAll('nav button').forEach(b => {
        b.onclick = () => go(b.dataset.t);
    });

    // 동적 좌석 맵 생성 실행 (HTML에 #plane이 존재할 때만 실행)
    const planeContainer = document.getElementById('plane');
    if (planeContainer) {
        initSeatMap();
    }
});

// 좌석 맵 빌드 함수
function initSeatMap() {
    const taken = new Set(['20A', '20B', '21F', '22C', '23D', '24A', '24B', '25E', '26F', '27C', '28A']);
    const cols = ['A', 'B', 'C', 'D', 'E', 'F'];
    let planeHtml = '';

    for (let r = 20; r <= 30; r++) {
        planeHtml += '<div class="row"><div class="rn">' + r + '</div>';
        cols.forEach((c, i) => {
            if (i === 3) planeHtml += '<div class="aisle"></div>';
            const id = r + c;
            let cls = 'seat' + (taken.has(id) ? ' taken' : '') + (r <= 21 ? ' extra' : '');
            planeHtml += '<div class="' + cls + '" data-s="' + id + '">' + c + '</div>';
        });
        planeHtml += '</div>';
    }
    
    const planeElement = document.getElementById('plane');
    planeElement.innerHTML = planeHtml;

    let cur = null;
    document.querySelectorAll('.seat:not(.taken)').forEach(s => {
        s.onclick = () => {
            if (cur) cur.classList.remove('pick');
            s.classList.add('pick');
            cur = s;
            document.getElementById('seatpick').textContent = s.dataset.s;
            const extra = s.classList.contains('extra') ? 15000 : 0;
            document.getElementById('seattotal').textContent = (1596400 + extra).toLocaleString() + '원';
        };
    });
}

// 여정 타입 스왑 가상 이펙트
function triggerSwapEffect() {
    const swapBtn = document.querySelector('.swap');
    if(swapBtn) {
        swapBtn.style.transform = 'translateY(-50%) rotate(180deg)';
        setTimeout(() => swapBtn.style.transform = 'translateY(-50%)', 300);
    }
}

// --- 챗봇 비즈니스 로직 ---
const botAnswers = {
    '비회원': '본 예매 시스템은 비회원 예매를 완벽히 지원합니다. 예매 완료 시 발급되는 [예약번호(PNR)] 및 조회용 비밀번호를 통해 비회원도 체크인 및 예약 조회가 가능합니다.',
    '체크인': '온라인 모바일 체크인은 출발 시각 기준 국내선 정시 30분 전, 국제선은 1시간 전까지 수속이 가능합니다. 제한 시간이 지나면 공항 카운터를 이용하셔야 합니다.',
    '결항': '지상직 관제 시스템을 통해 항공편이 결항(CANCELLED) 코드로 전환되는 즉시, 해당 예약자들의 결제 내역은 수수료 없이 전액 자동 환불 승인이 처리되도록 시스템이 연동되어 있습니다.'
};

function toggleChatWindow() {
    const w = document.getElementById('chatWindow');
    w.classList.toggle('open');
    const icon = document.getElementById('chatIcon');
    icon.textContent = w.classList.contains('open') ? 'close' : 'forum';
}

function sendQuickQuery(text) {
    appendChatBubble(text, 'user');
    processBotLogic(text);
}

function sendUserChatMessage() {
    const input = document.getElementById('chatInputField');
    const text = input.value.trim();
    if (!text) return;
    appendChatBubble(text, 'user');
    input.value = '';
    processBotLogic(text);
}

function appendChatBubble(text, className) {
    const box = document.getElementById('chatBodyContainer');
    const b = document.createElement('div');
    b.className = 'chat-bubble ' + className;
    b.innerText = text;
    box.appendChild(b);
    box.scrollTop = box.scrollHeight;
}

function processBotLogic(text) {
    let response = "죄송합니다. 입력하신 문의 내역은 챗봇 학습 데이터 외의 항목입니다. 상세 지원이 필요하시면 상단 고객센터 메뉴를 통해 1:1 채팅 문의를 남겨주시면 현장 지상직 담당자(STAFF)가 답변해 드립니다.";

    if (text.includes('비회원') || text.includes('로그인')) {
        response = botAnswers['비회원'];
    } else if (text.includes('체크인') || text.includes('시간') || text.includes('수속')) {
        response = botAnswers['체크인'];
    } else if (text.includes('결항') || text.includes('취소') || text.includes('환불') || text.includes('보상')) {
        response = botAnswers['결항'];
    }

    setTimeout(() => {
        appendChatBubble(response, 'bot');
    }, 400);
}