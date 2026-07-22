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

-- ============================================
-- GATE_AREA 테이블 데이터 추가
-- ============================================
INSERT INTO GATE_AREA (AREA_NAME)
VALUES ('D구역');

INSERT INTO GATE_AREA (AREA_NAME)
VALUES ('E구역');

INSERT INTO GATE_AREA (AREA_NAME)
VALUES ('F구역');

COMMIT;

-- ============================================
-- SEASON 테이블 데이터 추가
-- ============================================
MERGE INTO SEASON S
USING (
    SELECT
        '2026년 성수기' AS SEASON_NAME,
        DATE '2026-01-01' AS START_DATE,
        DATE '2026-06-30' AS END_DATE,
        1.8 AS SEASON_RATIO
    FROM DUAL

    UNION ALL

    SELECT
        '2026년 여름 극성수기',
        DATE '2026-07-01',
        DATE '2026-08-15',
        2.0
    FROM DUAL

    UNION ALL

    SELECT
        '2026년 비성수기',
        DATE '2026-08-16',
        DATE '2026-10-01',
        0.8
    FROM DUAL

    UNION ALL

    SELECT
        '2026년 일반',
        DATE '2026-10-02',
        DATE '2026-11-15',
        1.0
    FROM DUAL

    UNION ALL

    SELECT
        '2026년 겨울 극성수기',
        DATE '2026-11-16',
        DATE '2026-12-31',
        2.0
    FROM DUAL
) SRC
ON (S.SEASON_NAME = SRC.SEASON_NAME)

WHEN MATCHED THEN
    UPDATE SET
        S.START_DATE   = SRC.START_DATE,
        S.END_DATE     = SRC.END_DATE,
        S.SEASON_RATIO = SRC.SEASON_RATIO,
        S.IS_ACTIVE    = 'Y'

WHEN NOT MATCHED THEN
    INSERT (
        SEASON_NAME,
        START_DATE,
        END_DATE,
        SEASON_RATIO,
        IS_ACTIVE
    )
    VALUES (
        SRC.SEASON_NAME,
        SRC.START_DATE,
        SRC.END_DATE,
        SRC.SEASON_RATIO,
        'Y'
    );

COMMIT;

-- ============================================
-- SEASON 테이블 데이터 수정 & 추가
-- ============================================
MERGE INTO SEASON S
USING (
    SELECT
        '2026년 성수기' AS SEASON_NAME,
        DATE '2026-01-01' AS START_DATE,
        DATE '2026-06-30' AS END_DATE,
        1.8 AS SEASON_RATIO
    FROM DUAL

    UNION ALL

    SELECT
        '2026년 여름 극성수기',
        DATE '2026-07-01',
        DATE '2026-08-15',
        2.0
    FROM DUAL

    UNION ALL

    SELECT
        '2026년 비성수기',
        DATE '2026-08-16',
        DATE '2026-10-01',
        0.8
    FROM DUAL

    UNION ALL

    SELECT
        '2026년 일반',
        DATE '2026-10-02',
        DATE '2026-11-15',
        1.0
    FROM DUAL

    UNION ALL

    SELECT
        '2026년 겨울 극성수기',
        DATE '2026-11-16',
        DATE '2026-12-31',
        2.0
    FROM DUAL
) SRC
ON (S.SEASON_NAME = SRC.SEASON_NAME)

WHEN MATCHED THEN
    UPDATE SET
        S.START_DATE   = SRC.START_DATE,
        S.END_DATE     = SRC.END_DATE,
        S.SEASON_RATIO = SRC.SEASON_RATIO,
        S.IS_ACTIVE    = 'Y'

WHEN NOT MATCHED THEN
    INSERT (
        SEASON_NAME,
        START_DATE,
        END_DATE,
        SEASON_RATIO,
        IS_ACTIVE
    )
    VALUES (
        SRC.SEASON_NAME,
        SRC.START_DATE,
        SRC.END_DATE,
        SRC.SEASON_RATIO,
        'Y'
    );

COMMIT;

