-- =============================================================================
-- SkyRoute 예약/결제 병렬 개발용 최소 더미데이터
-- =============================================================================

-- 노선 유형 ------------------------------------------------------------
INSERT INTO ROUTE_TYPE (type_name, route_price) VALUES ('단거리', 50000);
INSERT INTO ROUTE_TYPE (type_name, route_price) VALUES ('중거리', 120000);
INSERT INTO ROUTE_TYPE (type_name, route_price) VALUES ('장거리', 200000);

-- 권역 ----------------------------------------------------------------
INSERT INTO REGION (region_name) VALUES ('국내');
INSERT INTO REGION (region_name) VALUES ('아시아');
INSERT INTO REGION (region_name) VALUES ('유럽');

-- 게이트 구역 -----------------------------------
INSERT INTO GATE_AREA (area_name) VALUES ('A구역');
INSERT INTO GATE_AREA (area_name) VALUES ('B구역');
INSERT INTO GATE_AREA (area_name) VALUES ('C구역');

-- 시즌 ------------------
INSERT INTO SEASON (season_name, start_date, end_date, season_ratio)
VALUES ('2026년 성수기', DATE '2026-01-01', DATE '2026-07-31', 1.8);
INSERT INTO SEASON (season_name, start_date, end_date, season_ratio)
VALUES ('2026년 일반', DATE '2026-08-01', DATE '2026-09-01', 1.0);
INSERT INTO SEASON (season_name, start_date, end_date, season_ratio)
VALUES ('2026년 비수기', DATE '2026-09-02', DATE '2026-10-31', 0.8);

-- 공항 ----------------------------------------------------------------
INSERT INTO AIRPORT (iata_code, airport_name, country, timezone, region_id, flight_type)
VALUES ('GMP', '김포국제공항', '대한민국', 'Asia/Seoul',
        (SELECT region_id FROM REGION WHERE region_name='국내'), 'DOM');
INSERT INTO AIRPORT (iata_code, airport_name, country, timezone, region_id, flight_type)
VALUES ('CJU', '제주국제공항', '대한민국', 'Asia/Seoul',
        (SELECT region_id FROM REGION WHERE region_name='국내'), 'DOM');
INSERT INTO AIRPORT (iata_code, airport_name, country, timezone, region_id, flight_type)
VALUES ('PUS', '김해국제공항', '대한민국', 'Asia/Seoul',
        (SELECT region_id FROM REGION WHERE region_name='국내'), 'DOM');
INSERT INTO AIRPORT (iata_code, airport_name, country, timezone, region_id, flight_type)
VALUES ('NRT', '도쿄 나리타 국제공항', '일본', 'Asia/Seoul',
        (SELECT region_id FROM REGION WHERE region_name='아시아'), 'INT');
        
-- 항공기 -----------------------------------------
INSERT INTO AIRCRAFT (reg_no, model_name, total_seats)
VALUES ('HL8001', 'B737-800', 150);
INSERT INTO AIRCRAFT (reg_no, model_name, total_seats)
VALUES ('HL8002', 'B7-800', 200);
INSERT INTO AIRCRAFT (reg_no, model_name, total_seats)
VALUES ('HL8003', 'B777-300', 250);

-- 게이트 -----------------------------------------
INSERT INTO GATE (airport_id, gate_code, gate_area_id, flight_type)
VALUES ((SELECT airport_id FROM AIRPORT WHERE iata_code='GMP'), 'G1',
        (SELECT gate_area_id FROM GATE_AREA WHERE area_name='A구역'), 'DOM');
INSERT INTO GATE (airport_id, gate_code, gate_area_id, flight_type)
VALUES ((SELECT airport_id FROM AIRPORT WHERE iata_code='GMP'), 'G2',
        (SELECT gate_area_id FROM GATE_AREA WHERE area_name='B구역'), 'DOM');
