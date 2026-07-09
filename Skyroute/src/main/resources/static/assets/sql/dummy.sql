/*
===============================================================================
SkyRoute 예약/결제 병렬 개발용 더미 데이터 RESET 스크립트 v4 SAFE
-------------------------------------------------------------------------------
변경점
- v2에서 AIRPORT/GATE/ROUTE 등 일부 구간이 INSERT ALL이라 기존 데이터가 있으면
  ORA-00001이 발생하던 문제 수정
- 기준/운영/예약 더미 데이터를 MERGE 또는 INSERT ... WHERE NOT EXISTS 방식으로 변경
- v3에서 FLIGHT_FARE 업데이트 시 ORA-30926이 발생하던 문제 수정
- FLIGHT_FARE.fare_id 컬럼이 있는 DB와 없는 DB를 모두 지원

실행 권장
- SQL Developer에서 파일 전체를 F5로 실행
- 이전 실행이 오류로 멈췄다면 먼저 ROLLBACK 후 실행

테스트 계정 비밀번호 원문
- 1234
===============================================================================
*/

SET DEFINE OFF;
SET SERVEROUTPUT ON;

ROLLBACK;
WHENEVER SQLERROR EXIT SQL.SQLCODE ROLLBACK;

PROMPT [0] 관련 테이블 기존 데이터 삭제 시작

/* =============================================================================
[0] 기존 데이터 삭제
- FK 의존성 때문에 자식 테이블부터 삭제한다.
- MEMBER는 계정 데이터 보호를 위해 삭제하지 않는다.
============================================================================= */

DELETE FROM FAVORITE_ROUTE;
DELETE FROM REFUND;
DELETE FROM PAYMENT;
DELETE FROM TICKET;
DELETE FROM BOOKING_PASSENGER;
DELETE FROM BOOKING;
DELETE FROM SAVED_PASSENGER;
DELETE FROM FLIGHT_NOTICE;
DELETE FROM FLIGHT_FARE;
DELETE FROM FLIGHT;
DELETE FROM FARE;
DELETE FROM SEAT;
DELETE FROM AIRCRAFT;
DELETE FROM ROUTE;
DELETE FROM GATE;
DELETE FROM AIRPORT;
DELETE FROM SEASON;
DELETE FROM SEAT_CLASS;
DELETE FROM ROUTE_TYPE;
DELETE FROM GATE_AREA;
DELETE FROM REGION;

PROMPT [0] 삭제 완료

/* =============================================================================
[1] 테스트 회원 보장
============================================================================= */

MERGE INTO MEMBER m
USING (
    SELECT 'dummy_user_pay' AS login_id,
           '$2a$10$F44IUxgXmKG5.ep1i4QVze8JRXhlU4OGAMVsv650SSymAF8Q/WLkG' AS password,
           '결제테스트유저' AS name,
           'dummy_user_pay@skyroute.test' AS email,
           '010-9000-0001' AS phone,
           '06234' AS zipcode,
           '서울특별시 강남구 테헤란로' AS address1,
           '101호' AS address2,
           'USER' AS role,
           'ACTIVE' AS status
    FROM dual
) src
ON (m.login_id = src.login_id)
WHEN MATCHED THEN
    UPDATE SET m.password = src.password,
               m.name = src.name,
               m.email = src.email,
               m.phone = src.phone,
               m.zipcode = src.zipcode,
               m.address1 = src.address1,
               m.address2 = src.address2,
               m.role = src.role,
               m.status = src.status
WHEN NOT MATCHED THEN
    INSERT (login_id, password, name, email, phone, zipcode, address1, address2, role, status)
    VALUES (src.login_id, src.password, src.name, src.email, src.phone, src.zipcode, src.address1, src.address2, src.role, src.status);

MERGE INTO MEMBER m
USING (
    SELECT 'dummy_user_refund' AS login_id,
           '$2a$10$F44IUxgXmKG5.ep1i4QVze8JRXhlU4OGAMVsv650SSymAF8Q/WLkG' AS password,
           '환불테스트유저' AS name,
           'dummy_user_refund@skyroute.test' AS email,
           '010-9000-0002' AS phone,
           '04524' AS zipcode,
           '서울특별시 중구 세종대로' AS address1,
           '202호' AS address2,
           'USER' AS role,
           'ACTIVE' AS status
    FROM dual
) src
ON (m.login_id = src.login_id)
WHEN MATCHED THEN
    UPDATE SET m.password = src.password,
               m.name = src.name,
               m.email = src.email,
               m.phone = src.phone,
               m.zipcode = src.zipcode,
               m.address1 = src.address1,
               m.address2 = src.address2,
               m.role = src.role,
               m.status = src.status
WHEN NOT MATCHED THEN
    INSERT (login_id, password, name, email, phone, zipcode, address1, address2, role, status)
    VALUES (src.login_id, src.password, src.name, src.email, src.phone, src.zipcode, src.address1, src.address2, src.role, src.status);

MERGE INTO MEMBER m
USING (
    SELECT 'dummy_staff_ops' AS login_id,
           '$2a$10$F44IUxgXmKG5.ep1i4QVze8JRXhlU4OGAMVsv650SSymAF8Q/WLkG' AS password,
           '운항테스트스태프' AS name,
           'dummy_staff_ops@skyroute.test' AS email,
           '010-9000-0003' AS phone,
           '06000' AS zipcode,
           '서울특별시 강남구 공항대로' AS address1,
           '303호' AS address2,
           'STAFF' AS role,
           'ACTIVE' AS status
    FROM dual
) src
ON (m.login_id = src.login_id)
WHEN MATCHED THEN
    UPDATE SET m.password = src.password,
               m.name = src.name,
               m.email = src.email,
               m.phone = src.phone,
               m.zipcode = src.zipcode,
               m.address1 = src.address1,
               m.address2 = src.address2,
               m.role = src.role,
               m.status = src.status
WHEN NOT MATCHED THEN
    INSERT (login_id, password, name, email, phone, zipcode, address1, address2, role, status)
    VALUES (src.login_id, src.password, src.name, src.email, src.phone, src.zipcode, src.address1, src.address2, src.role, src.status);

MERGE INTO MEMBER m
USING (
    SELECT 'dummy_admin_ops' AS login_id,
           '$2a$10$F44IUxgXmKG5.ep1i4QVze8JRXhlU4OGAMVsv650SSymAF8Q/WLkG' AS password,
           '관리자테스트계정' AS name,
           'dummy_admin_ops@skyroute.test' AS email,
           '010-9000-0004' AS phone,
           '03187' AS zipcode,
           '서울특별시 종로구 세종대로' AS address1,
           '404호' AS address2,
           'ADMIN' AS role,
           'ACTIVE' AS status
    FROM dual
) src
ON (m.login_id = src.login_id)
WHEN MATCHED THEN
    UPDATE SET m.password = src.password,
               m.name = src.name,
               m.email = src.email,
               m.phone = src.phone,
               m.zipcode = src.zipcode,
               m.address1 = src.address1,
               m.address2 = src.address2,
               m.role = src.role,
               m.status = src.status
WHEN NOT MATCHED THEN
    INSERT (login_id, password, name, email, phone, zipcode, address1, address2, role, status)
    VALUES (src.login_id, src.password, src.name, src.email, src.phone, src.zipcode, src.address1, src.address2, src.role, src.status);

PROMPT [1] 테스트 회원 보장 완료

/* =============================================================================
[2] ADMIN 기준 마스터 데이터
============================================================================= */

MERGE INTO REGION t
USING (
    SELECT '국내' region_name, 'Y' is_active FROM dual UNION ALL
    SELECT '일본', 'Y' FROM dual UNION ALL
    SELECT '동남아', 'Y' FROM dual UNION ALL
    SELECT '미주', 'Y' FROM dual UNION ALL
    SELECT '유럽', 'Y' FROM dual
) s
ON (t.region_name = s.region_name)
WHEN MATCHED THEN UPDATE SET t.is_active = s.is_active
WHEN NOT MATCHED THEN INSERT (region_name, is_active) VALUES (s.region_name, s.is_active);

MERGE INTO GATE_AREA t
USING (
    SELECT 'A구역' area_name, 'Y' is_active FROM dual UNION ALL
    SELECT 'B구역', 'Y' FROM dual UNION ALL
    SELECT 'C구역', 'Y' FROM dual UNION ALL
    SELECT 'D구역', 'Y' FROM dual UNION ALL
    SELECT 'E구역', 'Y' FROM dual
) s
ON (t.area_name = s.area_name)
WHEN MATCHED THEN UPDATE SET t.is_active = s.is_active
WHEN NOT MATCHED THEN INSERT (area_name, is_active) VALUES (s.area_name, s.is_active);