-- ============================================
-- AIRPORT 테이블 데이터 추가
-- ============================================
MERGE INTO AIRPORT A
USING (
    SELECT
        SRC.IATA_CODE,
        SRC.AIRPORT_NAME,
        SRC.COUNTRY,
        SRC.TIMEZONE,
        R.REGION_ID,
        SRC.FLIGHT_TYPE
    FROM (
        /* 국내: 대한민국 공항 3개 */
        SELECT 'GMP' AS IATA_CODE,
               '김포국제공항' AS AIRPORT_NAME,
               '대한민국' AS COUNTRY,
               'Asia/Seoul' AS TIMEZONE,
               '국내' AS REGION_NAME,
               'DOM' AS FLIGHT_TYPE
        FROM DUAL

        UNION ALL

        SELECT 'PUS',
               '김해국제공항',
               '대한민국',
               'Asia/Seoul',
               '국내',
               'DOM'
        FROM DUAL

        UNION ALL

        SELECT 'CJU',
               '제주국제공항',
               '대한민국',
               'Asia/Seoul',
               '국내',
               'DOM'
        FROM DUAL

        /* 아시아: 일본, 중국, 싱가포르 */
        UNION ALL

        SELECT 'NRT',
               '나리타 국제공항',
               '일본',
               'Asia/Tokyo',
               '아시아',
               'INT'
        FROM DUAL

        UNION ALL

        SELECT 'PEK',
               '베이징 서우두 국제공항',
               '중국',
               'Asia/Shanghai',
               '아시아',
               'INT'
        FROM DUAL

        UNION ALL

        SELECT 'SIN',
               '싱가포르 창이공항',
               '싱가포르',
               'Asia/Singapore',
               '아시아',
               'INT'
        FROM DUAL

        /* 유럽: 영국, 프랑스, 독일 */
        UNION ALL

        SELECT 'LHR',
               '런던 히드로 공항',
               '영국',
               'Europe/London',
               '유럽',
               'INT'
        FROM DUAL

        UNION ALL

        SELECT 'CDG',
               '파리 샤를 드골 공항',
               '프랑스',
               'Europe/Paris',
               '유럽',
               'INT'
        FROM DUAL

        UNION ALL

        SELECT 'FRA',
               '프랑크푸르트 공항',
               '독일',
               'Europe/Berlin',
               '유럽',
               'INT'
        FROM DUAL

        /* 미주: 미국, 캐나다, 브라질 */
        UNION ALL

        SELECT 'JFK',
               '존 F. 케네디 국제공항',
               '미국',
               'America/New_York',
               '미주',
               'INT'
        FROM DUAL

        UNION ALL

        SELECT 'YYZ',
               '토론토 피어슨 국제공항',
               '캐나다',
               'America/Toronto',
               '미주',
               'INT'
        FROM DUAL

        UNION ALL

        SELECT 'GRU',
               '상파울루 과룰류스 국제공항',
               '브라질',
               'America/Sao_Paulo',
               '미주',
               'INT'
        FROM DUAL

        /* 대양주: 호주, 뉴질랜드, 피지 */
        UNION ALL

        SELECT 'SYD',
               '시드니 킹스포드 스미스 공항',
               '호주',
               'Australia/Sydney',
               '대양주',
               'INT'
        FROM DUAL

        UNION ALL

        SELECT 'AKL',
               '오클랜드 국제공항',
               '뉴질랜드',
               'Pacific/Auckland',
               '대양주',
               'INT'
        FROM DUAL

        UNION ALL

        SELECT 'NAN',
               '나디 국제공항',
               '피지',
               'Pacific/Fiji',
               '대양주',
               'INT'
        FROM DUAL

        /* 기타: 아랍에미리트, 카타르, 남아프리카공화국 */
        UNION ALL

        SELECT 'DXB',
               '두바이 국제공항',
               '아랍에미리트',
               'Asia/Dubai',
               '기타',
               'INT'
        FROM DUAL

        UNION ALL

        SELECT 'DOH',
               '도하 하마드 국제공항',
               '카타르',
               'Asia/Qatar',
               '기타',
               'INT'
        FROM DUAL

        UNION ALL

        SELECT 'JNB',
               '요하네스버그 O.R. 탐보 국제공항',
               '남아프리카공화국',
               'Africa/Johannesburg',
               '기타',
               'INT'
        FROM DUAL
    ) SRC
    JOIN REGION R
      ON R.REGION_NAME = SRC.REGION_NAME
     AND R.IS_ACTIVE = 'Y'
) DATA
ON (A.IATA_CODE = DATA.IATA_CODE)

WHEN NOT MATCHED THEN
    INSERT (
        IATA_CODE,
        AIRPORT_NAME,
        COUNTRY,
        TIMEZONE,
        REGION_ID,
        FLIGHT_TYPE,
        IS_ACTIVE
    )
    VALUES (
        DATA.IATA_CODE,
        DATA.AIRPORT_NAME,
        DATA.COUNTRY,
        DATA.TIMEZONE,
        DATA.REGION_ID,
        DATA.FLIGHT_TYPE,
        'Y'
    );

COMMIT;

MERGE INTO AIRPORT A
USING (
    SELECT
        'ICN' AS IATA_CODE,
        '인천국제공항' AS AIRPORT_NAME,
        '대한민국' AS COUNTRY,
        'Asia/Seoul' AS TIMEZONE,
        R.REGION_ID,
        'INT' AS FLIGHT_TYPE
    FROM REGION R
    WHERE R.REGION_NAME = '국내'
      AND R.IS_ACTIVE = 'Y'
) SRC
ON (A.IATA_CODE = SRC.IATA_CODE)

WHEN MATCHED THEN
    UPDATE SET
        A.AIRPORT_NAME = SRC.AIRPORT_NAME,
        A.COUNTRY      = SRC.COUNTRY,
        A.TIMEZONE     = SRC.TIMEZONE,
        A.REGION_ID    = SRC.REGION_ID,
        A.FLIGHT_TYPE  = SRC.FLIGHT_TYPE,
        A.IS_ACTIVE    = 'Y'

WHEN NOT MATCHED THEN
    INSERT (
        IATA_CODE,
        AIRPORT_NAME,
        COUNTRY,
        TIMEZONE,
        REGION_ID,
        FLIGHT_TYPE,
        IS_ACTIVE
    )
    VALUES (
        SRC.IATA_CODE,
        SRC.AIRPORT_NAME,
        SRC.COUNTRY,
        SRC.TIMEZONE,
        SRC.REGION_ID,
        SRC.FLIGHT_TYPE,
        'Y'
    );

COMMIT;

-- ============================================
-- GATE 테이블 데이터 추가
-- ============================================
MERGE INTO GATE G
USING (
    SELECT
        A.AIRPORT_ID,
        A.AREA_LETTER || TO_CHAR(N.GATE_NO) AS GATE_CODE,
        GA.GATE_AREA_ID,
        A.FLIGHT_TYPE
    FROM (
        SELECT
            AIRPORT_ID,
            FLIGHT_TYPE,
            CHR(
                65 + MOD(
                    ROW_NUMBER() OVER (ORDER BY IATA_CODE) - 1,
                    6
                )
            ) AS AREA_LETTER
        FROM AIRPORT
        WHERE IS_ACTIVE = 'Y'
    ) A
    CROSS JOIN (
        SELECT 1 AS GATE_NO FROM DUAL
        UNION ALL
        SELECT 2 AS GATE_NO FROM DUAL
    ) N
    JOIN GATE_AREA GA
      ON GA.AREA_NAME = A.AREA_LETTER || '구역'
     AND GA.IS_ACTIVE = 'Y'
) SRC
ON (
       G.AIRPORT_ID = SRC.AIRPORT_ID
   AND G.GATE_CODE  = SRC.GATE_CODE
)

