/*
    NOTICE 더미데이터 30건
    기준 카테고리:
    SKY_NEWS         = 스카이 소식
    FUEL_SURCHARGE   = 유류할증료
    MEMBERSHIP_CLUB  = 멤버십클럽
    PARTNER_NEWS     = 제휴사 소식
    ETC              = 기타

    주의:
    - created_by는 ACTIVE 상태의 STAFF 계정 중 member_id가 가장 작은 계정으로 자동 지정함.
    - STAFF 계정이 없으면 ORA-01403 오류가 날 수 있음.
    - 기존 테스트 공지를 지우고 정확히 30건만 만들고 싶으면 아래 DELETE를 주석 해제.
*/

SET DEFINE OFF;

-- 정확히 30개만 만들고 싶을 때만 사용
-- DELETE FROM NOTICE;

DECLARE
    v_staff_id NUMBER;
BEGIN
    SELECT member_id
    INTO v_staff_id
    FROM (
        SELECT member_id
        FROM MEMBER
        WHERE role = 'STAFF'
          AND status = 'ACTIVE'
        ORDER BY member_id
    )
    WHERE ROWNUM = 1;

    INSERT INTO NOTICE (category, title, content, is_public, created_by, created_at)
    VALUES (
        'SKY_NEWS',
        '2026년 하계 운항 스케줄 일부 조정 안내',
        q'[2026년 하계 성수기 기간 동안 일부 국내선 및 국제선 운항 시간이 조정됩니다.
항공권 예매 및 탑승 전 마이페이지 또는 운항정보 화면에서 최신 출도착 시간을 확인해 주시기 바랍니다.]',
        'Y',
        v_staff_id,
        TO_TIMESTAMP('2026-07-08 09:20', 'YYYY-MM-DD HH24:MI')
    );

    INSERT INTO NOTICE (category, title, content, is_public, created_by, created_at)
    VALUES (
        'SKY_NEWS',
        '인천공항 탑승수속 카운터 운영 위치 안내',
        q'[인천공항 혼잡 완화를 위해 일부 국제선 탑승수속 카운터 운영 위치가 조정됩니다.
공항 도착 후 전광판과 모바일 안내를 확인하시고, 여유 있게 탑승수속을 진행해 주시기 바랍니다.]',
        'Y',
        v_staff_id,
        TO_TIMESTAMP('2026-07-07 16:40', 'YYYY-MM-DD HH24:MI')
    );

    INSERT INTO NOTICE (category, title, content, is_public, created_by, created_at)
    VALUES (
        'SKY_NEWS',
        '국내선 모바일 탑승권 이용 가능 시간 변경 안내',
        q'[국내선 모바일 탑승권 발급 가능 시간이 일부 변경됩니다.
변경된 이용 시간은 항공편 출발 전 체크인 가능 시간에 맞춰 적용되며, 자세한 내용은 체크인 화면에서 확인하실 수 있습니다.]',
        'Y',
        v_staff_id,
        TO_TIMESTAMP('2026-07-06 11:10', 'YYYY-MM-DD HH24:MI')
    );

    INSERT INTO NOTICE (category, title, content, is_public, created_by, created_at)
    VALUES (
        'SKY_NEWS',
        '국제선 사전 체크인 서비스 확대 안내',
        q'[국제선 이용 고객 편의를 위해 사전 체크인 가능 노선이 확대됩니다.
대상 노선은 순차적으로 적용되며, 여권 정보 확인이 필요한 경우 공항 카운터 방문이 필요할 수 있습니다.]',
        'Y',
        v_staff_id,
        TO_TIMESTAMP('2026-07-05 10:30', 'YYYY-MM-DD HH24:MI')
    );

    INSERT INTO NOTICE (category, title, content, is_public, created_by, created_at)
    VALUES (
        'SKY_NEWS',
        '김포공항 라운지 운영 시간 임시 조정 안내',
        q'[김포공항 내 라운지 시설 점검으로 운영 시간이 임시 조정됩니다.
이용 예정 고객께서는 공항 방문 전 운영 시간을 확인해 주시기 바랍니다.]',
        'Y',
        v_staff_id,
        TO_TIMESTAMP('2026-07-04 14:00', 'YYYY-MM-DD HH24:MI')
    );

    INSERT INTO NOTICE (category, title, content, is_public, created_by, created_at)
    VALUES (
        'SKY_NEWS',
        '일본 노선 증편 운항 안내',
        q'[여름 여행 수요 증가에 따라 일부 일본 노선의 운항 횟수가 확대됩니다.
증편 항공편은 항공권 검색 화면에서 순차적으로 조회 가능하며, 운항 일정은 현지 사정에 따라 변경될 수 있습니다.]',
        'Y',
        v_staff_id,
        TO_TIMESTAMP('2026-07-03 09:50', 'YYYY-MM-DD HH24:MI')
    );

    INSERT INTO NOTICE (category, title, content, is_public, created_by, created_at)
    VALUES (
        'SKY_NEWS',
        '기상 악화 시 항공편 운항정보 확인 요청',
        q'[태풍, 집중호우 등 기상 상황에 따라 항공편 운항 시간이 변경되거나 지연될 수 있습니다.
공항으로 이동하기 전 홈페이지의 출도착 조회와 알림 내역을 반드시 확인해 주시기 바랍니다.]',
        'Y',
        v_staff_id,
        TO_TIMESTAMP('2026-07-02 18:15', 'YYYY-MM-DD HH24:MI')
    );

    INSERT INTO NOTICE (category, title, content, is_public, created_by, created_at)
    VALUES (
        'SKY_NEWS',
        '공항 혼잡 시간대 조기 도착 권고 안내',
        q'[성수기 기간 공항 보안검색과 탑승수속 대기 시간이 길어질 수 있습니다.
국내선은 출발 1시간 30분 전, 국제선은 출발 3시간 전까지 공항에 도착하시는 것을 권장드립니다.]',
        'Y',
        v_staff_id,
        TO_TIMESTAMP('2026-07-01 08:40', 'YYYY-MM-DD HH24:MI')
    );

    INSERT INTO NOTICE (category, title, content, is_public, created_by, created_at)
    VALUES (
        'FUEL_SURCHARGE',
        '2026년 7월 국제선 유류할증료 안내',
        q'[2026년 7월 발권분 국제선 항공권에 적용되는 유류할증료를 안내드립니다.
유류할증료는 항공권 구매 시점과 운항 구간에 따라 달라질 수 있으며, 환율 및 유가 변동에 따라 매월 조정될 수 있습니다.]',
        'Y',
        v_staff_id,
        TO_TIMESTAMP('2026-06-30 10:00', 'YYYY-MM-DD HH24:MI')
    );

    INSERT INTO NOTICE (category, title, content, is_public, created_by, created_at)
    VALUES (
        'FUEL_SURCHARGE',
        '2026년 7월 국내선 유류할증료 안내',
        q'[2026년 7월 국내선 항공권 구매 시 적용되는 유류할증료를 안내드립니다.
유류할증료는 항공권 운임과 별도로 부과되며, 구매 이후 환불 시점에 따라 환불 가능 여부가 달라질 수 있습니다.]',
        'Y',
        v_staff_id,
        TO_TIMESTAMP('2026-06-29 10:00', 'YYYY-MM-DD HH24:MI')
    );

    INSERT INTO NOTICE (category, title, content, is_public, created_by, created_at)
    VALUES (
        'FUEL_SURCHARGE',
        '2026년 8월 국제선 유류할증료 사전 안내',
        q'[2026년 8월 국제선 유류할증료 적용 예정 금액을 사전 안내드립니다.
실제 부과 금액은 발권일 기준으로 적용되므로 예약일과 결제일이 다른 경우 금액이 달라질 수 있습니다.]',
        'Y',
        v_staff_id,
        TO_TIMESTAMP('2026-06-28 13:20', 'YYYY-MM-DD HH24:MI')
    );

    INSERT INTO NOTICE (category, title, content, is_public, created_by, created_at)
    VALUES (
        'FUEL_SURCHARGE',
        '항공권 구매 시 유류할증료 적용 기준 안내',
        q'[유류할증료는 항공권 발권일을 기준으로 적용됩니다.
예약만 완료하고 결제가 지연되는 경우 결제 시점의 유류할증료가 반영될 수 있으니 구매 전 최종 결제 금액을 확인해 주시기 바랍니다.]',
        'Y',
        v_staff_id,
        TO_TIMESTAMP('2026-06-27 15:45', 'YYYY-MM-DD HH24:MI')
    );

    INSERT INTO NOTICE (category, title, content, is_public, created_by, created_at)
    VALUES (
        'FUEL_SURCHARGE',
        '노선별 유류할증료 적용 통화 안내',
        q'[국제선 유류할증료는 노선과 출발 국가에 따라 적용 통화가 다를 수 있습니다.
해외 출발 항공권은 현지 통화 또는 결제 통화 기준으로 금액이 표시될 수 있습니다.]',
        'Y',
        v_staff_id,
        TO_TIMESTAMP('2026-06-26 09:25', 'YYYY-MM-DD HH24:MI')
    );

    INSERT INTO NOTICE (category, title, content, is_public, created_by, created_at)
    VALUES (
        'FUEL_SURCHARGE',
        '유류할증료 환불 기준 안내',
        q'[항공권 환불 시 유류할증료는 운임 규정과 환불 신청 시점에 따라 환불 처리됩니다.
일부 프로모션 항공권은 환불 수수료가 별도로 적용될 수 있으므로 상세 운임 규정을 확인해 주시기 바랍니다.]',
        'Y',
        v_staff_id,
        TO_TIMESTAMP('2026-06-25 12:30', 'YYYY-MM-DD HH24:MI')
    );

    INSERT INTO NOTICE (category, title, content, is_public, created_by, created_at)
    VALUES (
        'MEMBERSHIP_CLUB',
        '스카이 멤버십클럽 등급 산정 기준 안내',
        q'[스카이 멤버십클럽 등급은 탑승 실적과 적립 마일리지를 기준으로 산정됩니다.
등급 산정 기간과 반영 기준은 회원 유형에 따라 다를 수 있으니 마이페이지에서 회원 정보를 확인해 주시기 바랍니다.]',
        'Y',
        v_staff_id,
        TO_TIMESTAMP('2026-06-24 10:10', 'YYYY-MM-DD HH24:MI')
    );

    INSERT INTO NOTICE (category, title, content, is_public, created_by, created_at)
    VALUES (
        'MEMBERSHIP_CLUB',
        '멤버십 마일리지 유효기간 안내',
        q'[적립된 마일리지는 적립일과 회원 약관에 따라 유효기간이 적용됩니다.
소멸 예정 마일리지는 마이페이지에서 확인하실 수 있으며, 사용 기한 내 보너스 항공권 또는 제휴 혜택으로 이용해 주시기 바랍니다.]',
        'Y',
        v_staff_id,
        TO_TIMESTAMP('2026-06-23 17:00', 'YYYY-MM-DD HH24:MI')
    );

    INSERT INTO NOTICE (category, title, content, is_public, created_by, created_at)
    VALUES (
        'MEMBERSHIP_CLUB',
        '가족 마일리지 합산 신청 서류 안내',
        q'[가족 마일리지 합산 이용을 위해서는 가족관계를 확인할 수 있는 서류 제출이 필요합니다.
제출 서류는 발급일 기준 유효기간이 적용될 수 있으며, 승인 후 합산 사용이 가능합니다.]',
        'Y',
        v_staff_id,
        TO_TIMESTAMP('2026-06-22 11:40', 'YYYY-MM-DD HH24:MI')
    );

    INSERT INTO NOTICE (category, title, content, is_public, created_by, created_at)
    VALUES (
        'MEMBERSHIP_CLUB',
        '보너스 항공권 예약 대기 운영 안내',
        q'[보너스 항공권 좌석은 항공편별 잔여 좌석 상황에 따라 예약 가능 여부가 달라질 수 있습니다.
예약 대기 신청 후 좌석이 확보되면 알림을 통해 안내드리며, 안내된 기한 내 결제가 필요합니다.]',
        'Y',
        v_staff_id,
        TO_TIMESTAMP('2026-06-21 09:30', 'YYYY-MM-DD HH24:MI')
    );

    INSERT INTO NOTICE (category, title, content, is_public, created_by, created_at)
    VALUES (
        'MEMBERSHIP_CLUB',
        '회원 정보 최신화 캠페인 안내',
        q'[정확한 예약 안내와 알림 수신을 위해 회원 정보 최신화를 부탁드립니다.
휴대전화 번호와 이메일 주소가 올바르지 않은 경우 예약 변경, 결제, 지연/결항 알림을 받지 못할 수 있습니다.]',
        'Y',
        v_staff_id,
        TO_TIMESTAMP('2026-06-20 13:15', 'YYYY-MM-DD HH24:MI')
    );

    INSERT INTO NOTICE (category, title, content, is_public, created_by, created_at)
    VALUES (
        'MEMBERSHIP_CLUB',
        '우수회원 수하물 우대 혜택 안내',
        q'[스카이 멤버십클럽 우수회원에게는 등급별 수하물 우대 혜택이 제공됩니다.
혜택 적용 여부는 탑승 항공편, 운임 종류, 공동운항 여부에 따라 달라질 수 있습니다.]',
        'Y',
        v_staff_id,
        TO_TIMESTAMP('2026-06-19 15:20', 'YYYY-MM-DD HH24:MI')
    );

    INSERT INTO NOTICE (category, title, content, is_public, created_by, created_at)
    VALUES (
        'PARTNER_NEWS',
        '제휴 호텔 마일리지 적립 서비스 안내',
        q'[스카이 제휴 호텔 이용 시 멤버십 번호를 등록하면 숙박 실적에 따라 마일리지를 적립할 수 있습니다.
제휴 호텔별 적립 기준과 적립 시점은 다를 수 있으니 예약 전 이용 조건을 확인해 주시기 바랍니다.]',
        'Y',
        v_staff_id,
        TO_TIMESTAMP('2026-06-18 10:35', 'YYYY-MM-DD HH24:MI')
    );

    INSERT INTO NOTICE (category, title, content, is_public, created_by, created_at)
    VALUES (
        'PARTNER_NEWS',
        '렌터카 제휴 할인 서비스 안내',
        q'[국내외 주요 렌터카 제휴사를 통해 차량을 예약하는 고객에게 할인 혜택이 제공됩니다.
할인율과 적용 조건은 제휴사 사정에 따라 변경될 수 있으며, 예약 전 제휴사 안내를 확인해 주시기 바랍니다.]',
        'Y',
        v_staff_id,
        TO_TIMESTAMP('2026-06-17 16:10', 'YYYY-MM-DD HH24:MI')
    );

    INSERT INTO NOTICE (category, title, content, is_public, created_by, created_at)
    VALUES (
        'PARTNER_NEWS',
        '카드사 제휴 결제 혜택 안내',
        q'[일부 제휴 카드로 항공권을 결제하시는 고객께 청구 할인 또는 무이자 할부 혜택이 제공됩니다.
혜택은 카드사별 조건과 결제 금액에 따라 달라질 수 있으며, 예산 소진 시 조기 종료될 수 있습니다.]',
        'Y',
        v_staff_id,
        TO_TIMESTAMP('2026-06-16 14:50', 'YYYY-MM-DD HH24:MI')
    );

    INSERT INTO NOTICE (category, title, content, is_public, created_by, created_at)
    VALUES (
        'PARTNER_NEWS',
        '쇼핑 제휴몰 마일리지 적립 안내',
        q'[스카이 제휴 쇼핑몰을 경유하여 상품을 구매하면 구매 금액에 따라 마일리지를 적립할 수 있습니다.
마일리지 적립은 제휴몰 이용 조건을 충족한 경우에만 적용되며, 적립 결과는 일정 기간 이후 확인 가능합니다.]',
        'Y',
        v_staff_id,
        TO_TIMESTAMP('2026-06-15 09:10', 'YYYY-MM-DD HH24:MI')
    );

    INSERT INTO NOTICE (category, title, content, is_public, created_by, created_at)
    VALUES (
        'PARTNER_NEWS',
        '공항 리무진 제휴 할인 안내',
        q'[공항 리무진 제휴 노선 이용 고객을 대상으로 항공권 예매 고객 할인 혜택을 제공합니다.
탑승권 또는 예약번호 제시가 필요할 수 있으며, 적용 노선과 기간은 제휴사 운영 상황에 따라 변경될 수 있습니다.]',
        'Y',
        v_staff_id,
        TO_TIMESTAMP('2026-06-14 12:25', 'YYYY-MM-DD HH24:MI')
    );

    INSERT INTO NOTICE (category, title, content, is_public, created_by, created_at)
    VALUES (
        'PARTNER_NEWS',
        '여행자보험 제휴 서비스 안내',
        q'[항공권 예매 고객은 제휴 보험사를 통해 여행자보험 상품을 간편하게 확인할 수 있습니다.
보험 가입과 보상 관련 사항은 제휴 보험사의 약관에 따라 운영됩니다.]',
        'Y',
        v_staff_id,
        TO_TIMESTAMP('2026-06-13 11:05', 'YYYY-MM-DD HH24:MI')
    );

    INSERT INTO NOTICE (category, title, content, is_public, created_by, created_at)
    VALUES (
        'ETC',
        '홈페이지 정기 점검 안내',
        q'[보다 안정적인 서비스 제공을 위해 홈페이지 정기 점검이 진행됩니다.
점검 시간 동안 항공권 검색, 예약, 결제, 마이페이지 일부 기능 이용이 일시적으로 제한될 수 있습니다.]',
        'Y',
        v_staff_id,
        TO_TIMESTAMP('2026-06-12 23:00', 'YYYY-MM-DD HH24:MI')
    );

    INSERT INTO NOTICE (category, title, content, is_public, created_by, created_at)
    VALUES (
        'ETC',
        '개인정보 처리방침 개정 안내',
        q'[개인정보 처리방침 일부 내용이 개정되어 사전 안내드립니다.
개정 내용은 개인정보 수집 항목, 보관 기간, 처리 위탁 관련 항목을 중심으로 반영됩니다.]',
        'Y',
        v_staff_id,
        TO_TIMESTAMP('2026-06-11 09:00', 'YYYY-MM-DD HH24:MI')
    );

    INSERT INTO NOTICE (category, title, content, is_public, created_by, created_at)
    VALUES (
        'ETC',
        '고객센터 운영 시간 변경 안내',
        q'[상담 품질 개선을 위해 고객센터 운영 시간이 일부 변경됩니다.
예약, 결제, 환불 문의는 홈페이지 마이페이지와 자주 묻는 질문을 통해서도 확인하실 수 있습니다.]',
        'Y',
        v_staff_id,
        TO_TIMESTAMP('2026-06-10 18:30', 'YYYY-MM-DD HH24:MI')
    );

    INSERT INTO NOTICE (category, title, content, is_public, created_by, created_at)
    VALUES (
        'ETC',
        '보안 강화를 위한 비밀번호 변경 권고',
        q'[회원 계정 보호를 위해 주기적인 비밀번호 변경을 권장드립니다.
타 사이트와 동일한 비밀번호 사용을 피하고, 영문, 숫자, 특수문자를 조합한 안전한 비밀번호를 사용해 주시기 바랍니다.]',
        'N',
        v_staff_id,
        TO_TIMESTAMP('2026-06-09 10:00', 'YYYY-MM-DD HH24:MI')
    );

    COMMIT;
END;
/

SELECT category, COUNT(*) AS cnt
FROM NOTICE
GROUP BY category
ORDER BY category;

SELECT COUNT(*) AS total_notice_count
FROM NOTICE;