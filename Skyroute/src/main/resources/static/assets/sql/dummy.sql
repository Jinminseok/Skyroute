-- =============================================================================
-- SkyRoute 예약/결제 병렬 개발용 최소 더미데이터
-- 전제: DDL 방금 새로 돌려서 관련 테이블이 비어있는 상태
--       MEMBER는 a팀이 이미 시드해둔 상태(BOOKING 등에서 참조)
-- 범위: 지상직(운항/운임) 미구현분을 대신해 예약이 참조하는 상류 데이터만 채움
--       BOOKING/TICKET/PAYMENT는 d/e팀 코드가 생성하므로 비워둠
-- 게이트는 nullable이라 GATE/GATE_AREA 생략
-- =============================================================================

-- 1) 노선 유형 ------------------------------------------------------------
INSERT INTO ROUTE_TYPE (type_name, route_price) VALUES ('단거리', 50000);
INSERT INTO ROUTE_TYPE (type_name, route_price) VALUES ('중거리', 120000);
INSERT INTO ROUTE_TYPE (type_name, route_price) VALUES ('장거리', 200000);

-- 2) 권역 ----------------------------------------------------------------
INSERT INTO REGION (region_name) VALUES ('국내');
INSERT INTO REGION (region_name) VALUES ('아시아');

-- 3) 좌석 등급 (class_ratio: 등급 보정값) -----------------------------------
INSERT INTO SEAT_CLASS (class_name, "ROWS", "COLUMNS", class_ratio, sort_order)
VALUES ('비즈니스', 5, 4, 2.0, 1);
INSERT INTO SEAT_CLASS (class_name, "ROWS", "COLUMNS", class_ratio, sort_order)
VALUES ('이코노미', 20, 6, 1.0, 2);

-- 4) 시즌 (더미는 1개로 통일 → FARE 계산 시 시즌 모호성 없음) ------------------
INSERT INTO SEASON (season_name, start_date, end_date, season_ratio)
VALUES ('2026년 성수기', DATE '2026-01-01', DATE '2026-07-31', 1.8);
INSERT INTO SEASON (season_name, start_date, end_date, season_ratio)
VALUES ('2026년 일반', DATE '2026-08-01', DATE '2026-09-01', 1.0);
INSERT INTO SEASON (season_name, start_date, end_date, season_ratio)
VALUES ('2026년 비수기', DATE '2026-09-02', DATE '2026-10-31', 0.8);

-- 5) 공항 ----------------------------------------------------------------
INSERT INTO AIRPORT (iata_code, airport_name, country, timezone, region_id, flight_type)
VALUES ('GMP', '김포국제공항', '대한민국', 'Asia/Seoul',
        (SELECT region_id FROM REGION WHERE region_name='국내'), 'DOM');
INSERT INTO AIRPORT (iata_code, airport_name, country, timezone, region_id, flight_type)
VALUES ('CJU', '제주국제공항', '일본', 'Asia/Seoul',
        (SELECT region_id FROM REGION WHERE region_name='아시아'), 'DOM');

-- 6) 항공기 (더미 좌석 8석과 맞춤) -----------------------------------------
INSERT INTO AIRCRAFT (reg_no, model_name, total_seats)
VALUES ('HL8001', 'B737-800', 8);

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

-- 8) 노선 (왕복 테스트용 GMP<->CJU 양방향) ---------------------------------
INSERT INTO ROUTE (departure_airport_id, arrival_airport_id, flight_type, route_type_id)
VALUES ((SELECT airport_id FROM AIRPORT WHERE iata_code='GMP'),
        (SELECT airport_id FROM AIRPORT WHERE iata_code='CJU'),
        'DOM', (SELECT route_type_id FROM ROUTE_TYPE WHERE type_name='국내선'));