WHEN NOT MATCHED THEN
    INSERT (
        AIRPORT_ID,
        GATE_CODE,
        GATE_AREA_ID,
        FLIGHT_TYPE,
        IS_ACTIVE
    )
    VALUES (
        SRC.AIRPORT_ID,
        SRC.GATE_CODE,
        SRC.GATE_AREA_ID,
        SRC.FLIGHT_TYPE,
        'Y'
    );

COMMIT;

MERGE INTO GATE G
USING (
    SELECT
        A.AIRPORT_ID,
        GD.GATE_CODE,
        GA.GATE_AREA_ID,
        'INT' AS FLIGHT_TYPE
    FROM AIRPORT A
    CROSS JOIN (
        SELECT 'A1' AS GATE_CODE FROM DUAL
        UNION ALL
        SELECT 'A2' AS GATE_CODE FROM DUAL
    ) GD
    JOIN GATE_AREA GA
      ON GA.AREA_NAME = 'A구역'
     AND GA.IS_ACTIVE = 'Y'
    WHERE A.IATA_CODE = 'ICN'
      AND A.IS_ACTIVE = 'Y'
) SRC
ON (
       G.AIRPORT_ID = SRC.AIRPORT_ID
   AND G.GATE_CODE  = SRC.GATE_CODE
)

WHEN MATCHED THEN
    UPDATE SET
        G.GATE_AREA_ID = SRC.GATE_AREA_ID,
        G.FLIGHT_TYPE  = SRC.FLIGHT_TYPE,
        G.IS_ACTIVE    = 'Y'

WHEN NOT MATCHED THEN
    INSERT (
        AIRPORT_ID,
        GATE_CODE,
        GATE_AREA_ID,
        FLIGHT_TYPE,
        IS_ACTIVE
    )
    VALUES (
        SRC.AIRPORT_ID,
        SRC.GATE_CODE,
        SRC.GATE_AREA_ID,
        SRC.FLIGHT_TYPE,
        'Y'
    );

COMMIT;


-- ============================================
-- ROUTE 테이블 데이터 추가
-- ============================================
MERGE INTO ROUTE R
USING (
    SELECT
        DEP.AIRPORT_ID AS DEPARTURE_AIRPORT_ID,
        ARR.AIRPORT_ID AS ARRIVAL_AIRPORT_ID,
        RD.FLIGHT_TYPE,
        RT.ROUTE_TYPE_ID
    FROM (
        /* =========================================================
           국내선: 모든 국내 공항 조합을 양방향으로 등록
           ========================================================= */

        SELECT 'GMP' AS DEP_IATA,
               'PUS' AS ARR_IATA,
               'DOM' AS FLIGHT_TYPE,
               '단거리' AS TYPE_NAME
        FROM DUAL

        UNION ALL
        SELECT 'PUS', 'GMP', 'DOM', '단거리' FROM DUAL

        UNION ALL
        SELECT 'GMP', 'CJU', 'DOM', '단거리' FROM DUAL

        UNION ALL
        SELECT 'CJU', 'GMP', 'DOM', '단거리' FROM DUAL

        UNION ALL
        SELECT 'PUS', 'CJU', 'DOM', '단거리' FROM DUAL

        UNION ALL
        SELECT 'CJU', 'PUS', 'DOM', '단거리' FROM DUAL


        /* =========================================================
           아시아 국제선
           ========================================================= */

        UNION ALL
        SELECT 'ICN', 'NRT', 'INT', '단거리' FROM DUAL

        UNION ALL
        SELECT 'NRT', 'ICN', 'INT', '단거리' FROM DUAL

        UNION ALL
        SELECT 'ICN', 'PEK', 'INT', '단거리' FROM DUAL

        UNION ALL
        SELECT 'PEK', 'ICN', 'INT', '단거리' FROM DUAL

        UNION ALL
        SELECT 'ICN', 'SIN', 'INT', '중거리' FROM DUAL

        UNION ALL
        SELECT 'SIN', 'ICN', 'INT', '중거리' FROM DUAL


        /* =========================================================
           유럽 국제선
           ========================================================= */

        UNION ALL
        SELECT 'ICN', 'LHR', 'INT', '장거리' FROM DUAL

        UNION ALL
        SELECT 'LHR', 'ICN', 'INT', '장거리' FROM DUAL

        UNION ALL
        SELECT 'ICN', 'CDG', 'INT', '장거리' FROM DUAL

        UNION ALL
        SELECT 'CDG', 'ICN', 'INT', '장거리' FROM DUAL

        UNION ALL
        SELECT 'ICN', 'FRA', 'INT', '장거리' FROM DUAL

        UNION ALL
        SELECT 'FRA', 'ICN', 'INT', '장거리' FROM DUAL


        /* =========================================================
           미주 국제선
           ========================================================= */

        UNION ALL
        SELECT 'ICN', 'JFK', 'INT', '장거리' FROM DUAL

        UNION ALL
        SELECT 'JFK', 'ICN', 'INT', '장거리' FROM DUAL

        UNION ALL
        SELECT 'ICN', 'YYZ', 'INT', '장거리' FROM DUAL

        UNION ALL
        SELECT 'YYZ', 'ICN', 'INT', '장거리' FROM DUAL

        UNION ALL
        SELECT 'ICN', 'GRU', 'INT', '장거리' FROM DUAL

        UNION ALL
        SELECT 'GRU', 'ICN', 'INT', '장거리' FROM DUAL


        /* =========================================================
           대양주 국제선
           ========================================================= */

        UNION ALL
        SELECT 'ICN', 'SYD', 'INT', '장거리' FROM DUAL

        UNION ALL
        SELECT 'SYD', 'ICN', 'INT', '장거리' FROM DUAL

        UNION ALL
        SELECT 'ICN', 'AKL', 'INT', '장거리' FROM DUAL

        UNION ALL
        SELECT 'AKL', 'ICN', 'INT', '장거리' FROM DUAL

        UNION ALL
        SELECT 'ICN', 'NAN', 'INT', '장거리' FROM DUAL

        UNION ALL
        SELECT 'NAN', 'ICN', 'INT', '장거리' FROM DUAL


        /* =========================================================
           기타 권역: 중동·아프리카
           ========================================================= */

        UNION ALL
        SELECT 'ICN', 'DXB', 'INT', '장거리' FROM DUAL

        UNION ALL
        SELECT 'DXB', 'ICN', 'INT', '장거리' FROM DUAL

        UNION ALL
        SELECT 'ICN', 'DOH', 'INT', '장거리' FROM DUAL

        UNION ALL
        SELECT 'DOH', 'ICN', 'INT', '장거리' FROM DUAL

        UNION ALL
        SELECT 'ICN', 'JNB', 'INT', '장거리' FROM DUAL

        UNION ALL
        SELECT 'JNB', 'ICN', 'INT', '장거리' FROM DUAL
    ) RD
    JOIN AIRPORT DEP
      ON DEP.IATA_CODE = RD.DEP_IATA
     AND DEP.IS_ACTIVE = 'Y'
    JOIN AIRPORT ARR
      ON ARR.IATA_CODE = RD.ARR_IATA
     AND ARR.IS_ACTIVE = 'Y'
    JOIN ROUTE_TYPE RT
      ON RT.TYPE_NAME = RD.TYPE_NAME
     AND RT.IS_ACTIVE = 'Y'
) SRC
ON (
       R.DEPARTURE_AIRPORT_ID = SRC.DEPARTURE_AIRPORT_ID
   AND R.ARRIVAL_AIRPORT_ID   = SRC.ARRIVAL_AIRPORT_ID
)