MERGE INTO ROUTE_TYPE t
USING (
    SELECT '초단거리' type_name, 'Y' is_active FROM dual UNION ALL
    SELECT '단거리', 'Y' FROM dual UNION ALL
    SELECT '중거리', 'Y' FROM dual UNION ALL
    SELECT '장거리', 'Y' FROM dual UNION ALL
    SELECT '초장거리', 'Y' FROM dual
) s
ON (t.type_name = s.type_name)
WHEN MATCHED THEN UPDATE SET t.is_active = s.is_active
WHEN NOT MATCHED THEN INSERT (type_name, is_active) VALUES (s.type_name, s.is_active);

MERGE INTO SEAT_CLASS t
USING (
    SELECT '퍼스트' class_name, 1 rows_val, 2 columns_val, 1 sort_order, 'Y' is_active FROM dual UNION ALL
    SELECT '비즈니스', 1, 2, 2, 'Y' FROM dual UNION ALL
    SELECT '프리미엄 이코노미', 1, 2, 3, 'Y' FROM dual UNION ALL
    SELECT '이코노미', 1, 6, 4, 'Y' FROM dual
) s
ON (t.class_name = s.class_name)
WHEN MATCHED THEN UPDATE SET t."ROWS" = s.rows_val,
                             t."COLUMNS" = s.columns_val,
                             t.sort_order = s.sort_order,
                             t.is_active = s.is_active
WHEN NOT MATCHED THEN INSERT (class_name, "ROWS", "COLUMNS", sort_order, is_active)
VALUES (s.class_name, s.rows_val, s.columns_val, s.sort_order, s.is_active);

MERGE INTO SEASON t
USING (
    SELECT '개발용 현재시즌' season_name, TRUNC(SYSDATE) - 30 start_date, TRUNC(SYSDATE) + 30 end_date, 'Y' is_active FROM dual UNION ALL
    SELECT '개발용 가을시즌', TRUNC(SYSDATE) + 31, TRUNC(SYSDATE) + 120, 'Y' FROM dual UNION ALL
    SELECT '개발용 겨울시즌', TRUNC(SYSDATE) + 121, TRUNC(SYSDATE) + 210, 'Y' FROM dual UNION ALL
    SELECT '개발용 봄시즌', TRUNC(SYSDATE) + 211, TRUNC(SYSDATE) + 300, 'Y' FROM dual
) s
ON (t.season_name = s.season_name)
WHEN MATCHED THEN UPDATE SET t.start_date = s.start_date,
                             t.end_date = s.end_date,
                             t.is_active = s.is_active
WHEN NOT MATCHED THEN INSERT (season_name, start_date, end_date, is_active)
VALUES (s.season_name, s.start_date, s.end_date, s.is_active);

PROMPT [2] 기준 마스터 데이터 생성 완료

/* =============================================================================
[3] 공항 / 게이트 / 노선 데이터
- 기존 데이터가 있어도 ORA-00001이 나지 않도록 MERGE/NOT EXISTS 사용
============================================================================= */

MERGE INTO AIRPORT t
USING (
    SELECT 'GMP' iata_code, '김포국제공항' airport_name, '대한민국' country, 'Asia/Seoul' timezone, (SELECT region_id FROM REGION WHERE region_name = '국내') region_id, 'DOM' flight_type, 'Y' is_active FROM dual UNION ALL
    SELECT 'CJU', '제주국제공항', '대한민국', 'Asia/Seoul', (SELECT region_id FROM REGION WHERE region_name = '국내'), 'DOM', 'Y' FROM dual UNION ALL
    SELECT 'ICN', '인천국제공항', '대한민국', 'Asia/Seoul', (SELECT region_id FROM REGION WHERE region_name = '국내'), 'INT', 'Y' FROM dual UNION ALL
    SELECT 'NRT', '나리타국제공항', '일본', 'Asia/Tokyo', (SELECT region_id FROM REGION WHERE region_name = '일본'), 'INT', 'Y' FROM dual UNION ALL
    SELECT 'KIX', '간사이국제공항', '일본', 'Asia/Tokyo', (SELECT region_id FROM REGION WHERE region_name = '일본'), 'INT', 'Y' FROM dual
) s
ON (t.iata_code = s.iata_code)
WHEN MATCHED THEN UPDATE SET t.airport_name = s.airport_name,
                             t.country = s.country,
                             t.timezone = s.timezone,
                             t.region_id = s.region_id,
                             t.flight_type = s.flight_type,
                             t.is_active = s.is_active
WHEN NOT MATCHED THEN INSERT (iata_code, airport_name, country, timezone, region_id, flight_type, is_active)
VALUES (s.iata_code, s.airport_name, s.country, s.timezone, s.region_id, s.flight_type, s.is_active);

MERGE INTO GATE t
USING (
    SELECT (SELECT airport_id FROM AIRPORT WHERE iata_code = 'GMP') airport_id, 'A1' gate_code, (SELECT gate_area_id FROM GATE_AREA WHERE area_name = 'A구역') gate_area_id, 'DOM' flight_type, 'Y' is_active FROM dual UNION ALL
    SELECT (SELECT airport_id FROM AIRPORT WHERE iata_code = 'CJU'), 'A1', (SELECT gate_area_id FROM GATE_AREA WHERE area_name = 'A구역'), 'DOM', 'Y' FROM dual UNION ALL
    SELECT (SELECT airport_id FROM AIRPORT WHERE iata_code = 'ICN'), 'B1', (SELECT gate_area_id FROM GATE_AREA WHERE area_name = 'B구역'), 'INT', 'Y' FROM dual UNION ALL
    SELECT (SELECT airport_id FROM AIRPORT WHERE iata_code = 'NRT'), 'C1', (SELECT gate_area_id FROM GATE_AREA WHERE area_name = 'C구역'), 'INT', 'Y' FROM dual UNION ALL
    SELECT (SELECT airport_id FROM AIRPORT WHERE iata_code = 'KIX'), 'D1', (SELECT gate_area_id FROM GATE_AREA WHERE area_name = 'D구역'), 'INT', 'Y' FROM dual
) s
ON (t.airport_id = s.airport_id AND t.gate_code = s.gate_code)
WHEN MATCHED THEN UPDATE SET t.gate_area_id = s.gate_area_id,
                             t.flight_type = s.flight_type,
                             t.is_active = s.is_active
WHEN NOT MATCHED THEN INSERT (airport_id, gate_code, gate_area_id, flight_type, is_active)
VALUES (s.airport_id, s.gate_code, s.gate_area_id, s.flight_type, s.is_active);

INSERT INTO ROUTE (departure_airport_id, arrival_airport_id, flight_type, route_type_id, is_active)
SELECT s.departure_airport_id, s.arrival_airport_id, s.flight_type, s.route_type_id, s.is_active
FROM (
    SELECT (SELECT airport_id FROM AIRPORT WHERE iata_code = 'GMP') departure_airport_id, (SELECT airport_id FROM AIRPORT WHERE iata_code = 'CJU') arrival_airport_id, 'DOM' flight_type, (SELECT route_type_id FROM ROUTE_TYPE WHERE type_name = '초단거리') route_type_id, 'Y' is_active FROM dual UNION ALL
    SELECT (SELECT airport_id FROM AIRPORT WHERE iata_code = 'CJU'), (SELECT airport_id FROM AIRPORT WHERE iata_code = 'GMP'), 'DOM', (SELECT route_type_id FROM ROUTE_TYPE WHERE type_name = '초단거리'), 'Y' FROM dual UNION ALL
    SELECT (SELECT airport_id FROM AIRPORT WHERE iata_code = 'ICN'), (SELECT airport_id FROM AIRPORT WHERE iata_code = 'NRT'), 'INT', (SELECT route_type_id FROM ROUTE_TYPE WHERE type_name = '중거리'), 'Y' FROM dual UNION ALL
    SELECT (SELECT airport_id FROM AIRPORT WHERE iata_code = 'NRT'), (SELECT airport_id FROM AIRPORT WHERE iata_code = 'ICN'), 'INT', (SELECT route_type_id FROM ROUTE_TYPE WHERE type_name = '중거리'), 'Y' FROM dual UNION ALL
    SELECT (SELECT airport_id FROM AIRPORT WHERE iata_code = 'ICN'), (SELECT airport_id FROM AIRPORT WHERE iata_code = 'KIX'), 'INT', (SELECT route_type_id FROM ROUTE_TYPE WHERE type_name = '중거리'), 'Y' FROM dual
) s
WHERE NOT EXISTS (
    SELECT 1 FROM ROUTE t
    WHERE t.departure_airport_id = s.departure_airport_id
      AND t.arrival_airport_id = s.arrival_airport_id
);

