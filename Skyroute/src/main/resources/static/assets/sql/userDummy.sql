-- =============================================================================
-- SkyRoute 거래성 테이블 통합 더미데이터
-- 대상: SAVED_PASSENGER / BOOKING / BOOKING_PASSENGER / TICKET / PAYMENT / REFUND
--       FAVORITE_ROUTE / NOTIFICATION / CHAT_INQUIRY / CHAT_MESSAGE / FAQ
-- 전제: MEMBER(USER 회원), 항공편 KE101/KE102(HL8001), 좌석(HL8001), FLIGHT_FARE 존재
-- 특징: IDENTITY id를 RETURNING INTO로 잡아 자식 테이블에 연결. 예약/결제/환불 정합.
--       국제선 KE201(HL8003)은 좌석 미시드라 티켓 대상에서 제외.
-- =============================================================================

DECLARE
  -- 회원
  v_user1 NUMBER; v_user2 NUMBER; v_staff NUMBER;
  -- 항공편 / 공항 / 좌석등급
  v_ke101 NUMBER; v_ke102 NUMBER;
  v_gmp   NUMBER; v_cju   NUMBER; v_nrt   NUMBER;
  v_eco   NUMBER;
  -- 좌석 (HL8001)
  v_s2a NUMBER; v_s2b NUMBER; v_s2c NUMBER; v_s3a NUMBER;
  -- 운임
  v_fare101 NUMBER; v_fare102 NUMBER;
  -- 등록 탑승객
  v_sp1 NUMBER; v_sp2 NUMBER; v_sp3 NUMBER;
  -- 예약 / 탑승객 / 티켓 / 결제 / 환불용 캡처
  v_bkA NUMBER; v_bkB NUMBER; v_bkC NUMBER;
  v_paxA NUMBER; v_paxB NUMBER; v_paxC NUMBER;
  v_tkB_in NUMBER; v_tkC NUMBER;
  v_payB NUMBER; v_payC NUMBER;
  -- 채팅
  v_inq1 NUMBER; v_inq2 NUMBER;