WHEN MATCHED THEN
    UPDATE SET
        R.FLIGHT_TYPE  = SRC.FLIGHT_TYPE,
        R.ROUTE_TYPE_ID = SRC.ROUTE_TYPE_ID,
        R.IS_ACTIVE     = 'Y'

WHEN NOT MATCHED THEN
    INSERT (
        DEPARTURE_AIRPORT_ID,
        ARRIVAL_AIRPORT_ID,
        FLIGHT_TYPE,
        ROUTE_TYPE_ID,
        IS_ACTIVE
    )
    VALUES (
        SRC.DEPARTURE_AIRPORT_ID,
        SRC.ARRIVAL_AIRPORT_ID,
        SRC.FLIGHT_TYPE,
        SRC.ROUTE_TYPE_ID,
        'Y'
    );

COMMIT;

-- ============================================
-- AIRCRAFT 테이블 데이터 추가
-- ============================================
MERGE INTO AIRCRAFT A
USING (
    SELECT 'SR-A001' AS REG_NO,
           'Airbus A321-200' AS MODEL_NAME,
           180 AS TOTAL_SEATS,
           '운항가능' AS STATUS_NAME
    FROM DUAL

    UNION ALL

    SELECT 'SR-A002',
           'Airbus A321neo',
           195,
           '운항가능'
    FROM DUAL

    UNION ALL

    SELECT 'SR-B001',
           'Boeing 737-800',
           189,
           '운항가능'
    FROM DUAL

    UNION ALL

    SELECT 'SR-B002',
           'Boeing 737-900ER',
           215,
           '운항가능'
    FROM DUAL

    UNION ALL

    SELECT 'SR-A003',
           'Airbus A330-300',
           290,
           '운항가능'
    FROM DUAL

    UNION ALL

    SELECT 'SR-B003',
           'Boeing 787-9',
           290,
           '운항가능'
    FROM DUAL

    UNION ALL

    SELECT 'SR-A004',
           'Airbus A350-900',
           325,
           '운항가능'
    FROM DUAL

    UNION ALL

    SELECT 'SR-B004',
           'Boeing 777-300ER',
           350,
           '운항가능'
    FROM DUAL

    UNION ALL

    SELECT 'SR-A005',
           'Airbus A330-200',
           250,
           '운항가능'
    FROM DUAL

    UNION ALL

    SELECT 'SR-B005',
           'Boeing 787-10',
           330,
           '운항가능'
    FROM DUAL
) SRC
ON (A.REG_NO = SRC.REG_NO)

WHEN MATCHED THEN
    UPDATE SET
        A.MODEL_NAME  = SRC.MODEL_NAME,
        A.TOTAL_SEATS = SRC.TOTAL_SEATS,
        A.STATUS_NAME = SRC.STATUS_NAME,
        A.IS_ACTIVE   = 'Y'

WHEN NOT MATCHED THEN
    INSERT (
        REG_NO,
        MODEL_NAME,
        TOTAL_SEATS,
        STATUS_NAME,
        IS_ACTIVE
    )
    VALUES (
        SRC.REG_NO,
        SRC.MODEL_NAME,
        SRC.TOTAL_SEATS,
        SRC.STATUS_NAME,
        'Y'
    );

COMMIT;