UPDATE ROUTE t
SET t.is_active = 'Y',
    t.flight_type = (
        SELECT s.flight_type FROM (
            SELECT (SELECT airport_id FROM AIRPORT WHERE iata_code = 'GMP') departure_airport_id, (SELECT airport_id FROM AIRPORT WHERE iata_code = 'CJU') arrival_airport_id, 'DOM' flight_type FROM dual UNION ALL
            SELECT (SELECT airport_id FROM AIRPORT WHERE iata_code = 'CJU'), (SELECT airport_id FROM AIRPORT WHERE iata_code = 'GMP'), 'DOM' FROM dual UNION ALL
            SELECT (SELECT airport_id FROM AIRPORT WHERE iata_code = 'ICN'), (SELECT airport_id FROM AIRPORT WHERE iata_code = 'NRT'), 'INT' FROM dual UNION ALL
            SELECT (SELECT airport_id FROM AIRPORT WHERE iata_code = 'NRT'), (SELECT airport_id FROM AIRPORT WHERE iata_code = 'ICN'), 'INT' FROM dual UNION ALL
            SELECT (SELECT airport_id FROM AIRPORT WHERE iata_code = 'ICN'), (SELECT airport_id FROM AIRPORT WHERE iata_code = 'KIX'), 'INT' FROM dual
        ) s
        WHERE s.departure_airport_id = t.departure_airport_id
          AND s.arrival_airport_id = t.arrival_airport_id
    )
WHERE EXISTS (
    SELECT 1 FROM (
        SELECT (SELECT airport_id FROM AIRPORT WHERE iata_code = 'GMP') departure_airport_id, (SELECT airport_id FROM AIRPORT WHERE iata_code = 'CJU') arrival_airport_id FROM dual UNION ALL
        SELECT (SELECT airport_id FROM AIRPORT WHERE iata_code = 'CJU'), (SELECT airport_id FROM AIRPORT WHERE iata_code = 'GMP') FROM dual UNION ALL
        SELECT (SELECT airport_id FROM AIRPORT WHERE iata_code = 'ICN'), (SELECT airport_id FROM AIRPORT WHERE iata_code = 'NRT') FROM dual UNION ALL
        SELECT (SELECT airport_id FROM AIRPORT WHERE iata_code = 'NRT'), (SELECT airport_id FROM AIRPORT WHERE iata_code = 'ICN') FROM dual UNION ALL
        SELECT (SELECT airport_id FROM AIRPORT WHERE iata_code = 'ICN'), (SELECT airport_id FROM AIRPORT WHERE iata_code = 'KIX') FROM dual
    ) s
    WHERE s.departure_airport_id = t.departure_airport_id
      AND s.arrival_airport_id = t.arrival_airport_id
);

PROMPT [3] 공항/게이트/노선 생성 완료

/* =============================================================================
[4] 항공기 / 좌석 데이터
============================================================================= */

MERGE INTO AIRCRAFT t
USING (
    SELECT 'HL-SR001' reg_no, 'SkyRoute A320 Dummy' model_name, 12 total_seats, '운항가능' status_name, 'Y' is_active FROM dual UNION ALL
    SELECT 'HL-SR002', 'SkyRoute B737 Dummy', 12, '운항가능', 'Y' FROM dual UNION ALL
    SELECT 'HL-SR003', 'SkyRoute A321 Dummy', 12, '운항가능', 'Y' FROM dual UNION ALL
    SELECT 'HL-SR004', 'SkyRoute B787 Dummy', 12, '운항가능', 'Y' FROM dual UNION ALL
    SELECT 'HL-SR005', 'SkyRoute A330 Dummy', 12, '운항가능', 'Y' FROM dual
) s
ON (t.reg_no = s.reg_no)
WHEN MATCHED THEN UPDATE SET t.model_name = s.model_name,
                             t.total_seats = s.total_seats,
                             t.status_name = s.status_name,
                             t.is_active = s.is_active
WHEN NOT MATCHED THEN INSERT (reg_no, model_name, total_seats, status_name, is_active)
VALUES (s.reg_no, s.model_name, s.total_seats, s.status_name, s.is_active);

INSERT INTO SEAT (aircraft_id, seat_no, seat_class_id, is_active)
SELECT a.aircraft_id,
       seat_map.seat_no,
       sc.seat_class_id,
       'Y'
FROM AIRCRAFT a
CROSS JOIN (
    SELECT '1A' AS seat_no, '퍼스트' AS class_name FROM dual UNION ALL
    SELECT '1B', '퍼스트' FROM dual UNION ALL
    SELECT '2A', '비즈니스' FROM dual UNION ALL
    SELECT '2B', '비즈니스' FROM dual UNION ALL
    SELECT '3A', '프리미엄 이코노미' FROM dual UNION ALL
    SELECT '3B', '프리미엄 이코노미' FROM dual UNION ALL
    SELECT '4A', '이코노미' FROM dual UNION ALL
    SELECT '4B', '이코노미' FROM dual UNION ALL
    SELECT '4C', '이코노미' FROM dual UNION ALL
    SELECT '4D', '이코노미' FROM dual UNION ALL
    SELECT '4E', '이코노미' FROM dual UNION ALL
    SELECT '4F', '이코노미' FROM dual
) seat_map
JOIN SEAT_CLASS sc ON sc.class_name = seat_map.class_name
WHERE a.reg_no IN ('HL-SR001', 'HL-SR002', 'HL-SR003', 'HL-SR004', 'HL-SR005')
  AND NOT EXISTS (
      SELECT 1 FROM SEAT t
      WHERE t.aircraft_id = a.aircraft_id
        AND t.seat_no = seat_map.seat_no
  );

UPDATE SEAT t
SET t.is_active = 'Y',
    t.seat_class_id = (
        SELECT sc.seat_class_id
        FROM (
            SELECT '1A' AS seat_no, '퍼스트' AS class_name FROM dual UNION ALL
            SELECT '1B', '퍼스트' FROM dual UNION ALL
            SELECT '2A', '비즈니스' FROM dual UNION ALL
            SELECT '2B', '비즈니스' FROM dual UNION ALL
            SELECT '3A', '프리미엄 이코노미' FROM dual UNION ALL
            SELECT '3B', '프리미엄 이코노미' FROM dual UNION ALL
            SELECT '4A', '이코노미' FROM dual UNION ALL
            SELECT '4B', '이코노미' FROM dual UNION ALL
            SELECT '4C', '이코노미' FROM dual UNION ALL
            SELECT '4D', '이코노미' FROM dual UNION ALL
            SELECT '4E', '이코노미' FROM dual UNION ALL
            SELECT '4F', '이코노미' FROM dual
        ) seat_map
        JOIN SEAT_CLASS sc ON sc.class_name = seat_map.class_name
        WHERE seat_map.seat_no = t.seat_no
    )
WHERE EXISTS (
    SELECT 1 FROM AIRCRAFT a
    WHERE a.aircraft_id = t.aircraft_id
      AND a.reg_no IN ('HL-SR001', 'HL-SR002', 'HL-SR003', 'HL-SR004', 'HL-SR005')
)
AND t.seat_no IN ('1A','1B','2A','2B','3A','3B','4A','4B','4C','4D','4E','4F');

PROMPT [4] 항공기/좌석 생성 완료

/* =============================================================================
[5] 운임 / 운항 스케줄 / 항공편 운임
============================================================================= */

INSERT INTO FARE (route_id, seat_class_id, season_id, price, is_active)
SELECT r.route_id,
       sc.seat_class_id,
       se.season_id,
       CASE
           WHEN r.flight_type = 'DOM' AND sc.class_name = '퍼스트' THEN 199000
           WHEN r.flight_type = 'DOM' AND sc.class_name = '비즈니스' THEN 139000
           WHEN r.flight_type = 'DOM' AND sc.class_name = '프리미엄 이코노미' THEN 99000
           WHEN r.flight_type = 'DOM' AND sc.class_name = '이코노미' THEN 69000
           WHEN r.flight_type = 'INT' AND sc.class_name = '퍼스트' THEN 520000
           WHEN r.flight_type = 'INT' AND sc.class_name = '비즈니스' THEN 390000
           WHEN r.flight_type = 'INT' AND sc.class_name = '프리미엄 이코노미' THEN 260000
           WHEN r.flight_type = 'INT' AND sc.class_name = '이코노미' THEN 180000
       END AS price,
       'Y'
