-- ============================================
-- 테스트 회원 4명 (user01 ~ user04)
-- 비밀번호: 전부 1234 (BCrypt, strength 10)
-- ============================================

INSERT INTO MEMBER (login_id, password, name, email, phone, zipcode, address1, address2, role, status)
VALUES ('user01', '$2a$10$OWIoYMeZ7iWoAPO8jKnvwOKq567jsrzWizkv0cHMNYRhRiLW2RkHa',
        '김민준', 'user01@test.com', '010-1111-0001',
        '06236', '서울특별시 강남구 테헤란로 123', '101동 1001호', 'USER', 'ACTIVE');

INSERT INTO MEMBER (login_id, password, name, email, phone, zipcode, address1, address2, role, status)
VALUES ('user02', '$2a$10$hA3us/Q0JN6sNeq/4o.8vONVtyxFDhiiutchocA/nyZDDRR4pWjDy',
        '이서연', 'user02@test.com', '010-1111-0002',
        '04524', '서울특별시 중구 세종대로 110', '203호', 'USER', 'ACTIVE');

INSERT INTO MEMBER (login_id, password, name, email, phone, zipcode, address1, address2, role, status)
VALUES ('user03', '$2a$10$X0.hNbiZ632gvex79RULnO0HpybRJPD31VWgrmE5Kbd2ubitNWm.a',
        '박도윤', 'user03@test.com', '010-1111-0003',
        '13529', '경기도 성남시 분당구 판교역로 235', 'A동 502호', 'USER', 'ACTIVE');

INSERT INTO MEMBER (login_id, password, name, email, phone, zipcode, address1, address2, role, status)
VALUES ('user04', '$2a$10$m023fK3c0HFcnIkyLKqKjute2vbrokle0cl/7CNctYuNd6H6uhr3W',
        '최지우', 'user04@test.com', '010-1111-0004',
        '48058', '부산광역시 해운대구 센텀중앙로 55', '1203호', 'USER', 'ACTIVE');

COMMIT;

-- ============================================
-- FAQ 더미 데이터 15건
-- 카테고리: 예약/항공권, 결제/환불/취소, 수하물, 탑승/수속, 기타
-- priority_num: 전체 기준 노출 우선순위 1~10 (미지정 = 0)
-- ============================================

-- ---------- 예약/항공권 ----------
INSERT INTO FAQ (category, question, answer, is_visible, priority_num) VALUES
('예약/항공권', '항공권 예매는 출발 며칠 전까지 가능한가요?',
 'SkyRoute에서는 출발 시각 기준 2시간 전까지 예매가 가능합니다. 다만 성수기나 만석이 예상되는 노선은 조기에 마감될 수 있으니 여유 있게 예약해 주시기 바랍니다.', 'Y', 1);

INSERT INTO FAQ (category, question, answer, is_visible, priority_num) VALUES
('예약/항공권', '동승자를 함께 예약할 수 있나요?',
 '1회 예약당 최대 5명까지 동승자를 등록하실 수 있습니다. 예약 과정에서 각 탑승객의 이름, 생년월일, 성별을 입력해 주셔야 하며, 국제선의 경우 여권 정보가 추가로 필요합니다.', 'Y', 6);

INSERT INTO FAQ (category, question, answer, is_visible, priority_num) VALUES
('예약/항공권', '좌석은 언제 지정하나요?',
 '예약 과정에서 좌석을 직접 선택하실 수 있습니다. 좌석 선택 후 10분간 임시로 선점되며, 해당 시간 내에 결제가 완료되지 않으면 선점이 자동으로 해제됩니다.', 'Y', 0);

-- ---------- 결제/환불/취소 ----------
INSERT INTO FAQ (category, question, answer, is_visible, priority_num) VALUES
('결제/환불/취소', '예약을 취소하려면 어떻게 하나요?',
 '마이페이지 > 예약 내역에서 해당 예약을 선택한 뒤 취소를 요청하시면 됩니다. 취소가 완료되면 결제하신 수단으로 환불 절차가 자동으로 시작됩니다.', 'Y', 2);

INSERT INTO FAQ (category, question, answer, is_visible, priority_num) VALUES
('결제/환불/취소', '어떤 결제 수단을 사용할 수 있나요?',
 '신용카드와 카카오페이 결제를 지원합니다. 결제 수단별 한도 및 이용 조건은 각 카드사 또는 간편결제사의 정책을 따릅니다.', 'Y', 3);

INSERT INTO FAQ (category, question, answer, is_visible, priority_num) VALUES
('결제/환불/취소', '환불은 얼마나 걸리나요?',
 '카드 결제는 카드사 영업일 기준 3~5일, 간편결제는 2~3일 정도 소요됩니다. 정확한 환불 일정은 결제 수단별 정책에 따라 달라질 수 있습니다.', 'Y', 4);