-- ============================================
-- SEAT 테이블 데이터 추가
-- ============================================
MERGE INTO SEAT S
USING (
    WITH AIRCRAFT_LAYOUT AS (
        /* 등록번호, 일등석 수, 비즈니스 수, 이코노미 수,
           일등석 열 수, 비즈니스 열 수, 이코노미 열 수 */

        SELECT
            'SR-A001' AS REG_NO,
            8 AS FIRST_COUNT,
            20 AS BUSINESS_COUNT,
            152 AS ECONOMY_COUNT,
            4 AS FIRST_COLUMNS,
            4 AS BUSINESS_COLUMNS,
            6 AS ECONOMY_COLUMNS
        FROM DUAL

        UNION ALL
        SELECT 'SR-A002', 8, 20, 167, 4, 4, 6 FROM DUAL

        UNION ALL
        SELECT 'SR-B001', 8, 16, 165, 4, 4, 6 FROM DUAL

        UNION ALL
        SELECT 'SR-B002', 8, 24, 183, 4, 4, 6 FROM DUAL

        UNION ALL
        SELECT 'SR-A003', 12, 30, 248, 4, 6, 8 FROM DUAL

        UNION ALL
        SELECT 'SR-B003', 12, 30, 248, 4, 6, 9 FROM DUAL

        UNION ALL
        SELECT 'SR-A004', 12, 40, 273, 4, 8, 9 FROM DUAL

        UNION ALL
        SELECT 'SR-B004', 16, 48, 286, 4, 8, 10 FROM DUAL

        UNION ALL
        SELECT 'SR-A005', 12, 24, 214, 4, 6, 8 FROM DUAL

        UNION ALL
        SELECT 'SR-B005', 12, 42, 276, 4, 6, 9 FROM DUAL
    ),

    CLASS_LAYOUT AS (
        /* 일등석: 1행부터 시작 */
        SELECT
            REG_NO,
            '일등석' AS CLASS_NAME,
            FIRST_COUNT AS SEAT_COUNT,
            FIRST_COLUMNS AS COLUMN_COUNT,
            1 AS START_ROW
        FROM AIRCRAFT_LAYOUT

        UNION ALL

        /* 비즈니스: 일등석 마지막 행 다음부터 시작 */
        SELECT
            REG_NO,
            '비즈니스',
            BUSINESS_COUNT,
            BUSINESS_COLUMNS,
            1 + CEIL(FIRST_COUNT / FIRST_COLUMNS)
        FROM AIRCRAFT_LAYOUT

        UNION ALL

        /* 이코노미: 비즈니스 마지막 행 다음부터 시작 */
        SELECT
            REG_NO,
            '이코노미',
            ECONOMY_COUNT,
            ECONOMY_COLUMNS,
            1
            + CEIL(FIRST_COUNT / FIRST_COLUMNS)
            + CEIL(BUSINESS_COUNT / BUSINESS_COLUMNS)
        FROM AIRCRAFT_LAYOUT
    ),

    NUMBERS AS (
        SELECT LEVEL AS SEAT_SEQ
        FROM DUAL
        CONNECT BY LEVEL <= 400
    )

    SELECT
        A.AIRCRAFT_ID,

        /* 예: 1A, 1B, 2A, 12C */
        TO_CHAR(
            CL.START_ROW
            + TRUNC((N.SEAT_SEQ - 1) / CL.COLUMN_COUNT)
        )
        ||
        CHR(
            65 + MOD(N.SEAT_SEQ - 1, CL.COLUMN_COUNT)
        ) AS SEAT_NO,

        SC.SEAT_CLASS_ID,
        'Y' AS IS_ACTIVE

    FROM CLASS_LAYOUT CL

    JOIN AIRCRAFT A
      ON A.REG_NO = CL.REG_NO
     AND A.IS_ACTIVE = 'Y'

    JOIN SEAT_CLASS SC
      ON SC.CLASS_NAME = CL.CLASS_NAME

    JOIN NUMBERS N
      ON N.SEAT_SEQ <= CL.SEAT_COUNT
) SRC
ON (
       S.AIRCRAFT_ID = SRC.AIRCRAFT_ID
   AND S.SEAT_NO = SRC.SEAT_NO
)

WHEN NOT MATCHED THEN
    INSERT (
        AIRCRAFT_ID,
        SEAT_NO,
        SEAT_CLASS_ID,
        IS_ACTIVE
    )
    VALUES (
        SRC.AIRCRAFT_ID,
        SRC.SEAT_NO,
        SRC.SEAT_CLASS_ID,
        SRC.IS_ACTIVE
    );

COMMIT;

-- ============================================
-- FARE 테이블 데이터 추가
-- ============================================
MERGE INTO FARE F
USING (
    SELECT
        R.ROUTE_ID,
        SC.SEAT_CLASS_ID,
        S.SEASON_ID,

        /* StaffFareMapper.xml과 같은 계산식 */
        RT.ROUTE_PRICE
            * S.SEASON_RATIO
            * SC.CLASS_RATIO AS CALCULATED_PRICE

    FROM ROUTE R

    JOIN ROUTE_TYPE RT
      ON RT.ROUTE_TYPE_ID = R.ROUTE_TYPE_ID

    CROSS JOIN SEAT_CLASS SC
    CROSS JOIN SEASON S

    WHERE R.IS_ACTIVE = 'Y'
      AND RT.IS_ACTIVE = 'Y'
      AND S.IS_ACTIVE = 'Y'
) SRC
ON (
       F.ROUTE_ID      = SRC.ROUTE_ID
   AND F.SEAT_CLASS_ID = SRC.SEAT_CLASS_ID
   AND F.SEASON_ID     = SRC.SEASON_ID
)

WHEN MATCHED THEN
    UPDATE SET
        F.PRICE     = SRC.CALCULATED_PRICE,
        F.IS_ACTIVE = 'Y'

WHEN NOT MATCHED THEN
    INSERT (
        ROUTE_ID,
        SEAT_CLASS_ID,
        SEASON_ID,
        PRICE,
        IS_ACTIVE
    )
    VALUES (
        SRC.ROUTE_ID,
        SRC.SEAT_CLASS_ID,
        SRC.SEASON_ID,
        SRC.CALCULATED_PRICE,
        'Y'
    );
    