FROM ROUTE r
CROSS JOIN SEAT_CLASS sc
JOIN SEASON se ON se.season_name = '개발용 현재시즌'
WHERE sc.class_name IN ('퍼스트', '비즈니스', '프리미엄 이코노미', '이코노미')
  AND EXISTS (
      SELECT 1 FROM AIRPORT da JOIN AIRPORT aa ON 1 = 1
      WHERE da.airport_id = r.departure_airport_id
        AND aa.airport_id = r.arrival_airport_id
        AND ((da.iata_code = 'GMP' AND aa.iata_code = 'CJU')
          OR (da.iata_code = 'CJU' AND aa.iata_code = 'GMP')
          OR (da.iata_code = 'ICN' AND aa.iata_code = 'NRT')
          OR (da.iata_code = 'NRT' AND aa.iata_code = 'ICN')
          OR (da.iata_code = 'ICN' AND aa.iata_code = 'KIX'))
  )
  AND NOT EXISTS (
      SELECT 1 FROM FARE t
      WHERE t.route_id = r.route_id
        AND t.seat_class_id = sc.seat_class_id
        AND t.season_id = se.season_id
  );

UPDATE FARE t
SET t.is_active = 'Y',
    t.price = (
        SELECT CASE
                   WHEN r.flight_type = 'DOM' AND sc.class_name = '퍼스트' THEN 199000
                   WHEN r.flight_type = 'DOM' AND sc.class_name = '비즈니스' THEN 139000
                   WHEN r.flight_type = 'DOM' AND sc.class_name = '프리미엄 이코노미' THEN 99000
                   WHEN r.flight_type = 'DOM' AND sc.class_name = '이코노미' THEN 69000
                   WHEN r.flight_type = 'INT' AND sc.class_name = '퍼스트' THEN 520000
                   WHEN r.flight_type = 'INT' AND sc.class_name = '비즈니스' THEN 390000
                   WHEN r.flight_type = 'INT' AND sc.class_name = '프리미엄 이코노미' THEN 260000
                   WHEN r.flight_type = 'INT' AND sc.class_name = '이코노미' THEN 180000
               END
        FROM ROUTE r
        JOIN SEAT_CLASS sc ON sc.seat_class_id = t.seat_class_id
        WHERE r.route_id = t.route_id
    )
WHERE t.season_id = (SELECT season_id FROM SEASON WHERE season_name = '개발용 현재시즌')
  AND EXISTS (
      SELECT 1 FROM ROUTE r
      JOIN AIRPORT da ON r.departure_airport_id = da.airport_id
      JOIN AIRPORT aa ON r.arrival_airport_id = aa.airport_id
      WHERE r.route_id = t.route_id
        AND ((da.iata_code = 'GMP' AND aa.iata_code = 'CJU')
          OR (da.iata_code = 'CJU' AND aa.iata_code = 'GMP')
          OR (da.iata_code = 'ICN' AND aa.iata_code = 'NRT')
          OR (da.iata_code = 'NRT' AND aa.iata_code = 'ICN')
          OR (da.iata_code = 'ICN' AND aa.iata_code = 'KIX'))
  );

MERGE INTO FLIGHT t
USING (
    SELECT 'SR101' flight_no,
           (SELECT r.route_id FROM ROUTE r JOIN AIRPORT da ON r.departure_airport_id = da.airport_id JOIN AIRPORT aa ON r.arrival_airport_id = aa.airport_id WHERE da.iata_code = 'GMP' AND aa.iata_code = 'CJU' AND ROWNUM = 1) route_id,
           (SELECT aircraft_id FROM AIRCRAFT WHERE reg_no = 'HL-SR001') aircraft_id,
           (SELECT g.gate_id FROM GATE g JOIN AIRPORT a ON g.airport_id = a.airport_id WHERE a.iata_code = 'GMP' AND g.gate_code = 'A1') departure_gate_id,
           (SELECT g.gate_id FROM GATE g JOIN AIRPORT a ON g.airport_id = a.airport_id WHERE a.iata_code = 'CJU' AND g.gate_code = 'A1') arrival_gate_id,
           CAST(TRUNC(SYSDATE) + 1 + 9/24 AS TIMESTAMP) departure_time,
           CAST(TRUNC(SYSDATE) + 1 + 10/24 + 10/1440 AS TIMESTAMP) arrival_time,
           'SCHEDULED' flight_status,
           CAST(NULL AS NUMBER) delay_minutes,
           'N' is_deleted
    FROM dual UNION ALL
    SELECT 'SR102',
           (SELECT r.route_id FROM ROUTE r JOIN AIRPORT da ON r.departure_airport_id = da.airport_id JOIN AIRPORT aa ON r.arrival_airport_id = aa.airport_id WHERE da.iata_code = 'CJU' AND aa.iata_code = 'GMP' AND ROWNUM = 1),
           (SELECT aircraft_id FROM AIRCRAFT WHERE reg_no = 'HL-SR002'),
           (SELECT g.gate_id FROM GATE g JOIN AIRPORT a ON g.airport_id = a.airport_id WHERE a.iata_code = 'CJU' AND g.gate_code = 'A1'),
           (SELECT g.gate_id FROM GATE g JOIN AIRPORT a ON g.airport_id = a.airport_id WHERE a.iata_code = 'GMP' AND g.gate_code = 'A1'),
           CAST(TRUNC(SYSDATE) + 1 + 18/24 AS TIMESTAMP),
           CAST(TRUNC(SYSDATE) + 1 + 19/24 + 10/1440 AS TIMESTAMP),
           'SCHEDULED', CAST(NULL AS NUMBER), 'N'
    FROM dual UNION ALL
    SELECT 'SR201',
           (SELECT r.route_id FROM ROUTE r JOIN AIRPORT da ON r.departure_airport_id = da.airport_id JOIN AIRPORT aa ON r.arrival_airport_id = aa.airport_id WHERE da.iata_code = 'ICN' AND aa.iata_code = 'NRT' AND ROWNUM = 1),
           (SELECT aircraft_id FROM AIRCRAFT WHERE reg_no = 'HL-SR003'),
           (SELECT g.gate_id FROM GATE g JOIN AIRPORT a ON g.airport_id = a.airport_id WHERE a.iata_code = 'ICN' AND g.gate_code = 'B1'),
           (SELECT g.gate_id FROM GATE g JOIN AIRPORT a ON g.airport_id = a.airport_id WHERE a.iata_code = 'NRT' AND g.gate_code = 'C1'),
           CAST(TRUNC(SYSDATE) + 1 + 11/24 AS TIMESTAMP),
           CAST(TRUNC(SYSDATE) + 1 + 13/24 + 30/1440 AS TIMESTAMP),
           'DELAYED', 30, 'N'
    FROM dual UNION ALL
    SELECT 'SR202',
           (SELECT r.route_id FROM ROUTE r JOIN AIRPORT da ON r.departure_airport_id = da.airport_id JOIN AIRPORT aa ON r.arrival_airport_id = aa.airport_id WHERE da.iata_code = 'NRT' AND aa.iata_code = 'ICN' AND ROWNUM = 1),
           (SELECT aircraft_id FROM AIRCRAFT WHERE reg_no = 'HL-SR004'),
           (SELECT g.gate_id FROM GATE g JOIN AIRPORT a ON g.airport_id = a.airport_id WHERE a.iata_code = 'NRT' AND g.gate_code = 'C1'),
           (SELECT g.gate_id FROM GATE g JOIN AIRPORT a ON g.airport_id = a.airport_id WHERE a.iata_code = 'ICN' AND g.gate_code = 'B1'),
           CAST(TRUNC(SYSDATE) + 2 + 14/24 AS TIMESTAMP),
           CAST(TRUNC(SYSDATE) + 2 + 16/24 + 30/1440 AS TIMESTAMP),
           'SCHEDULED', CAST(NULL AS NUMBER), 'N'
    FROM dual UNION ALL
    SELECT 'SR301',
           (SELECT r.route_id FROM ROUTE r JOIN AIRPORT da ON r.departure_airport_id = da.airport_id JOIN AIRPORT aa ON r.arrival_airport_id = aa.airport_id WHERE da.iata_code = 'ICN' AND aa.iata_code = 'KIX' AND ROWNUM = 1),
           (SELECT aircraft_id FROM AIRCRAFT WHERE reg_no = 'HL-SR005'),
           (SELECT g.gate_id FROM GATE g JOIN AIRPORT a ON g.airport_id = a.airport_id WHERE a.iata_code = 'ICN' AND g.gate_code = 'B1'),
           (SELECT g.gate_id FROM GATE g JOIN AIRPORT a ON g.airport_id = a.airport_id WHERE a.iata_code = 'KIX' AND g.gate_code = 'D1'),
           CAST(TRUNC(SYSDATE) + 1 + 15/24 AS TIMESTAMP),
           CAST(TRUNC(SYSDATE) + 1 + 17/24 AS TIMESTAMP),
           'CANCELLED', CAST(NULL AS NUMBER), 'N'
    FROM dual
) s
ON (t.flight_no = s.flight_no AND t.departure_time = s.departure_time)
WHEN MATCHED THEN UPDATE SET t.route_id = s.route_id,
                             t.aircraft_id = s.aircraft_id,
                             t.departure_gate_id = s.departure_gate_id,
                             t.arrival_gate_id = s.arrival_gate_id,
                             t.arrival_time = s.arrival_time,
                             t.flight_status = s.flight_status,
                             t.delay_minutes = s.delay_minutes,
                             t.is_deleted = s.is_deleted,
                             t.updated_at = SYSTIMESTAMP