BEGIN
  ---------------------------------------------------------------------------
  -- 0) 참조값 조회 + 프리체크 (없으면 명확히 중단)
  ---------------------------------------------------------------------------
  SELECT MIN(member_id) INTO v_user1 FROM MEMBER WHERE role='USER';
  SELECT MIN(member_id) INTO v_user2 FROM MEMBER WHERE role='USER' AND member_id > v_user1;
  SELECT MIN(member_id) INTO v_staff FROM MEMBER WHERE role IN ('STAFF','ADMIN');
  IF v_user1 IS NULL THEN
    RAISE_APPLICATION_ERROR(-20001,'USER 회원이 없습니다. MEMBER 시드부터 확인하세요.');
  END IF;
  IF v_user2 IS NULL THEN v_user2 := v_user1; END IF;   -- 회원 1명뿐이면 재사용
  IF v_staff IS NULL THEN v_staff := v_user1; END IF;   -- STAFF 없으면 대체(더미용)

  SELECT MIN(flight_id) INTO v_ke101 FROM FLIGHT WHERE flight_no='KE101';
  SELECT MIN(flight_id) INTO v_ke102 FROM FLIGHT WHERE flight_no='KE102';
  IF v_ke101 IS NULL OR v_ke102 IS NULL THEN
    RAISE_APPLICATION_ERROR(-20002,'KE101/KE102 항공편이 없습니다. 운항스케줄 시드부터 확인하세요.');
  END IF;

  SELECT MIN(airport_id) INTO v_gmp FROM AIRPORT WHERE iata_code='GMP';
  SELECT MIN(airport_id) INTO v_cju FROM AIRPORT WHERE iata_code='CJU';
  SELECT MIN(airport_id) INTO v_nrt FROM AIRPORT WHERE iata_code='NRT';

  SELECT MIN(seat_class_id) INTO v_eco FROM SEAT_CLASS WHERE class_name='이코노미';

  SELECT MIN(s.seat_id) INTO v_s2a FROM SEAT s JOIN AIRCRAFT a ON a.aircraft_id=s.aircraft_id
    WHERE a.reg_no='HL8001' AND s.seat_no='1A';
  SELECT MIN(s.seat_id) INTO v_s2b FROM SEAT s JOIN AIRCRAFT a ON a.aircraft_id=s.aircraft_id
    WHERE a.reg_no='HL8001' AND s.seat_no='1B';
  SELECT MIN(s.seat_id) INTO v_s2c FROM SEAT s JOIN AIRCRAFT a ON a.aircraft_id=s.aircraft_id
    WHERE a.reg_no='HL8001' AND s.seat_no='1C';
  SELECT MIN(s.seat_id) INTO v_s3a FROM SEAT s JOIN AIRCRAFT a ON a.aircraft_id=s.aircraft_id
    WHERE a.reg_no='HL8001' AND s.seat_no='1D';
  IF v_s2a IS NULL OR v_s2b IS NULL OR v_s2c IS NULL OR v_s3a IS NULL THEN
    RAISE_APPLICATION_ERROR(-20003,'HL8001 좌석(1A/1B/1C/1D)이 없습니다. 좌석 시드부터 확인하세요.');
  END IF;

  SELECT MIN(price) INTO v_fare101 FROM FLIGHT_FARE WHERE flight_id=v_ke101 AND seat_class_id=v_eco;
  SELECT MIN(price) INTO v_fare102 FROM FLIGHT_FARE WHERE flight_id=v_ke102 AND seat_class_id=v_eco;
  IF v_fare101 IS NULL OR v_fare102 IS NULL THEN
    RAISE_APPLICATION_ERROR(-20004,'KE101/KE102 이코노미 FLIGHT_FARE가 없습니다. FARE->FLIGHT_FARE 시드부터 확인하세요.');
  END IF;

  ---------------------------------------------------------------------------
  -- 1) 등록 탑승객 SAVED_PASSENGER (user1 2명, user2 1명)
  ---------------------------------------------------------------------------
  INSERT INTO SAVED_PASSENGER (member_id, name, birth_date, phone, gender)
  VALUES (v_user1, '홍길동', DATE '1990-05-15', '010-1111-2222', 'M')
  RETURNING saved_passenger_id INTO v_sp1;
  INSERT INTO SAVED_PASSENGER (member_id, name, birth_date, phone, gender)
  VALUES (v_user1, '홍길순', DATE '1993-08-21', '010-1111-3333', 'F')
  RETURNING saved_passenger_id INTO v_sp2;
  INSERT INTO SAVED_PASSENGER (member_id, name, birth_date, phone, gender)
  VALUES (v_user2, '김영희', DATE '1988-11-03', '010-4444-5555', 'F')
  RETURNING saved_passenger_id INTO v_sp3;

  ---------------------------------------------------------------------------
  -- 2) 예약 A : user1 / 편도 KE101 / PENDING (결제 대기)
  ---------------------------------------------------------------------------
  INSERT INTO BOOKING (booking_no, member_id, trip_type, outbound_flight_id,
                       inbound_flight_id, status, total_amount)
  VALUES ('BK-2026-0001', v_user1, 'ONEWAY', v_ke101, NULL, 'PENDING', v_fare101)
  RETURNING booking_id INTO v_bkA;

  INSERT INTO BOOKING_PASSENGER (booking_id, saved_passenger_id, name, birth_date, phone, gender)
  VALUES (v_bkA, v_sp1, '홍길동', DATE '1990-05-15', '010-1111-2222', 'M')
  RETURNING booking_passenger_id INTO v_paxA;

  INSERT INTO TICKET (booking_id, booking_passenger_id, flight_id, seat_id,
                      leg_type, fare_amount, hold_status)
  VALUES (v_bkA, v_paxA, v_ke101, v_s2a, 'OUTBOUND', v_fare101, 'HOLDING');

  -- 결제 A : READY (아직 미결제)
  INSERT INTO PAYMENT (booking_id, method, amount, status, merchant_uid)
  VALUES (v_bkA, 'CARD', v_fare101, 'READY', 'ord_2026_0001');

  ---------------------------------------------------------------------------
  -- 3) 예약 B : user1 / 왕복 KE101+KE102 / CONFIRMED / 부분환불(오는편 취소)
  ---------------------------------------------------------------------------
  INSERT INTO BOOKING (booking_no, member_id, trip_type, outbound_flight_id,
                       inbound_flight_id, status, total_amount)
  VALUES ('BK-2026-0002', v_user1, 'ROUNDTRIP', v_ke101, v_ke102, 'CONFIRMED',
          v_fare101 + v_fare102)
  RETURNING booking_id INTO v_bkB;

  INSERT INTO BOOKING_PASSENGER (booking_id, saved_passenger_id, name, birth_date, phone, gender)
  VALUES (v_bkB, v_sp2, '홍길순', DATE '1993-08-21', '010-1111-3333', 'F')
  RETURNING booking_passenger_id INTO v_paxB;

  -- 가는편: CONFIRMED
  INSERT INTO TICKET (booking_id, booking_passenger_id, flight_id, seat_id,
                      leg_type, fare_amount, hold_status)
  VALUES (v_bkB, v_paxB, v_ke101, v_s2b, 'OUTBOUND', v_fare101, 'CONFIRMED');
  -- 오는편: 부분환불로 CANCELLED (환불 대상)
  INSERT INTO TICKET (booking_id, booking_passenger_id, flight_id, seat_id,
                      leg_type, fare_amount, hold_status)
  VALUES (v_bkB, v_paxB, v_ke102, v_s2c, 'INBOUND', v_fare102, 'CANCELLED')
  RETURNING ticket_id INTO v_tkB_in;

  -- 결제 B : PARTIAL_REFUNDED (오는편 금액만 환불)
  INSERT INTO PAYMENT (booking_id, method, amount, status, imp_uid, merchant_uid,
                       paid_at, refund_amount, refunded_at)
  VALUES (v_bkB, 'KAKAOPAY', v_fare101 + v_fare102, 'PARTIAL_REFUNDED',
          'imp_2026_0002', 'ord_2026_0002', SYSTIMESTAMP, v_fare102, SYSTIMESTAMP)
  RETURNING payment_id INTO v_payB;

  ---------------------------------------------------------------------------
  -- 4) 예약 C : user2 / 편도 KE102 / CANCELLED / 전액환불
  ---------------------------------------------------------------------------
  INSERT INTO BOOKING (booking_no, member_id, trip_type, outbound_flight_id,
                       inbound_flight_id, status, total_amount, cancelled_at)
  VALUES ('BK-2026-0003', v_user2, 'ONEWAY', v_ke102, NULL, 'CANCELLED',
          v_fare102, SYSTIMESTAMP)
  RETURNING booking_id INTO v_bkC;

  INSERT INTO BOOKING_PASSENGER (booking_id, saved_passenger_id, name, birth_date, phone, gender)
  VALUES (v_bkC, v_sp3, '김영희', DATE '1988-11-03', '010-4444-5555', 'F')
  RETURNING booking_passenger_id INTO v_paxC;

  INSERT INTO TICKET (booking_id, booking_passenger_id, flight_id, seat_id,
                      leg_type, fare_amount, hold_status)
  VALUES (v_bkC, v_paxC, v_ke102, v_s3a, 'OUTBOUND', v_fare102, 'CANCELLED')
  RETURNING ticket_id INTO v_tkC;

  -- 결제 C : REFUNDED (전액)
  INSERT INTO PAYMENT (booking_id, method, amount, status, imp_uid, merchant_uid,
                       paid_at, refund_amount, refunded_at)
  VALUES (v_bkC, 'CARD', v_fare102, 'REFUNDED',
          'imp_2026_0003', 'ord_2026_0003', SYSTIMESTAMP, v_fare102, SYSTIMESTAMP)
  RETURNING payment_id INTO v_payC;

  ---------------------------------------------------------------------------
  -- 5) 환불 REFUND (2건) : 예약B 오는편 부분환불 + 예약C 전액환불
  ---------------------------------------------------------------------------
  INSERT INTO REFUND (payment_id, ticket_id, amount, reason)
  VALUES (v_payB, v_tkB_in, v_fare102, '왕복 중 오는편 취소');
  INSERT INTO REFUND (payment_id, ticket_id, amount, reason)
  VALUES (v_payC, v_tkC, v_fare102, '단순 변심 취소');

  ---------------------------------------------------------------------------
  -- 6) 관심(찜) FAVORITE_ROUTE (3건) : ROUTE 2 + FLIGHT 1
  --    CHECK: ROUTE는 dep+arr NOT NULL & flight NULL / FLIGHT는 반대
  ---------------------------------------------------------------------------
  INSERT INTO FAVORITE_ROUTE (member_id, favorite_type, departure_airport_id, arrival_airport_id)
  VALUES (v_user1, 'ROUTE', v_gmp, v_cju);
  INSERT INTO FAVORITE_ROUTE (member_id, favorite_type, departure_airport_id, arrival_airport_id)
  VALUES (v_user2, 'ROUTE', v_gmp, v_nrt);
  INSERT INTO FAVORITE_ROUTE (member_id, favorite_type, flight_id)
  VALUES (v_user1, 'FLIGHT', v_ke101);

  ---------------------------------------------------------------------------
  -- 7) 알림 NOTIFICATION (3건)
  ---------------------------------------------------------------------------
  INSERT INTO NOTIFICATION (member_id, type, content, is_read)
  VALUES (v_user1, 'PAYMENT', '예약 BK-2026-0002 결제가 완료되었습니다.', 'Y');
  INSERT INTO NOTIFICATION (member_id, type, content, is_read)
  VALUES (v_user1, 'FLIGHT_DELAY', 'KE101편 출발이 30분 지연되었습니다.', 'N');
  INSERT INTO NOTIFICATION (member_id, type, content, is_read)
  VALUES (v_user2, 'REFUND', '예약 BK-2026-0003 환불이 완료되었습니다.', 'N');

  ---------------------------------------------------------------------------
  -- 8) 채팅 문의 CHAT_INQUIRY (2건) + 메시지 CHAT_MESSAGE (3건)
  ---------------------------------------------------------------------------
  INSERT INTO CHAT_INQUIRY (member_id, staff_id, status)
  VALUES (v_user1, v_staff, 'IN_PROGRESS')
  RETURNING inquiry_id INTO v_inq1;
  INSERT INTO CHAT_INQUIRY (member_id, staff_id, status, resolved_at)
  VALUES (v_user2, v_staff, 'RESOLVED', SYSTIMESTAMP)
  RETURNING inquiry_id INTO v_inq2;

  INSERT INTO CHAT_MESSAGE (inquiry_id, sender_id, sender_type, content)
  VALUES (v_inq1, v_user1, 'USER', '수하물 규정이 어떻게 되나요?');
  INSERT INTO CHAT_MESSAGE (inquiry_id, sender_id, sender_type, content)
  VALUES (v_inq1, v_staff, 'STAFF', '국내선 기준 위탁수하물 15kg까지 무료입니다.');
  INSERT INTO CHAT_MESSAGE (inquiry_id, sender_id, sender_type, content)
  VALUES (v_inq2, v_user2, 'USER', '환불은 며칠 걸리나요?');

  ---------------------------------------------------------------------------
  -- 9) FAQ (3건) : FK 없음
  ---------------------------------------------------------------------------
  INSERT INTO FAQ (category, question, answer)
  VALUES ('예약', '예약 후 탑승자 이름을 변경할 수 있나요?',
          '출발 24시간 전까지 마이페이지에서 변경 가능합니다.');
  INSERT INTO FAQ (category, question, answer)
  VALUES ('결제', '어떤 결제수단을 지원하나요?',
          '신용카드, 카카오페이, 가상계좌를 지원합니다.');
  INSERT INTO FAQ (category, question, answer)
  VALUES ('취소/환불', '환불 수수료는 얼마인가요?',
          '출발일 기준 잔여일수에 따라 차등 부과됩니다.');

  COMMIT;
END;
/

-- =============================================================================
-- 확인용 (선택)
-- SELECT b.booking_no, b.status, p.status AS pay_status, p.amount, p.refund_amount
--   FROM BOOKING b LEFT JOIN PAYMENT p ON p.booking_id = b.booking_id
--  ORDER BY b.booking_no;
-- SELECT r.refund_id, r.amount, r.reason FROM REFUND r;
-- SELECT category, question FROM FAQ;
-- =============================================================================