COMMIT;

-- ============================================
-- FLIGHT, FLIGHT_FARE 테이블 데이터 추가
-- ============================================
SAVEPOINT BEFORE_ROTATION_FLIGHT;

MERGE INTO FLIGHT F
USING (
    WITH FLIGHT_PLAN AS (
        /* =========================================================
           SR-A001 : PUS → GMP → CJU → PUS
           국내선 회항 준비시간 60분
           ========================================================= */
        SELECT
            'SR101' AS FLIGHT_NO,
            'SR-A001' AS REG_NO,
            'PUS' AS DEP_IATA,
            'GMP' AS ARR_IATA,
            TIMESTAMP '2026-07-24 06:30:00' AS DEPARTURE_TIME,
            TIMESTAMP '2026-07-24 07:40:00' AS ARRIVAL_TIME,
            1 AS DEP_GATE_SEQ,
            1 AS ARR_GATE_SEQ
        FROM DUAL

        UNION ALL
        SELECT
            'SR102', 'SR-A001', 'GMP', 'CJU',
            TIMESTAMP '2026-07-24 08:40:00',
            TIMESTAMP '2026-07-24 09:50:00',
            1, 1
        FROM DUAL

        UNION ALL
        SELECT
            'SR103', 'SR-A001', 'CJU', 'PUS',
            TIMESTAMP '2026-07-24 10:50:00',
            TIMESTAMP '2026-07-24 11:50:00',
            1, 1
        FROM DUAL


        /* =========================================================
           SR-A002 : GMP → PUS → CJU → GMP
           ========================================================= */
        UNION ALL
        SELECT
            'SR104', 'SR-A002', 'GMP', 'PUS',
            TIMESTAMP '2026-07-24 07:00:00',
            TIMESTAMP '2026-07-24 08:10:00',
            2, 2
        FROM DUAL

        UNION ALL
        SELECT
            'SR105', 'SR-A002', 'PUS', 'CJU',
            TIMESTAMP '2026-07-24 09:10:00',
            TIMESTAMP '2026-07-24 10:10:00',
            2, 2
        FROM DUAL

        UNION ALL
        SELECT
            'SR106', 'SR-A002', 'CJU', 'GMP',
            TIMESTAMP '2026-07-24 11:10:00',
            TIMESTAMP '2026-07-24 12:20:00',
            2, 2
        FROM DUAL


        /* =========================================================
           SR-B001 : ICN → NRT → ICN
           국제선 현지 회항 준비시간 90분
           ========================================================= */
        UNION ALL
        SELECT
            'SR107', 'SR-B001', 'ICN', 'NRT',
            TIMESTAMP '2026-07-24 08:00:00',
            TIMESTAMP '2026-07-24 10:30:00',
            2, 1
        FROM DUAL

        UNION ALL
        SELECT
            'SR108', 'SR-B001', 'NRT', 'ICN',
            TIMESTAMP '2026-07-24 12:00:00',
            TIMESTAMP '2026-07-24 14:30:00',
            1, 2
        FROM DUAL


        /* =========================================================
           SR-B002 : ICN → PEK → ICN
           ========================================================= */
        UNION ALL
        SELECT
            'SR109', 'SR-B002', 'ICN', 'PEK',
            TIMESTAMP '2026-07-24 09:00:00',
            TIMESTAMP '2026-07-24 11:20:00',
            1, 1
        FROM DUAL

        UNION ALL
        SELECT
            'SR110', 'SR-B002', 'PEK', 'ICN',
            TIMESTAMP '2026-07-24 12:50:00',
            TIMESTAMP '2026-07-24 15:10:00',
            1, 1
        FROM DUAL


        /* =========================================================
           SR-A003 : ICN → SIN → ICN → DXB → ICN
           ========================================================= */
        UNION ALL
        SELECT
            'SR111', 'SR-A003', 'ICN', 'SIN',
            TIMESTAMP '2026-07-24 07:30:00',
            TIMESTAMP '2026-07-24 14:00:00',
            1, 1
        FROM DUAL

        UNION ALL
        SELECT
            'SR112', 'SR-A003', 'SIN', 'ICN',
            TIMESTAMP '2026-07-24 16:00:00',
            TIMESTAMP '2026-07-24 22:30:00',
            1, 1
        FROM DUAL

        UNION ALL
        SELECT
            'SR113', 'SR-A003', 'ICN', 'DXB',
            TIMESTAMP '2026-07-25 08:30:00',
            TIMESTAMP '2026-07-25 18:30:00',
            2, 1
        FROM DUAL

        UNION ALL
        SELECT
            'SR114', 'SR-A003', 'DXB', 'ICN',
            TIMESTAMP '2026-07-25 21:30:00',
            TIMESTAMP '2026-07-26 07:30:00',
            1, 2
        FROM DUAL


        /* =========================================================
           SR-A005 : ICN → DOH → ICN → SYD → ICN
           ========================================================= */
        UNION ALL
        SELECT
            'SR115', 'SR-A005', 'ICN', 'DOH',
            TIMESTAMP '2026-07-24 10:00:00',
            TIMESTAMP '2026-07-24 20:20:00',
            2, 1
        FROM DUAL

        UNION ALL
        SELECT
            'SR116', 'SR-A005', 'DOH', 'ICN',
            TIMESTAMP '2026-07-24 23:20:00',
            TIMESTAMP '2026-07-25 09:40:00',
            1, 2
        FROM DUAL

        UNION ALL
        SELECT
            'SR117', 'SR-A005', 'ICN', 'SYD',
            TIMESTAMP '2026-07-26 10:30:00',
            TIMESTAMP '2026-07-26 21:00:00',
            1, 1
        FROM DUAL

        UNION ALL
        SELECT
            'SR118', 'SR-A005', 'SYD', 'ICN',
            TIMESTAMP '2026-07-26 23:30:00',
            TIMESTAMP '2026-07-27 10:00:00',
            1, 1
        FROM DUAL


        /* =========================================================
           SR-B003 : ICN → JFK → ICN → YYZ → ICN
           ========================================================= */
        UNION ALL
        SELECT
            'SR119', 'SR-B003', 'ICN', 'JFK',
            TIMESTAMP '2026-07-24 11:00:00',
            TIMESTAMP '2026-07-25 01:00:00',
            1, 1
        FROM DUAL

        UNION ALL
        SELECT
            'SR120', 'SR-B003', 'JFK', 'ICN',
            TIMESTAMP '2026-07-25 04:00:00',
            TIMESTAMP '2026-07-25 18:00:00',
            1, 1
        FROM DUAL

        UNION ALL
        SELECT
            'SR121', 'SR-B003', 'ICN', 'YYZ',
            TIMESTAMP '2026-07-26 11:00:00',
            TIMESTAMP '2026-07-27 00:00:00',
            2, 1
        FROM DUAL

        UNION ALL
        SELECT
            'SR122', 'SR-B003', 'YYZ', 'ICN',
            TIMESTAMP '2026-07-27 03:00:00',
            TIMESTAMP '2026-07-27 16:00:00',
            1, 2
        FROM DUAL


        /* =========================================================
           SR-A004 : ICN → CDG → ICN → FRA → ICN
           ========================================================= */
        UNION ALL
        SELECT
            'SR123', 'SR-A004', 'ICN', 'CDG',
            TIMESTAMP '2026-07-24 12:00:00',
            TIMESTAMP '2026-07-25 02:00:00',
            2, 1
        FROM DUAL

        UNION ALL
        SELECT
            'SR124', 'SR-A004', 'CDG', 'ICN',
            TIMESTAMP '2026-07-25 05:00:00',
            TIMESTAMP '2026-07-25 18:30:00',
            1, 2
        FROM DUAL

        UNION ALL
        SELECT
            'SR125', 'SR-A004', 'ICN', 'FRA',
            TIMESTAMP '2026-07-26 12:00:00',
            TIMESTAMP '2026-07-27 01:00:00',
            1, 1
        FROM DUAL

        UNION ALL
        SELECT
            'SR126', 'SR-A004', 'FRA', 'ICN',
            TIMESTAMP '2026-07-27 04:00:00',
            TIMESTAMP '2026-07-27 17:00:00',
            1, 1
        FROM DUAL


        /* =========================================================
           SR-B004 : ICN → LHR → ICN → JNB → ICN
           ========================================================= */
        UNION ALL
        SELECT
            'SR127', 'SR-B004', 'ICN', 'LHR',
            TIMESTAMP '2026-07-24 13:00:00',
            TIMESTAMP '2026-07-25 03:30:00',
            1, 1
        FROM DUAL

        UNION ALL
        SELECT
            'SR128', 'SR-B004', 'LHR', 'ICN',
            TIMESTAMP '2026-07-25 06:30:00',
            TIMESTAMP '2026-07-25 21:00:00',
            1, 1
        FROM DUAL

        UNION ALL
        SELECT
            'SR129', 'SR-B004', 'ICN', 'JNB',
            TIMESTAMP '2026-07-27 09:00:00',
            TIMESTAMP '2026-07-27 23:30:00',
            2, 1
        FROM DUAL

        UNION ALL
        SELECT
            'SR130', 'SR-B004', 'JNB', 'ICN',
            TIMESTAMP '2026-07-28 02:30:00',
            TIMESTAMP '2026-07-28 17:00:00',
            1, 2
        FROM DUAL


        /* =========================================================
           SR-B005 : ICN → AKL → ICN → NAN → ICN → GRU → ICN
           ========================================================= */
        UNION ALL
        SELECT
            'SR131', 'SR-B005', 'ICN', 'AKL',
            TIMESTAMP '2026-07-24 14:00:00',
            TIMESTAMP '2026-07-25 01:30:00',
            2, 1
        FROM DUAL

        UNION ALL
        SELECT
            'SR132', 'SR-B005', 'AKL', 'ICN',
            TIMESTAMP '2026-07-25 04:30:00',
            TIMESTAMP '2026-07-25 16:00:00',
            1, 2
        FROM DUAL

        UNION ALL
        SELECT
            'SR133', 'SR-B005', 'ICN', 'NAN',
            TIMESTAMP '2026-07-26 08:00:00',
            TIMESTAMP '2026-07-26 18:00:00',
            1, 1
        FROM DUAL

        UNION ALL
        SELECT
            'SR134', 'SR-B005', 'NAN', 'ICN',
            TIMESTAMP '2026-07-26 21:00:00',
            TIMESTAMP '2026-07-27 07:00:00',
            1, 1
        FROM DUAL

        UNION ALL
        SELECT
            'SR135', 'SR-B005', 'ICN', 'GRU',
            TIMESTAMP '2026-07-29 07:00:00',
            TIMESTAMP '2026-07-30 07:00:00',
            2, 1
        FROM DUAL

        UNION ALL
        SELECT
            'SR136', 'SR-B005', 'GRU', 'ICN',
            TIMESTAMP '2026-07-30 11:00:00',
            TIMESTAMP '2026-07-31 11:00:00',
            1, 2
        FROM DUAL
    ),

    /* 동일 노선이 중복 등록되어도 가장 작은 ROUTE_ID 하나만 사용 */
    ROUTE_MAP AS (
        SELECT
            R.ROUTE_ID,
            R.FLIGHT_TYPE,
            TRIM(DEP.IATA_CODE) AS DEP_IATA,
            TRIM(ARR.IATA_CODE) AS ARR_IATA,
            ROW_NUMBER() OVER (
                PARTITION BY
                    TRIM(DEP.IATA_CODE),
                    TRIM(ARR.IATA_CODE)
                ORDER BY R.ROUTE_ID
            ) AS ROUTE_SEQ
        FROM ROUTE R
        JOIN AIRPORT DEP
          ON DEP.AIRPORT_ID = R.DEPARTURE_AIRPORT_ID
        JOIN AIRPORT ARR
          ON ARR.AIRPORT_ID = R.ARRIVAL_AIRPORT_ID
        WHERE R.IS_ACTIVE = 'Y'
          AND DEP.IS_ACTIVE = 'Y'
          AND ARR.IS_ACTIVE = 'Y'
    ),

    /* 공항별 활성 게이트에 1, 2 순번 부여 */
    GATE_RANKED AS (
        SELECT
            G.GATE_ID,
            G.AIRPORT_ID,
            G.FLIGHT_TYPE,
            G.GATE_CODE,
            ROW_NUMBER() OVER (
                PARTITION BY G.AIRPORT_ID, G.FLIGHT_TYPE
                ORDER BY G.GATE_CODE, G.GATE_ID
            ) AS GATE_SEQ
        FROM GATE G
        WHERE G.IS_ACTIVE = 'Y'
    )

    SELECT
        P.FLIGHT_NO,
        RM.ROUTE_ID,
        AC.AIRCRAFT_ID,
        DG.GATE_ID AS DEPARTURE_GATE_ID,
        AG.GATE_ID AS ARRIVAL_GATE_ID,
        P.DEPARTURE_TIME,
        P.ARRIVAL_TIME
    FROM FLIGHT_PLAN P

    JOIN ROUTE_MAP RM
      ON RM.DEP_IATA = P.DEP_IATA
     AND RM.ARR_IATA = P.ARR_IATA
     AND RM.ROUTE_SEQ = 1

    JOIN AIRCRAFT AC
      ON AC.REG_NO = P.REG_NO
     AND AC.IS_ACTIVE = 'Y'
     AND AC.STATUS_NAME = '운항가능'

    JOIN AIRPORT DEP
      ON TRIM(DEP.IATA_CODE) = P.DEP_IATA

    JOIN AIRPORT ARR
      ON TRIM(ARR.IATA_CODE) = P.ARR_IATA

    JOIN GATE_RANKED DG
      ON DG.AIRPORT_ID = DEP.AIRPORT_ID
     AND DG.FLIGHT_TYPE = RM.FLIGHT_TYPE
     AND DG.GATE_SEQ = P.DEP_GATE_SEQ

    JOIN GATE_RANKED AG
      ON AG.AIRPORT_ID = ARR.AIRPORT_ID
     AND AG.FLIGHT_TYPE = RM.FLIGHT_TYPE
     AND AG.GATE_SEQ = P.ARR_GATE_SEQ
) SRC