WHEN NOT MATCHED THEN INSERT (flight_no, route_id, aircraft_id, departure_gate_id, arrival_gate_id, departure_time, arrival_time, flight_status, delay_minutes, is_deleted)
VALUES (s.flight_no, s.route_id, s.aircraft_id, s.departure_gate_id, s.arrival_gate_id, s.departure_time, s.arrival_time, s.flight_status, s.delay_minutes, s.is_deleted);

/* -----------------------------------------------------------------------------
[5-1] FLIGHT_FARE 생성/갱신
- v4 변경: 기존 INSERT 후 UPDATE 방식에서 단일 MERGE 방식으로 변경
- 이유: FARE 조인 결과가 중복될 경우 ORA-30926이 발생할 수 있으므로
        ROW_NUMBER()로 항공편+좌석등급당 1건만 선택한다.
- 현재 팀 DB에 FLIGHT_FARE.fare_id 컬럼이 추가된 경우도 자동 대응한다.
----------------------------------------------------------------------------- */
DECLARE
    v_has_fare_id NUMBER;
BEGIN
    SELECT COUNT(*)
      INTO v_has_fare_id
      FROM USER_TAB_COLUMNS
     WHERE TABLE_NAME = 'FLIGHT_FARE'
       AND COLUMN_NAME = 'FARE_ID';

    IF v_has_fare_id > 0 THEN
        EXECUTE IMMEDIATE q'[
            MERGE INTO FLIGHT_FARE t
            USING (
                SELECT flight_id, seat_class_id, fare_id, price
                FROM (
                    SELECT f.flight_id,
                           fare.seat_class_id,
                           fare.fare_id,
                           fare.price,
                           ROW_NUMBER() OVER (
                               PARTITION BY f.flight_id, fare.seat_class_id
                               ORDER BY fare.fare_id DESC
                           ) rn
                    FROM FLIGHT f
                    JOIN FARE fare ON fare.route_id = f.route_id
                    JOIN SEASON se ON se.season_id = fare.season_id
                    WHERE se.season_name = '개발용 현재시즌'
                      AND f.flight_no IN ('SR101', 'SR102', 'SR201', 'SR202', 'SR301')
                )
                WHERE rn = 1
            ) s
            ON (t.flight_id = s.flight_id AND t.seat_class_id = s.seat_class_id)
            WHEN MATCHED THEN
                UPDATE SET t.fare_id = s.fare_id,
                           t.price = s.price
            WHEN NOT MATCHED THEN
                INSERT (flight_id, seat_class_id, fare_id, price)
                VALUES (s.flight_id, s.seat_class_id, s.fare_id, s.price)
        ]';
    ELSE
        EXECUTE IMMEDIATE q'[
            MERGE INTO FLIGHT_FARE t
            USING (
                SELECT flight_id, seat_class_id, price
                FROM (
                    SELECT f.flight_id,
                           fare.seat_class_id,
                           fare.price,
                           ROW_NUMBER() OVER (
                               PARTITION BY f.flight_id, fare.seat_class_id
                               ORDER BY fare.fare_id DESC
                           ) rn
                    FROM FLIGHT f
                    JOIN FARE fare ON fare.route_id = f.route_id
                    JOIN SEASON se ON se.season_id = fare.season_id
                    WHERE se.season_name = '개발용 현재시즌'
                      AND f.flight_no IN ('SR101', 'SR102', 'SR201', 'SR202', 'SR301')
                )
                WHERE rn = 1
            ) s
            ON (t.flight_id = s.flight_id AND t.seat_class_id = s.seat_class_id)
            WHEN MATCHED THEN
                UPDATE SET t.price = s.price
            WHEN NOT MATCHED THEN
                INSERT (flight_id, seat_class_id, price)
                VALUES (s.flight_id, s.seat_class_id, s.price)
        ]';
    END IF;

    DBMS_OUTPUT.PUT_LINE('[5-1] FLIGHT_FARE 생성/갱신 완료');
END;
/

INSERT INTO FLIGHT_NOTICE (flight_id, notice_type, reason, delay_minutes, created_by)
SELECT s.flight_id, s.notice_type, s.reason, s.delay_minutes, s.created_by
FROM (
    SELECT (SELECT flight_id FROM FLIGHT WHERE flight_no = 'SR201' AND departure_time = CAST(TRUNC(SYSDATE) + 1 + 11/24 AS TIMESTAMP)) flight_id,
           'DELAY' notice_type,
           '기상 악화로 인한 출발 지연 테스트 데이터입니다.' reason,
           30 delay_minutes,
           (SELECT member_id FROM MEMBER WHERE login_id = 'dummy_staff_ops') created_by
    FROM dual UNION ALL
    SELECT (SELECT flight_id FROM FLIGHT WHERE flight_no = 'SR301' AND departure_time = CAST(TRUNC(SYSDATE) + 1 + 15/24 AS TIMESTAMP)),
           'CANCEL',
           '항공기 정비 점검으로 인한 결항 테스트 데이터입니다.',
           CAST(NULL AS NUMBER),
           (SELECT member_id FROM MEMBER WHERE login_id = 'dummy_staff_ops')
    FROM dual
) s
WHERE NOT EXISTS (
    SELECT 1 FROM FLIGHT_NOTICE t
    WHERE t.flight_id = s.flight_id
      AND t.notice_type = s.notice_type
);

PROMPT [5] 운임/운항 스케줄/항공편 운임 생성 완료

/* =============================================================================
[6] 등록 탑승객 / 예약 / 예약 탑승객 / 티켓 / 결제 / 환불
============================================================================= */

INSERT INTO SAVED_PASSENGER (member_id, name, birth_date, phone, gender, passport_no, passport_expiry)
SELECT s.member_id, s.name, s.birth_date, s.phone, s.gender, s.passport_no, s.passport_expiry
FROM (
    SELECT (SELECT member_id FROM MEMBER WHERE login_id = 'dummy_user_pay') member_id, '김하늘' name, DATE '1995-03-21' birth_date, '010-9100-0001' phone, 'F' gender, 'M12345678' passport_no, ADD_MONTHS(TRUNC(SYSDATE), 36) passport_expiry FROM dual UNION ALL
    SELECT (SELECT member_id FROM MEMBER WHERE login_id = 'dummy_user_pay'), '이도윤', DATE '1992-11-08', '010-9100-0002', 'M', 'M23456789', ADD_MONTHS(TRUNC(SYSDATE), 48) FROM dual UNION ALL
    SELECT (SELECT member_id FROM MEMBER WHERE login_id = 'dummy_user_pay'), '박서준', DATE '1989-07-15', '010-9100-0003', 'M', 'M34567890', ADD_MONTHS(TRUNC(SYSDATE), 60) FROM dual UNION ALL
    SELECT (SELECT member_id FROM MEMBER WHERE login_id = 'dummy_user_refund'), '최유나', DATE '1998-01-04', '010-9100-0004', 'F', 'M45678901', ADD_MONTHS(TRUNC(SYSDATE), 36) FROM dual UNION ALL
    SELECT (SELECT member_id FROM MEMBER WHERE login_id = 'dummy_user_refund'), '정민재', DATE '1990-09-19', '010-9100-0005', 'M', 'M56789012', ADD_MONTHS(TRUNC(SYSDATE), 48) FROM dual
) s
WHERE NOT EXISTS (
    SELECT 1 FROM SAVED_PASSENGER t
    WHERE t.member_id = s.member_id
      AND t.name = s.name
      AND t.birth_date = s.birth_date
);