-- CJU 게이트 : KE102 출발 게이트로 사용 (C1)
INSERT INTO GATE (airport_id, gate_code, gate_area_id, flight_type)
VALUES ((SELECT airport_id FROM AIRPORT WHERE iata_code='CJU'), 'C1',
        (SELECT gate_area_id FROM GATE_AREA WHERE area_name='A구역'), 'DOM');
INSERT INTO GATE (airport_id, gate_code, gate_area_id, flight_type)
VALUES ((SELECT airport_id FROM AIRPORT WHERE iata_code='CJU'), 'C2',
        (SELECT gate_area_id FROM GATE_AREA WHERE area_name='B구역'), 'DOM');

-- NRT 게이트 : KE201 도착 게이트로 사용 (국제선이라 flight_type='INT')
INSERT INTO GATE (airport_id, gate_code, gate_area_id, flight_type)
VALUES ((SELECT airport_id FROM AIRPORT WHERE iata_code='NRT'), 'N1',
        (SELECT gate_area_id FROM GATE_AREA WHERE area_name='C구역'), 'INT');

-- CJU: C1(A구역), C2(B구역)
INSERT INTO GATE (airport_id, gate_code, gate_area_id, flight_type)
VALUES ((SELECT airport_id FROM AIRPORT WHERE iata_code='CJU'), 'C1',
        (SELECT gate_area_id FROM GATE_AREA WHERE area_name='A구역'), 'DOM');
INSERT INTO GATE (airport_id, gate_code, gate_area_id, flight_type)
VALUES ((SELECT airport_id FROM AIRPORT WHERE iata_code='CJU'), 'C2',
        (SELECT gate_area_id FROM GATE_AREA WHERE area_name='B구역'), 'DOM');

-- 좌석 등급 (class_ratio: 등급 보정값) -----------------------------------
INSERT INTO SEAT_CLASS (class_name, class_ratio, sort_order)
VALUES ('일등석', 3.0, 1);
INSERT INTO SEAT_CLASS (class_name, class_ratio, sort_order)
VALUES ('비즈니스', 2.0, 2);
INSERT INTO SEAT_CLASS (class_name, class_ratio, sort_order)
VALUES ('이코노미', 1.0, 3);

-- 7) 좌석 (비즈 2 + 이코노미 6 = 8석) --------------------------------------
INSERT INTO SEAT (aircraft_id, seat_no, seat_class_id)
SELECT a.aircraft_id, x.seat_no, sc.seat_class_id
FROM AIRCRAFT a
JOIN (
  SELECT '1A' seat_no, '비즈니스' cls FROM DUAL UNION ALL
  SELECT '1B', '비즈니스' FROM DUAL UNION ALL
  SELECT '2A', '이코노미' FROM DUAL UNION ALL
  SELECT '2B', '이코노미' FROM DUAL UNION ALL
  SELECT '2C', '이코노미' FROM DUAL UNION ALL
  SELECT '3A', '이코노미' FROM DUAL UNION ALL
  SELECT '3B', '이코노미' FROM DUAL UNION ALL
  SELECT '3C', '이코노미' FROM DUAL
) x ON 1=1
JOIN SEAT_CLASS sc ON sc.class_name = x.cls
WHERE a.reg_no = 'HL8001';

-- 노선 (왕복 테스트용 GMP<->CJU 양방향) ---------------------------------
-- 1) GMP -> CJU (국내 단거리) : KE101, KE103 가는편
INSERT INTO ROUTE (departure_airport_id, arrival_airport_id, flight_type, route_type_id)
VALUES ((SELECT airport_id FROM AIRPORT WHERE iata_code='GMP'),
        (SELECT airport_id FROM AIRPORT WHERE iata_code='CJU'),
        'DOM', (SELECT route_type_id FROM ROUTE_TYPE WHERE type_name='단거리'));

-- 2) CJU -> GMP (국내 단거리) : KE102 오는편 (왕복 테스트용)
INSERT INTO ROUTE (departure_airport_id, arrival_airport_id, flight_type, route_type_id)
VALUES ((SELECT airport_id FROM AIRPORT WHERE iata_code='CJU'),
        (SELECT airport_id FROM AIRPORT WHERE iata_code='GMP'),
        'DOM', (SELECT route_type_id FROM ROUTE_TYPE WHERE type_name='단거리'));