INSERT INTO ROUTE (departure_airport_id, arrival_airport_id, flight_type, route_type_id)
VALUES ((SELECT airport_id FROM AIRPORT WHERE iata_code='CJU'),
        (SELECT airport_id FROM AIRPORT WHERE iata_code='GMP'),
        'DOM', (SELECT route_type_id FROM ROUTE_TYPE WHERE type_name='국내선'));

-- 9) 운임 FARE (route x seat_class x season, 스냅샷 계산) --------------------
--    price = route_price * season_ratio * class_ratio, 100원 단위 반올림
INSERT INTO FARE (route_id, seat_class_id, season_id, price, is_active)
SELECT r.route_id, sc.seat_class_id, s.season_id,
       ROUND(rt.route_price * s.season_ratio * sc.class_ratio, -2), 'Y'
FROM ROUTE r
JOIN ROUTE_TYPE rt ON rt.route_type_id = r.route_type_id
CROSS JOIN SEASON s
CROSS JOIN SEAT_CLASS sc
WHERE r.is_active='Y' AND s.is_active='Y' AND sc.is_active='Y';

-- 10) 항공편 FLIGHT (항상 미래 시각으로 → SYSTIMESTAMP 기준) ------------------
--     KE101 GMP→CJU (편도/왕복 가는편), KE102 CJU→GMP (왕복 오는편), KE103 GMP→CJU (편도 대체편)
INSERT INTO FLIGHT (flight_no, route_id, aircraft_id, departure_time, arrival_time)
VALUES ('KE101',
        (SELECT route_id FROM ROUTE
          WHERE departure_airport_id=(SELECT airport_id FROM AIRPORT WHERE iata_code='GMP')
            AND arrival_airport_id  =(SELECT airport_id FROM AIRPORT WHERE iata_code='CJU')),
        (SELECT aircraft_id FROM AIRCRAFT WHERE reg_no='HL8001'),
        SYSTIMESTAMP + INTERVAL '7' DAY,
        SYSTIMESTAMP + INTERVAL '7' DAY + INTERVAL '70' MINUTE);
INSERT INTO FLIGHT (flight_no, route_id, aircraft_id, departure_time, arrival_time)
VALUES ('KE102',
        (SELECT route_id FROM ROUTE
          WHERE departure_airport_id=(SELECT airport_id FROM AIRPORT WHERE iata_code='CJU')
            AND arrival_airport_id  =(SELECT airport_id FROM AIRPORT WHERE iata_code='GMP')),
        (SELECT aircraft_id FROM AIRCRAFT WHERE reg_no='HL8001'),
        SYSTIMESTAMP + INTERVAL '10' DAY,
        SYSTIMESTAMP + INTERVAL '10' DAY + INTERVAL '70' MINUTE);
INSERT INTO FLIGHT (flight_no, route_id, aircraft_id, departure_time, arrival_time)
VALUES ('KE103',
        (SELECT route_id FROM ROUTE
          WHERE departure_airport_id=(SELECT airport_id FROM AIRPORT WHERE iata_code='GMP')
            AND arrival_airport_id  =(SELECT airport_id FROM AIRPORT WHERE iata_code='CJU')),
        (SELECT aircraft_id FROM AIRCRAFT WHERE reg_no='HL8001'),
        SYSTIMESTAMP + INTERVAL '14' DAY,
        SYSTIMESTAMP + INTERVAL '14' DAY + INTERVAL '70' MINUTE);

-- 11) 항공편 운임 FLIGHT_FARE (항공편의 노선+출발일 시즌에 맞는 FARE 복사) --------
INSERT INTO FLIGHT_FARE (flight_id, seat_class_id, fare_id, price)
SELECT f.flight_id, fa.seat_class_id, fa.fare_id, fa.price
FROM FLIGHT f
JOIN FARE fa   ON fa.route_id = f.route_id AND fa.is_active = 'Y'
JOIN SEASON se ON fa.season_id = se.season_id
              AND CAST(f.departure_time AS DATE) BETWEEN se.start_date AND se.end_date;

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

COMMIT;