MERGE INTO BOOKING t
USING (
    SELECT 'BKDUMMY001' booking_no,
           (SELECT member_id FROM MEMBER WHERE login_id = 'dummy_user_pay') member_id,
           'ONEWAY' trip_type,
           (SELECT flight_id FROM FLIGHT WHERE flight_no = 'SR101' AND departure_time = CAST(TRUNC(SYSDATE) + 1 + 9/24 AS TIMESTAMP)) outbound_flight_id,
           CAST(NULL AS NUMBER) inbound_flight_id,
           'PENDING' status,
           69000 total_amount,
           SYSTIMESTAMP - INTERVAL '5' MINUTE created_at,
           CAST(NULL AS TIMESTAMP) cancelled_at
    FROM dual UNION ALL
    SELECT 'BKDUMMY002',
           (SELECT member_id FROM MEMBER WHERE login_id = 'dummy_user_pay'),
           'ONEWAY',
           (SELECT flight_id FROM FLIGHT WHERE flight_no = 'SR102' AND departure_time = CAST(TRUNC(SYSDATE) + 1 + 18/24 AS TIMESTAMP)),
           CAST(NULL AS NUMBER),
           'CONFIRMED',
           139000,
           SYSTIMESTAMP - INTERVAL '2' HOUR,
           CAST(NULL AS TIMESTAMP)
    FROM dual UNION ALL
    SELECT 'BKDUMMY003',
           (SELECT member_id FROM MEMBER WHERE login_id = 'dummy_user_pay'),
           'ONEWAY',
           (SELECT flight_id FROM FLIGHT WHERE flight_no = 'SR201' AND departure_time = CAST(TRUNC(SYSDATE) + 1 + 11/24 AS TIMESTAMP)),
           CAST(NULL AS NUMBER),
           'FAILED',
           520000,
           SYSTIMESTAMP - INTERVAL '1' HOUR,
           CAST(NULL AS TIMESTAMP)
    FROM dual UNION ALL
    SELECT 'BKDUMMY004',
           (SELECT member_id FROM MEMBER WHERE login_id = 'dummy_user_pay'),
           'ROUNDTRIP',
           (SELECT flight_id FROM FLIGHT WHERE flight_no = 'SR101' AND departure_time = CAST(TRUNC(SYSDATE) + 1 + 9/24 AS TIMESTAMP)),
           (SELECT flight_id FROM FLIGHT WHERE flight_no = 'SR102' AND departure_time = CAST(TRUNC(SYSDATE) + 1 + 18/24 AS TIMESTAMP)),
           'PENDING',
           198000,
           SYSTIMESTAMP - INTERVAL '15' MINUTE,
           CAST(NULL AS TIMESTAMP)
    FROM dual UNION ALL
    SELECT 'BKDUMMY005',
           (SELECT member_id FROM MEMBER WHERE login_id = 'dummy_user_refund'),
           'ROUNDTRIP',
           (SELECT flight_id FROM FLIGHT WHERE flight_no = 'SR201' AND departure_time = CAST(TRUNC(SYSDATE) + 1 + 11/24 AS TIMESTAMP)),
           (SELECT flight_id FROM FLIGHT WHERE flight_no = 'SR202' AND departure_time = CAST(TRUNC(SYSDATE) + 2 + 14/24 AS TIMESTAMP)),
           'CANCELLED',
           360000,
           SYSTIMESTAMP - INTERVAL '3' DAY,
           SYSTIMESTAMP - INTERVAL '1' DAY
    FROM dual
) s
ON (t.booking_no = s.booking_no)
WHEN MATCHED THEN UPDATE SET t.member_id = s.member_id,
                             t.trip_type = s.trip_type,
                             t.outbound_flight_id = s.outbound_flight_id,
                             t.inbound_flight_id = s.inbound_flight_id,
                             t.status = s.status,
                             t.total_amount = s.total_amount,
                             t.cancelled_at = s.cancelled_at
WHEN NOT MATCHED THEN INSERT (booking_no, member_id, trip_type, outbound_flight_id, inbound_flight_id, status, total_amount, created_at, cancelled_at)
VALUES (s.booking_no, s.member_id, s.trip_type, s.outbound_flight_id, s.inbound_flight_id, s.status, s.total_amount, s.created_at, s.cancelled_at);

INSERT INTO BOOKING_PASSENGER (booking_id, saved_passenger_id, name, birth_date, phone, gender, passport_no, passport_expiry)
SELECT s.booking_id, s.saved_passenger_id, s.name, s.birth_date, s.phone, s.gender, s.passport_no, s.passport_expiry
FROM (
    SELECT (SELECT booking_id FROM BOOKING WHERE booking_no = 'BKDUMMY001') booking_id, (SELECT saved_passenger_id FROM SAVED_PASSENGER sp JOIN MEMBER m ON sp.member_id = m.member_id WHERE m.login_id = 'dummy_user_pay' AND sp.name = '김하늘' AND ROWNUM = 1) saved_passenger_id, '김하늘' name, DATE '1995-03-21' birth_date, '010-9100-0001' phone, 'F' gender, 'M12345678' passport_no, ADD_MONTHS(TRUNC(SYSDATE), 36) passport_expiry FROM dual UNION ALL
    SELECT (SELECT booking_id FROM BOOKING WHERE booking_no = 'BKDUMMY002'), (SELECT saved_passenger_id FROM SAVED_PASSENGER sp JOIN MEMBER m ON sp.member_id = m.member_id WHERE m.login_id = 'dummy_user_pay' AND sp.name = '이도윤' AND ROWNUM = 1), '이도윤', DATE '1992-11-08', '010-9100-0002', 'M', 'M23456789', ADD_MONTHS(TRUNC(SYSDATE), 48) FROM dual UNION ALL
    SELECT (SELECT booking_id FROM BOOKING WHERE booking_no = 'BKDUMMY003'), (SELECT saved_passenger_id FROM SAVED_PASSENGER sp JOIN MEMBER m ON sp.member_id = m.member_id WHERE m.login_id = 'dummy_user_pay' AND sp.name = '박서준' AND ROWNUM = 1), '박서준', DATE '1989-07-15', '010-9100-0003', 'M', 'M34567890', ADD_MONTHS(TRUNC(SYSDATE), 60) FROM dual UNION ALL
    SELECT (SELECT booking_id FROM BOOKING WHERE booking_no = 'BKDUMMY004'), (SELECT saved_passenger_id FROM SAVED_PASSENGER sp JOIN MEMBER m ON sp.member_id = m.member_id WHERE m.login_id = 'dummy_user_pay' AND sp.name = '김하늘' AND ROWNUM = 1), '김하늘', DATE '1995-03-21', '010-9100-0001', 'F', 'M12345678', ADD_MONTHS(TRUNC(SYSDATE), 36) FROM dual UNION ALL
    SELECT (SELECT booking_id FROM BOOKING WHERE booking_no = 'BKDUMMY005'), (SELECT saved_passenger_id FROM SAVED_PASSENGER sp JOIN MEMBER m ON sp.member_id = m.member_id WHERE m.login_id = 'dummy_user_refund' AND sp.name = '최유나' AND ROWNUM = 1), '최유나', DATE '1998-01-04', '010-9100-0004', 'F', 'M45678901', ADD_MONTHS(TRUNC(SYSDATE), 36) FROM dual
) s
WHERE NOT EXISTS (
    SELECT 1 FROM BOOKING_PASSENGER t
    WHERE t.booking_id = s.booking_id
      AND t.name = s.name
      AND t.birth_date = s.birth_date
);