-- 3) GMP -> NRT (국제 중거리) : 국제선 케이스용 (선택)
INSERT INTO ROUTE (departure_airport_id, arrival_airport_id, flight_type, route_type_id)
VALUES ((SELECT airport_id FROM AIRPORT WHERE iata_code='GMP'),
        (SELECT airport_id FROM AIRPORT WHERE iata_code='NRT'),
        'INT', (SELECT route_type_id FROM ROUTE_TYPE WHERE type_name='중거리'));

-- 운임 FARE --------------------
-- FARE 생성 : route x seat_class x season 유효 조합 자동 생성
-- price = route_price * season_ratio * class_ratio, 100원 단위 반올림
INSERT INTO FARE (route_id, seat_class_id, season_id, price, is_active)
SELECT r.route_id, sc.seat_class_id, s.season_id,
       ROUND(rt.route_price * s.season_ratio * sc.class_ratio, -2), 'Y'
FROM ROUTE r
JOIN ROUTE_TYPE rt ON rt.route_type_id = r.route_type_id
CROSS JOIN SEASON s
CROSS JOIN SEAT_CLASS sc
WHERE r.is_active = 'Y' AND s.is_active = 'Y'
  AND NOT EXISTS (
      SELECT 1 FROM FARE f
      WHERE f.route_id = r.route_id
        AND f.seat_class_id = sc.seat_class_id
        AND f.season_id = s.season_id
  );

COMMIT;
-- 항공편 FLIGHT (항상 미래 시각으로 → SYSTIMESTAMP 기준) ------------------
-- KE101 GMP->CJU (HL8001) : 왕복 가는편 + 편도 테스트
INSERT INTO FLIGHT (flight_no, route_id, aircraft_id,
                    departure_gate_id, arrival_gate_id, departure_time, arrival_time)
VALUES ('KE101',
        (SELECT route_id FROM ROUTE
          WHERE departure_airport_id=(SELECT airport_id FROM AIRPORT WHERE iata_code='GMP')
            AND arrival_airport_id  =(SELECT airport_id FROM AIRPORT WHERE iata_code='CJU')),
        (SELECT aircraft_id FROM AIRCRAFT WHERE reg_no='HL8001'),
        (SELECT g.gate_id FROM GATE g JOIN AIRPORT a ON a.airport_id=g.airport_id
          WHERE a.iata_code='GMP' AND g.gate_code='G1'),   -- 출발 GMP/G1
        (SELECT g.gate_id FROM GATE g JOIN AIRPORT a ON a.airport_id=g.airport_id
          WHERE a.iata_code='CJU' AND g.gate_code='C1'),                                               -- 도착 CJU 게이트 없음 → NULL
        SYSTIMESTAMP + INTERVAL '7' DAY,
        SYSTIMESTAMP + INTERVAL '7' DAY + INTERVAL '70' MINUTE);

-- KE102 CJU->GMP (HL8001) : 왕복 오는편
INSERT INTO FLIGHT (flight_no, route_id, aircraft_id,
                    departure_gate_id, arrival_gate_id, departure_time, arrival_time)
VALUES ('KE102',
        (SELECT route_id FROM ROUTE
          WHERE departure_airport_id=(SELECT airport_id FROM AIRPORT WHERE iata_code='CJU')
            AND arrival_airport_id  =(SELECT airport_id FROM AIRPORT WHERE iata_code='GMP')),
        (SELECT aircraft_id FROM AIRCRAFT WHERE reg_no='HL8001'),
        (SELECT g.gate_id FROM GATE g JOIN AIRPORT a ON a.airport_id=g.airport_id
          WHERE a.iata_code='CJU' AND g.gate_code='C1'),                                               -- 출발 CJU 게이트 없음 → NULL
        (SELECT g.gate_id FROM GATE g JOIN AIRPORT a ON a.airport_id=g.airport_id
          WHERE a.iata_code='GMP' AND g.gate_code='G1'),   -- 도착 GMP/G1
        SYSTIMESTAMP + INTERVAL '10' DAY,
        SYSTIMESTAMP + INTERVAL '10' DAY + INTERVAL '70' MINUTE);