ON (
    F.FLIGHT_NO = SRC.FLIGHT_NO
)

WHEN MATCHED THEN
    UPDATE SET
        F.ROUTE_ID          = SRC.ROUTE_ID,
        F.AIRCRAFT_ID       = SRC.AIRCRAFT_ID,
        F.DEPARTURE_GATE_ID = SRC.DEPARTURE_GATE_ID,
        F.ARRIVAL_GATE_ID   = SRC.ARRIVAL_GATE_ID,
        F.DEPARTURE_TIME    = SRC.DEPARTURE_TIME,
        F.ARRIVAL_TIME      = SRC.ARRIVAL_TIME,
        F.FLIGHT_STATUS     = 'SCHEDULED',
        F.DELAY_MINUTES     = 0,
        F.IS_DELETED        = 'N',
        F.UPDATED_AT        = SYSTIMESTAMP

WHEN NOT MATCHED THEN
    INSERT (
        FLIGHT_NO,
        ROUTE_ID,
        AIRCRAFT_ID,
        DEPARTURE_GATE_ID,
        ARRIVAL_GATE_ID,
        DEPARTURE_TIME,
        ARRIVAL_TIME,
        FLIGHT_STATUS,
        DELAY_MINUTES,
        IS_DELETED
    )
    VALUES (
        SRC.FLIGHT_NO,
        SRC.ROUTE_ID,
        SRC.AIRCRAFT_ID,
        SRC.DEPARTURE_GATE_ID,
        SRC.ARRIVAL_GATE_ID,
        SRC.DEPARTURE_TIME,
        SRC.ARRIVAL_TIME,
        'SCHEDULED',
        0,
        'N'
    );
    
INSERT INTO FLIGHT_FARE (
    FLIGHT_ID,
    SEAT_CLASS_ID,
    FARE_ID,
    PRICE
)
SELECT
    F.FLIGHT_ID,
    FA.SEAT_CLASS_ID,
    FA.FARE_ID,
    FA.PRICE
FROM FLIGHT F

JOIN FARE FA
  ON FA.ROUTE_ID = F.ROUTE_ID
 AND FA.IS_ACTIVE = 'Y'

JOIN SEASON S
  ON S.SEASON_ID = FA.SEASON_ID
 AND S.IS_ACTIVE = 'Y'
 AND CAST(F.DEPARTURE_TIME AS DATE) >= TRUNC(S.START_DATE)
 AND CAST(F.DEPARTURE_TIME AS DATE) < TRUNC(S.END_DATE) + 1

WHERE REGEXP_LIKE(
    F.FLIGHT_NO,
    '^SR1(0[1-9]|[12][0-9]|3[0-6])$'
)
AND F.IS_DELETED = 'N';
COMMIT;