INSERT INTO TICKET (booking_id, booking_passenger_id, flight_id, seat_id, leg_type, fare_amount, hold_status, held_at, expired_at, checkin_status)
SELECT s.booking_id, s.booking_passenger_id, s.flight_id, s.seat_id, s.leg_type, s.fare_amount, s.hold_status, s.held_at, s.expired_at, s.checkin_status
FROM (
    SELECT (SELECT booking_id FROM BOOKING WHERE booking_no = 'BKDUMMY001') booking_id,
           (SELECT bp.booking_passenger_id FROM BOOKING_PASSENGER bp JOIN BOOKING b ON bp.booking_id = b.booking_id WHERE b.booking_no = 'BKDUMMY001' AND ROWNUM = 1) booking_passenger_id,
           (SELECT flight_id FROM FLIGHT WHERE flight_no = 'SR101' AND departure_time = CAST(TRUNC(SYSDATE) + 1 + 9/24 AS TIMESTAMP)) flight_id,
           (SELECT s.seat_id FROM SEAT s JOIN AIRCRAFT a ON s.aircraft_id = a.aircraft_id WHERE a.reg_no = 'HL-SR001' AND s.seat_no = '4A') seat_id,
           'OUTBOUND' leg_type, 69000 fare_amount, 'HOLDING' hold_status, SYSTIMESTAMP - INTERVAL '5' MINUTE held_at, SYSTIMESTAMP + INTERVAL '10' MINUTE expired_at, 'NOT_CHECKED_IN' checkin_status
    FROM dual UNION ALL
    SELECT (SELECT booking_id FROM BOOKING WHERE booking_no = 'BKDUMMY002'),
           (SELECT bp.booking_passenger_id FROM BOOKING_PASSENGER bp JOIN BOOKING b ON bp.booking_id = b.booking_id WHERE b.booking_no = 'BKDUMMY002' AND ROWNUM = 1),
           (SELECT flight_id FROM FLIGHT WHERE flight_no = 'SR102' AND departure_time = CAST(TRUNC(SYSDATE) + 1 + 18/24 AS TIMESTAMP)),
           (SELECT s.seat_id FROM SEAT s JOIN AIRCRAFT a ON s.aircraft_id = a.aircraft_id WHERE a.reg_no = 'HL-SR002' AND s.seat_no = '2A'),
           'OUTBOUND', 139000, 'CONFIRMED', SYSTIMESTAMP - INTERVAL '2' HOUR, CAST(NULL AS TIMESTAMP), 'NOT_CHECKED_IN'
    FROM dual UNION ALL
    SELECT (SELECT booking_id FROM BOOKING WHERE booking_no = 'BKDUMMY003'),
           (SELECT bp.booking_passenger_id FROM BOOKING_PASSENGER bp JOIN BOOKING b ON bp.booking_id = b.booking_id WHERE b.booking_no = 'BKDUMMY003' AND ROWNUM = 1),
           (SELECT flight_id FROM FLIGHT WHERE flight_no = 'SR201' AND departure_time = CAST(TRUNC(SYSDATE) + 1 + 11/24 AS TIMESTAMP)),
           (SELECT s.seat_id FROM SEAT s JOIN AIRCRAFT a ON s.aircraft_id = a.aircraft_id WHERE a.reg_no = 'HL-SR003' AND s.seat_no = '1A'),
           'OUTBOUND', 520000, 'RELEASED', SYSTIMESTAMP - INTERVAL '1' HOUR, SYSTIMESTAMP - INTERVAL '50' MINUTE, 'NOT_CHECKED_IN'
    FROM dual UNION ALL
    SELECT (SELECT booking_id FROM BOOKING WHERE booking_no = 'BKDUMMY004'),
           (SELECT bp.booking_passenger_id FROM BOOKING_PASSENGER bp JOIN BOOKING b ON bp.booking_id = b.booking_id WHERE b.booking_no = 'BKDUMMY004' AND ROWNUM = 1),
           (SELECT flight_id FROM FLIGHT WHERE flight_no = 'SR101' AND departure_time = CAST(TRUNC(SYSDATE) + 1 + 9/24 AS TIMESTAMP)),
           (SELECT s.seat_id FROM SEAT s JOIN AIRCRAFT a ON s.aircraft_id = a.aircraft_id WHERE a.reg_no = 'HL-SR001' AND s.seat_no = '3A'),
           'OUTBOUND', 99000, 'HOLDING', SYSTIMESTAMP - INTERVAL '15' MINUTE, SYSTIMESTAMP + INTERVAL '23' HOUR, 'NOT_CHECKED_IN'
    FROM dual UNION ALL
    SELECT (SELECT booking_id FROM BOOKING WHERE booking_no = 'BKDUMMY004'),
           (SELECT bp.booking_passenger_id FROM BOOKING_PASSENGER bp JOIN BOOKING b ON bp.booking_id = b.booking_id WHERE b.booking_no = 'BKDUMMY004' AND ROWNUM = 1),
           (SELECT flight_id FROM FLIGHT WHERE flight_no = 'SR102' AND departure_time = CAST(TRUNC(SYSDATE) + 1 + 18/24 AS TIMESTAMP)),
           (SELECT s.seat_id FROM SEAT s JOIN AIRCRAFT a ON s.aircraft_id = a.aircraft_id WHERE a.reg_no = 'HL-SR002' AND s.seat_no = '3A'),
           'INBOUND', 99000, 'HOLDING', SYSTIMESTAMP - INTERVAL '15' MINUTE, SYSTIMESTAMP + INTERVAL '23' HOUR, 'NOT_CHECKED_IN'
    FROM dual UNION ALL
    SELECT (SELECT booking_id FROM BOOKING WHERE booking_no = 'BKDUMMY005'),
           (SELECT bp.booking_passenger_id FROM BOOKING_PASSENGER bp JOIN BOOKING b ON bp.booking_id = b.booking_id WHERE b.booking_no = 'BKDUMMY005' AND ROWNUM = 1),
           (SELECT flight_id FROM FLIGHT WHERE flight_no = 'SR201' AND departure_time = CAST(TRUNC(SYSDATE) + 1 + 11/24 AS TIMESTAMP)),
           (SELECT s.seat_id FROM SEAT s JOIN AIRCRAFT a ON s.aircraft_id = a.aircraft_id WHERE a.reg_no = 'HL-SR003' AND s.seat_no = '4A'),
           'OUTBOUND', 180000, 'RELEASED', SYSTIMESTAMP - INTERVAL '3' DAY, CAST(NULL AS TIMESTAMP), 'NOT_CHECKED_IN'
    FROM dual UNION ALL
    SELECT (SELECT booking_id FROM BOOKING WHERE booking_no = 'BKDUMMY005'),
           (SELECT bp.booking_passenger_id FROM BOOKING_PASSENGER bp JOIN BOOKING b ON bp.booking_id = b.booking_id WHERE b.booking_no = 'BKDUMMY005' AND ROWNUM = 1),
           (SELECT flight_id FROM FLIGHT WHERE flight_no = 'SR202' AND departure_time = CAST(TRUNC(SYSDATE) + 2 + 14/24 AS TIMESTAMP)),
           (SELECT s.seat_id FROM SEAT s JOIN AIRCRAFT a ON s.aircraft_id = a.aircraft_id WHERE a.reg_no = 'HL-SR004' AND s.seat_no = '4A'),
           'INBOUND', 180000, 'RELEASED', SYSTIMESTAMP - INTERVAL '3' DAY, CAST(NULL AS TIMESTAMP), 'NOT_CHECKED_IN'
    FROM dual
) s
WHERE NOT EXISTS (
    SELECT 1 FROM TICKET t
    WHERE t.booking_id = s.booking_id
      AND t.flight_id = s.flight_id
      AND t.seat_id = s.seat_id
      AND t.leg_type = s.leg_type
);

MERGE INTO PAYMENT t
USING (
    SELECT (SELECT booking_id FROM BOOKING WHERE booking_no = 'BKDUMMY001') booking_id, 'CARD' method, 69000 amount, 'READY' status, CAST(NULL AS VARCHAR2(50)) imp_uid, 'MUID_BKDUMMY001' merchant_uid, CAST(NULL AS TIMESTAMP) paid_at, CAST(NULL AS NUMBER) refund_amount, CAST(NULL AS TIMESTAMP) refunded_at, SYSTIMESTAMP - INTERVAL '5' MINUTE created_at FROM dual UNION ALL
    SELECT (SELECT booking_id FROM BOOKING WHERE booking_no = 'BKDUMMY002'), 'KAKAOPAY', 139000, 'PAID', 'imp_dummy_002', 'MUID_BKDUMMY002', SYSTIMESTAMP - INTERVAL '2' HOUR, CAST(NULL AS NUMBER), CAST(NULL AS TIMESTAMP), SYSTIMESTAMP - INTERVAL '2' HOUR FROM dual UNION ALL
    SELECT (SELECT booking_id FROM BOOKING WHERE booking_no = 'BKDUMMY003'), 'CARD', 520000, 'FAILED', 'imp_dummy_003', 'MUID_BKDUMMY003', CAST(NULL AS TIMESTAMP), CAST(NULL AS NUMBER), CAST(NULL AS TIMESTAMP), SYSTIMESTAMP - INTERVAL '1' HOUR FROM dual UNION ALL
    SELECT (SELECT booking_id FROM BOOKING WHERE booking_no = 'BKDUMMY004'), 'VBANK', 198000, 'READY', CAST(NULL AS VARCHAR2(50)), 'MUID_BKDUMMY004', CAST(NULL AS TIMESTAMP), CAST(NULL AS NUMBER), CAST(NULL AS TIMESTAMP), SYSTIMESTAMP - INTERVAL '15' MINUTE FROM dual UNION ALL
    SELECT (SELECT booking_id FROM BOOKING WHERE booking_no = 'BKDUMMY005'), 'CARD', 360000, 'REFUNDED', 'imp_dummy_005', 'MUID_BKDUMMY005', SYSTIMESTAMP - INTERVAL '3' DAY, 360000, SYSTIMESTAMP - INTERVAL '1' DAY, SYSTIMESTAMP - INTERVAL '3' DAY FROM dual
) s
ON (t.merchant_uid = s.merchant_uid)
WHEN MATCHED THEN UPDATE SET t.booking_id = s.booking_id,
                             t.method = s.method,
                             t.amount = s.amount,
                             t.status = s.status,
                             t.imp_uid = s.imp_uid,
                             t.paid_at = s.paid_at,
                             t.refund_amount = s.refund_amount,
                             t.refunded_at = s.refunded_at