-- KE201 GMP->NRT (HL8003, 국제 중거리) : 국제선 케이스 (선택)
INSERT INTO FLIGHT (flight_no, route_id, aircraft_id,
                    departure_gate_id, arrival_gate_id, departure_time, arrival_time)
VALUES ('KE201',
        (SELECT route_id FROM ROUTE
          WHERE departure_airport_id=(SELECT airport_id FROM AIRPORT WHERE iata_code='GMP')
            AND arrival_airport_id  =(SELECT airport_id FROM AIRPORT WHERE iata_code='NRT')),
        (SELECT aircraft_id FROM AIRCRAFT WHERE reg_no='HL8003'),
        (SELECT g.gate_id FROM GATE g JOIN AIRPORT a ON a.airport_id=g.airport_id
          WHERE a.iata_code='GMP' AND g.gate_code='G2'),   -- 출발 GMP/G2
        (SELECT g.gate_id FROM GATE g JOIN AIRPORT a ON a.airport_id=g.airport_id
          WHERE a.iata_code='NRT' AND g.gate_code='A1'),                                               -- 도착 NRT 게이트 없음 → NULL
        SYSTIMESTAMP + INTERVAL '14' DAY,
        SYSTIMESTAMP + INTERVAL '14' DAY + INTERVAL '150' MINUTE);

-- 항공편 운임 FLIGHT_FARE (항공편의 노선+출발일 시즌에 맞는 FARE 복사) --------
INSERT INTO FLIGHT_FARE (flight_id, seat_class_id, fare_id, price)
SELECT f.flight_id, fa.seat_class_id, fa.fare_id, fa.price
FROM FLIGHT f
JOIN FARE fa   ON fa.route_id = f.route_id AND fa.is_active = 'Y'
JOIN SEASON se ON fa.season_id = se.season_id
              AND CAST(f.departure_time AS DATE) >= TRUNC(se.start_date)
              AND CAST(f.departure_time AS DATE) < TRUNC(se.end_date) + 1;
COMMIT;

-- =============================================================================
-- 확인용 (선택)
-- SELECT flight_no, seat_class_id, price FROM FLIGHT_FARE ff
--   JOIN FLIGHT f ON f.flight_id = ff.flight_id ORDER BY flight_no, seat_class_id;
-- =============================================================================
INSERT INTO AIRPORT (
    iata_code, airport_name, country, timezone, region_id, flight_type, is_active
)
VALUES (
    'PUS',
    '김해국제공항',
    '대한민국',
    'Asia/Seoul',
    (SELECT region_id FROM REGION WHERE region_name = '국내'),
    'DOM',
    'Y'
);

-- =============================================================================
-- FAQ 데이터
-- =============================================================================
-- 1. [예약/항공권] 카테고리
INSERT INTO FAQ (category, question, answer, is_visible, priority_num) 
VALUES ('예약/항공권', '예약 후 탑승자 이름을 변경할 수 있나요?', '안전 및 보안상의 이유로 예약이 완료된 후에는 원칙적으로 탑승자 이름을 변경할 수 없습니다. 단순 스펠링 오류의 경우, 탑승 전 고객센터로 신분증 사본을 보내주시면 확인 후 수정해 드립니다.', 'Y', 0);

INSERT INTO FAQ (category, question, answer, is_visible, priority_num) 
VALUES ('예약/항공권', '항공권 예약 시 좌석을 미리 지정할 수 있나요?', '네, 가능합니다. 항공권 예매 단계에서 원하시는 좌석을 선택하실 수 있으며, 예매 완료 후에도 [나의 여행] > [예약 조회/변경] 메뉴에서 언제든지 잔여 좌석을 지정하거나 변경하실 수 있습니다.', 'Y', 0);