-- ---------- 수하물 ----------
INSERT INTO FAQ (category, question, answer, is_visible, priority_num) VALUES
('수하물', '무료 위탁 수하물은 몇 kg까지인가요?',
 '이코노미 클래스는 23kg 1개, 비즈니스 클래스는 32kg 2개까지 무료로 위탁하실 수 있습니다. 초과분에 대해서는 공항 카운터에서 추가 요금이 부과됩니다.', 'Y', 5);

INSERT INTO FAQ (category, question, answer, is_visible, priority_num) VALUES
('수하물', '기내에 반입할 수 없는 물품은 무엇인가요?',
 '인화성 물질, 100ml를 초과하는 액체류, 날카로운 도구류 등은 기내 반입이 제한됩니다. 보조배터리는 반드시 기내에 휴대해야 하며 위탁 수하물로는 부치실 수 없습니다.', 'Y', 9);

INSERT INTO FAQ (category, question, answer, is_visible, priority_num) VALUES
('수하물', '수하물이 파손되거나 분실되었습니다.',
 '도착 공항의 수하물 서비스 카운터에서 즉시 신고해 주시기 바랍니다. 공항을 벗어난 이후에는 접수가 제한될 수 있으므로 반드시 현장에서 확인 후 신고해 주세요.', 'Y', 0);

-- ---------- 탑승/수속 ----------
INSERT INTO FAQ (category, question, answer, is_visible, priority_num) VALUES
('탑승/수속', '체크인은 언제부터 가능한가요?',
 '공항 카운터 체크인은 국내선 기준 출발 1시간 전, 국제선 기준 출발 3시간 전부터 가능합니다. 마감 시각 이후에는 탑승 수속이 제한되니 유의해 주시기 바랍니다.', 'Y', 7);

INSERT INTO FAQ (category, question, answer, is_visible, priority_num) VALUES
('탑승/수속', '탑승 게이트는 언제 확인할 수 있나요?',
 '탑승 게이트는 출발 약 1시간 전에 확정되며, 마이페이지 예약 상세 화면과 공항 안내 전광판에서 확인하실 수 있습니다. 게이트는 운항 사정에 따라 변경될 수 있습니다.', 'Y', 10);

INSERT INTO FAQ (category, question, answer, is_visible, priority_num) VALUES
('탑승/수속', '탑승 수속 시 어떤 신분증이 필요한가요?',
 '국내선은 주민등록증, 운전면허증 등 사진이 있는 신분증이 필요합니다. 국제선은 여권이 필수이며, 잔여 유효기간이 6개월 이상 남아 있어야 합니다.', 'Y', 0);

-- ---------- 기타 ----------
INSERT INTO FAQ (category, question, answer, is_visible, priority_num) VALUES
('기타', '항공편이 지연되면 어떻게 안내받나요?',
 '지연 또는 결항이 확정되면 예약 시 등록하신 이메일로 안내가 발송되며, 로그인 상태에서는 사이트 알림을 통해 실시간으로 확인하실 수 있습니다.', 'Y', 8);

INSERT INTO FAQ (category, question, answer, is_visible, priority_num) VALUES
('기타', '회원 탈퇴는 어떻게 하나요?',
 '마이페이지 > 회원정보에서 직접 탈퇴하실 수 있습니다. 진행 중인 예약이나 환불이 남아 있는 경우 처리가 완료된 후에 탈퇴가 가능합니다.', 'Y', 0);

INSERT INTO FAQ (category, question, answer, is_visible, priority_num) VALUES
('기타', '고객센터 운영 시간이 어떻게 되나요?',
 '고객센터는 평일 오전 9시부터 오후 6시까지 운영되며, 주말 및 공휴일은 휴무입니다. 운영 시간 외 문의는 챗봇을 이용해 주시기 바랍니다.', 'N', 0);

COMMIT;

-- ============================================
-- REGION 테이블 미주,유럽,대양주,기타 데이터 추가
-- ============================================
-- 미주
INSERT INTO REGION (REGION_NAME, IS_ACTIVE)
SELECT '미주', 'Y'
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1
    FROM REGION
    WHERE REGION_NAME = '미주'
);

/*
-- 유럽: 이미 존재하므로 중복이면 추가하지 않음
INSERT INTO REGION (REGION_NAME, IS_ACTIVE)
SELECT '유럽', 'Y'
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1
    FROM REGION
    WHERE REGION_NAME = '유럽'
);*/

-- 대양주
INSERT INTO REGION (REGION_NAME, IS_ACTIVE)
SELECT '대양주', 'Y'
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1
    FROM REGION
    WHERE REGION_NAME = '대양주'
);

-- 기타
INSERT INTO REGION (REGION_NAME, IS_ACTIVE)
SELECT '기타', 'Y'
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1
    FROM REGION
    WHERE REGION_NAME = '기타'
);

COMMIT;