WHEN NOT MATCHED THEN INSERT (booking_id, method, amount, status, imp_uid, merchant_uid, paid_at, refund_amount, refunded_at, created_at)
VALUES (s.booking_id, s.method, s.amount, s.status, s.imp_uid, s.merchant_uid, s.paid_at, s.refund_amount, s.refunded_at, s.created_at);

INSERT INTO REFUND (payment_id, ticket_id, amount, reason, refunded_at)
SELECT s.payment_id, s.ticket_id, s.amount, s.reason, s.refunded_at
FROM (
    SELECT (SELECT p.payment_id FROM PAYMENT p JOIN BOOKING b ON p.booking_id = b.booking_id WHERE b.booking_no = 'BKDUMMY005') payment_id,
           (SELECT t.ticket_id FROM TICKET t JOIN BOOKING b ON t.booking_id = b.booking_id WHERE b.booking_no = 'BKDUMMY005' AND t.leg_type = 'OUTBOUND' AND ROWNUM = 1) ticket_id,
           180000 amount,
           '사용자 취소 환불 테스트 데이터' reason,
           SYSTIMESTAMP - INTERVAL '1' DAY refunded_at
    FROM dual UNION ALL
    SELECT (SELECT p.payment_id FROM PAYMENT p JOIN BOOKING b ON p.booking_id = b.booking_id WHERE b.booking_no = 'BKDUMMY005'),
           (SELECT t.ticket_id FROM TICKET t JOIN BOOKING b ON t.booking_id = b.booking_id WHERE b.booking_no = 'BKDUMMY005' AND t.leg_type = 'INBOUND' AND ROWNUM = 1),
           180000,
           '사용자 취소 환불 테스트 데이터',
           SYSTIMESTAMP - INTERVAL '1' DAY
    FROM dual
) s
WHERE NOT EXISTS (
    SELECT 1 FROM REFUND t
    WHERE t.ticket_id = s.ticket_id
);

PROMPT [6] 예약/티켓/결제/환불 생성 완료

/* =============================================================================
[7] 관심 노선/항공편 테스트 데이터
============================================================================= */

INSERT INTO FAVORITE_ROUTE (member_id, favorite_type, departure_airport_id, arrival_airport_id, flight_id)
SELECT s.member_id, s.favorite_type, s.departure_airport_id, s.arrival_airport_id, s.flight_id
FROM (
    SELECT (SELECT member_id FROM MEMBER WHERE login_id = 'dummy_user_pay') member_id, 'ROUTE' favorite_type, (SELECT airport_id FROM AIRPORT WHERE iata_code = 'GMP') departure_airport_id, (SELECT airport_id FROM AIRPORT WHERE iata_code = 'CJU') arrival_airport_id, CAST(NULL AS NUMBER) flight_id FROM dual UNION ALL
    SELECT (SELECT member_id FROM MEMBER WHERE login_id = 'dummy_user_pay'), 'ROUTE', (SELECT airport_id FROM AIRPORT WHERE iata_code = 'ICN'), (SELECT airport_id FROM AIRPORT WHERE iata_code = 'NRT'), CAST(NULL AS NUMBER) FROM dual UNION ALL
    SELECT (SELECT member_id FROM MEMBER WHERE login_id = 'dummy_user_refund'), 'ROUTE', (SELECT airport_id FROM AIRPORT WHERE iata_code = 'ICN'), (SELECT airport_id FROM AIRPORT WHERE iata_code = 'KIX'), CAST(NULL AS NUMBER) FROM dual UNION ALL
    SELECT (SELECT member_id FROM MEMBER WHERE login_id = 'dummy_user_pay'), 'FLIGHT', CAST(NULL AS NUMBER), CAST(NULL AS NUMBER), (SELECT flight_id FROM FLIGHT WHERE flight_no = 'SR101' AND departure_time = CAST(TRUNC(SYSDATE) + 1 + 9/24 AS TIMESTAMP)) FROM dual UNION ALL
    SELECT (SELECT member_id FROM MEMBER WHERE login_id = 'dummy_user_refund'), 'FLIGHT', CAST(NULL AS NUMBER), CAST(NULL AS NUMBER), (SELECT flight_id FROM FLIGHT WHERE flight_no = 'SR201' AND departure_time = CAST(TRUNC(SYSDATE) + 1 + 11/24 AS TIMESTAMP)) FROM dual
) s
WHERE NOT EXISTS (
    SELECT 1 FROM FAVORITE_ROUTE t
    WHERE t.member_id = s.member_id
      AND NVL(t.favorite_type, '-') = NVL(s.favorite_type, '-')
      AND NVL(t.departure_airport_id, -1) = NVL(s.departure_airport_id, -1)
      AND NVL(t.arrival_airport_id, -1) = NVL(s.arrival_airport_id, -1)
      AND NVL(t.flight_id, -1) = NVL(s.flight_id, -1)
);

PROMPT [7] 관심 노선/항공편 생성 완료

COMMIT;
WHENEVER SQLERROR CONTINUE;

PROMPT [8] 전체 COMMIT 완료
PROMPT [9] 데이터 건수 확인

SELECT 'REGION' AS table_name, COUNT(*) AS cnt FROM REGION UNION ALL
SELECT 'GATE_AREA', COUNT(*) FROM GATE_AREA UNION ALL
SELECT 'ROUTE_TYPE', COUNT(*) FROM ROUTE_TYPE UNION ALL
SELECT 'SEAT_CLASS', COUNT(*) FROM SEAT_CLASS UNION ALL
SELECT 'SEASON', COUNT(*) FROM SEASON UNION ALL
SELECT 'AIRPORT', COUNT(*) FROM AIRPORT UNION ALL
SELECT 'GATE', COUNT(*) FROM GATE UNION ALL
SELECT 'ROUTE', COUNT(*) FROM ROUTE UNION ALL
SELECT 'AIRCRAFT', COUNT(*) FROM AIRCRAFT UNION ALL
SELECT 'SEAT', COUNT(*) FROM SEAT UNION ALL
SELECT 'FARE', COUNT(*) FROM FARE UNION ALL
SELECT 'FLIGHT', COUNT(*) FROM FLIGHT UNION ALL
SELECT 'FLIGHT_FARE', COUNT(*) FROM FLIGHT_FARE UNION ALL
SELECT 'FLIGHT_NOTICE', COUNT(*) FROM FLIGHT_NOTICE UNION ALL
SELECT 'SAVED_PASSENGER', COUNT(*) FROM SAVED_PASSENGER UNION ALL
SELECT 'BOOKING', COUNT(*) FROM BOOKING UNION ALL
SELECT 'BOOKING_PASSENGER', COUNT(*) FROM BOOKING_PASSENGER UNION ALL
SELECT 'TICKET', COUNT(*) FROM TICKET UNION ALL
SELECT 'PAYMENT', COUNT(*) FROM PAYMENT UNION ALL
SELECT 'REFUND', COUNT(*) FROM REFUND UNION ALL
SELECT 'FAVORITE_ROUTE', COUNT(*) FROM FAVORITE_ROUTE;

PROMPT [10] 핵심 데이터 미리보기

SELECT f.flight_no,
       da.iata_code AS departure_iata,
       aa.iata_code AS arrival_iata,
       f.departure_time,
       f.arrival_time,
       f.flight_status,
       f.delay_minutes
FROM FLIGHT f
JOIN ROUTE r ON f.route_id = r.route_id
JOIN AIRPORT da ON r.departure_airport_id = da.airport_id
JOIN AIRPORT aa ON r.arrival_airport_id = aa.airport_id
WHERE f.flight_no IN ('SR101', 'SR102', 'SR201', 'SR202', 'SR301')
ORDER BY f.departure_time;

SELECT b.booking_no,
       b.trip_type,
       b.status AS booking_status,
       b.total_amount,
       p.method,
       p.status AS payment_status,
       p.amount AS payment_amount
FROM BOOKING b
LEFT JOIN PAYMENT p ON b.booking_id = p.booking_id
WHERE b.booking_no IN ('BKDUMMY001', 'BKDUMMY002', 'BKDUMMY003', 'BKDUMMY004', 'BKDUMMY005')
ORDER BY b.booking_no;