-- 2. [결제/환불/취소] 카테고리
INSERT INTO FAQ (category, question, answer, is_visible, priority_num) 
VALUES ('결제/환불/취소', '환불 수수료는 얼마인가요?', '환불 수수료는 구매하신 항공권의 운임 규정 및 취소 시점(출발일 기준 남은 일수)에 따라 다르게 차등 부과됩니다. 자세한 수수료 규정은 홈페이지의 [운임 규정 안내]를 참고해 주시기 바랍니다.', 'Y', 0);

INSERT INTO FAQ (category, question, answer, is_visible, priority_num) 
VALUES ('결제/환불/취소', '어떤 결제수단을 지원하나요?', 'SkyRoute는 고객님의 편의를 위해 국내외 신용카드/체크카드 결제를 비롯하여, 간편결제(네이버페이, 카카오페이 등)와 실시간 계좌이체 서비스를 지원하고 있습니다.', 'Y', 0);

-- 3. [수하물] 카테고리
INSERT INTO FAQ (category, question, answer, is_visible, priority_num) 
VALUES ('수하물', '기내에 반입할 수 있는 수하물 무게와 크기 제한이 있나요?', '기내 반입 수하물은 승객 1인당 1개(무게 10kg 이하)로 제한되며, 세 모서리의 합이 115cm 이내(가로 55cm, 세로 40cm, 높이 20cm)여야 합니다. 이를 초과하는 수하물은 탑승 수속 시 위탁 수하물로 부쳐주셔야 합니다.', 'Y', 0);

INSERT INTO FAQ (category, question, answer, is_visible, priority_num) 
VALUES ('수하물', '위탁 수하물이 파손되거나 분실되면 어떻게 해야 하나요?', '수하물 수취 시 파손이나 지연, 분실을 확인하신 경우, 즉시 공항 내 위치한 SkyRoute 수하물 데스크에 신고해 주셔야 합니다. 공항을 이미 벗어나신 경우라면 7일 이내에 고객센터를 통해 사고 접수를 해주시기 바랍니다.', 'Y', 0);

-- 4. [탑승/수속] 카테고리
INSERT INTO FAQ (category, question, answer, is_visible, priority_num) 
VALUES ('탑승/수속', '공항에는 출발 몇 시간 전까지 도착해야 하나요?', '원활한 탑승 수속과 보안 검색을 위해 국내선은 출발 1시간 전, 국제선은 출발 2시간 30분 전까지 공항에 도착하시는 것을 권장합니다. 탑승구는 항공기 출발 15분 전에 마감됩니다.', 'Y', 0);

INSERT INTO FAQ (category, question, answer, is_visible, priority_num) 
VALUES ('탑승/수속', '모바일 탑승권만으로 수속이 가능한가요?', '네, 스마트폰으로 발급받으신 모바일 탑승권의 QR코드를 제시하시면 종이 탑승권 없이도 보안 검색대 통과 및 항공기 탑승이 모두 가능합니다.', 'Y', 0);

-- 5. [기타] 카테고리
INSERT INTO FAQ (category, question, answer, is_visible, priority_num) 
VALUES ('기타', '기내에 반려동물을 데리고 탈 수 있나요?', '네, 지정된 케이지에 넣은 소형 반려동물(개, 고양이, 새)에 한하여 기내 동반 탑승이 가능합니다. 단, 항공기당 탑승 가능한 반려동물의 수가 제한되어 있으므로 사전에 고객센터를 통해 예약을 확정해 주셔야 합니다.', 'Y', 0);

INSERT INTO FAQ (category, question, answer, is_visible, priority_num) 
VALUES ('기타', '기내식은 언제 제공되며, 특별식을 신청할 수 있나요?', '기내식은 이륙 후 안전 고도가 확보된 시점에 순차적으로 제공됩니다. 채식, 종교식, 알레르기 제한식 등의 특별 기내식은 항공편 출발 24시간 전까지 홈페이지를 통해 신청하실 수 있습니다.', 'Y', 0);

COMMIT;