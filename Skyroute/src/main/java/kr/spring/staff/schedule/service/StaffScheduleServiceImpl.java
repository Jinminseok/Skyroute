package kr.spring.staff.schedule.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.spring.staff.schedule.dao.StaffScheduleMapper;
import kr.spring.staff.schedule.vo.ScheduleVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/*
 * 운항 스케줄 서비스
 *
 * [핵심 원칙]
 * FLIGHT 과 FLIGHT_FARE 는 항상 함께 태어나고 함께 죽는다.
 * 운임 없는 항공편은 검색 쿼리의 INNER JOIN FLIGHT_FARE 에서 조용히 사라져
 * "등록은 됐는데 조회가 안 되는" 유령 항공편이 된다.
 * 따라서 운임 생성에 실패하면 항공편 등록 자체를 롤백한다.
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class StaffScheduleServiceImpl implements StaffScheduleService {
	
	private static final long DOMESTIC_TURNAROUND_MINUTES = 60L;
	private static final long INTERNATIONAL_TURNAROUND_MINUTES = 90L;

    private final StaffScheduleMapper staffScheduleMapper;


    @Override
    public List<ScheduleVO> getScheduleList() {
        return staffScheduleMapper.selectScheduleList();
    }


    @Override
    public ScheduleVO getSchedule(int flightId) {
        return staffScheduleMapper.selectSchedule(flightId);
    }


    /* =====================================================================
       등록 : FLIGHT → FLIGHT_FARE 순서로 한 트랜잭션에서 처리
       ===================================================================== */
    @Override
    public void insertSchedule(ScheduleVO scheduleVO) {

        // 1. 출발/도착 시각 기본 검증
        validateScheduleTime(scheduleVO);

        // 2. 출발일 기준 시즌 및 운임 검증
        validateFareAvailable(
                scheduleVO.getRoute_id(),
                scheduleVO.getDeparture_time()
        );

        // 3. 항공기 시간 중복, 위치, 회항시간 검증
        validateAircraftRotation(scheduleVO, null);

        // 4. FLIGHT 저장
        staffScheduleMapper.insertSchedule(scheduleVO);

        int flightId = scheduleVO.getFlight_id();

        // 5. 좌석 등급별 FLIGHT_FARE 생성
        int fareCount = staffScheduleMapper.insertFlightFare(flightId);

        if (fareCount == 0) {
            throw new IllegalStateException(
                    "운임을 생성하지 못했습니다. "
                    + "노선의 운임(FARE) 등록 상태를 확인해 주세요."
            );
        }

        log.debug(
                "<<항공편 등록>> flight_id={}, 생성된 운임={}건",
                flightId,
                fareCount
        );
    }


    /* =====================================================================
       수정 : 노선 또는 출발일이 바뀌면 적용 운임도 달라진다
       ===================================================================== */
    @Override
    public void updateSchedule(ScheduleVO scheduleVO) {

        int flightId = scheduleVO.getFlight_id();

        ScheduleVO before =
                staffScheduleMapper.selectSchedule(flightId);

        if (before == null) {
            throw new IllegalStateException(
                    "존재하지 않는 항공편입니다."
            );
        }

        validateScheduleTime(scheduleVO);

        boolean routeChanged =
                before.getRoute_id()
                != scheduleVO.getRoute_id();

        boolean aircraftChanged =
                before.getAircraft_id()
                != scheduleVO.getAircraft_id();

        boolean departureChanged =
                !Objects.equals(
                        before.getDeparture_time(),
                        scheduleVO.getDeparture_time()
                );

        boolean arrivalChanged =
                !Objects.equals(
                        before.getArrival_time(),
                        scheduleVO.getArrival_time()
                );

        // 노선이나 출발 시각이 변경되면 운임을 다시 생성해야 한다.
        boolean fareAffected =
                routeChanged || departureChanged;

        // 아래 값이 바뀌면 항공기 로테이션을 다시 검증해야 한다.
        boolean rotationAffected =
                routeChanged
                || aircraftChanged
                || departureChanged
                || arrivalChanged;

        /*
         * 예약이 있는 항공편의 노선·항공기·출발 시각 변경을 차단한다.
         *
         * 항공기를 변경하면 기존 TICKET.seat_id가 이전 항공기 좌석을
         * 계속 참조하는 문제가 생기므로 항공기 변경도 차단해야 한다.
         */
        if (fareAffected || aircraftChanged) {

            int activeTicket =
                    staffScheduleMapper.countActiveTicket(flightId);

            if (activeTicket > 0) {
                throw new IllegalStateException(
                        "예약이 있는 항공편은 노선·항공기·출발 시각을 "
                        + "변경할 수 없습니다. 결항 처리 후 신규 등록해 주세요. "
                        + "(점유 좌석 " + activeTicket + "건)"
                );
            }
        }

        if (fareAffected) {
            validateFareAvailable(
                    scheduleVO.getRoute_id(),
                    scheduleVO.getDeparture_time()
            );
        }

        if (rotationAffected) {

            /*
             * 현재 수정 중인 항공편을 중복 검사 대상에서 제외하기 위해
             * flightId를 전달한다.
             */
            validateAircraftRotation(
                    scheduleVO,
                    flightId
            );
        }

        staffScheduleMapper.updateSchedule(scheduleVO);

        if (fareAffected) {

            staffScheduleMapper.deleteFlightFare(flightId);

            int fareCount =
                    staffScheduleMapper.insertFlightFare(flightId);

            if (fareCount == 0) {
                throw new IllegalStateException(
                        "변경된 조건에 해당하는 운임을 "
                        + "생성하지 못했습니다."
                );
            }

            log.debug(
                    "<<항공편 수정 - 운임 재생성>> "
                    + "flight_id={}, 운임={}건",
                    flightId,
                    fareCount
            );
        }
    }


    @Override
    public void deleteSchedule(int flightId) {

        int activeTicket = staffScheduleMapper.countActiveTicket(flightId);

        if (activeTicket > 0) {
            throw new IllegalStateException(
                    "예약이 있는 항공편은 삭제할 수 없습니다. 결항(CANCELLED) 처리해 주세요. "
                    + "(점유 좌석 " + activeTicket + "건)");
        }

        // FLIGHT 은 소프트 삭제이므로 FLIGHT_FARE 는 남겨 둔다 (복구 가능)
        staffScheduleMapper.deleteSchedule(flightId);
    }


    @Override
    public void updateFlightStatus(ScheduleVO scheduleVO) {
        staffScheduleMapper.updateFlightStatus(scheduleVO);
    }

    
    /*
     * 출발 시각과 도착 시각의 기본 형식 및 순서 검증
     */
    private void validateScheduleTime(ScheduleVO scheduleVO) {

        LocalDateTime departure = parseDateTime(
                scheduleVO.getDeparture_time(),
                "출발 시각"
        );

        LocalDateTime arrival = parseDateTime(
                scheduleVO.getArrival_time(),
                "도착 시각"
        );

        if (!arrival.isAfter(departure)) {
            throw new IllegalStateException(
                    "도착 시각은 출발 시각보다 이후여야 합니다."
            );
        }
    }


    /*
     * 항공기 로테이션 전체 검증
     */
    private void validateAircraftRotation(
            ScheduleVO scheduleVO,
            Integer excludeFlightId
    ) {

        ScheduleVO routeInfo =
                staffScheduleMapper.selectRouteInfo(
                        scheduleVO.getRoute_id()
                );

        if (routeInfo == null) {
            throw new IllegalStateException(
                    "선택한 노선이 없거나 비활성화 상태입니다."
            );
        }

        /*
         * 동일 항공기가 같은 시간대에 이미 운항하는지 확인
         */
        int overlapCount =
                staffScheduleMapper.countAircraftTimeOverlap(
                        scheduleVO.getAircraft_id(),
                        scheduleVO.getDeparture_time(),
                        scheduleVO.getArrival_time(),
                        excludeFlightId
                );

        if (overlapCount > 0) {
            throw new IllegalStateException(
                    "선택한 항공기는 입력한 시간대에 "
                    + "이미 다른 운항 스케줄이 있습니다."
            );
        }

        /*
         * 신규 스케줄보다 앞에 위치한 항공편 조회
         */
        ScheduleVO previous =
                staffScheduleMapper.selectPreviousAircraftFlight(
                        scheduleVO.getAircraft_id(),
                        scheduleVO.getDeparture_time(),
                        excludeFlightId
                );

        if (previous != null) {
            validatePreviousConnection(
                    previous,
                    routeInfo,
                    scheduleVO
            );
        }

        /*
         * 신규 스케줄보다 뒤에 위치한 항공편 조회
         */
        ScheduleVO next =
                staffScheduleMapper.selectNextAircraftFlight(
                        scheduleVO.getAircraft_id(),
                        scheduleVO.getArrival_time(),
                        excludeFlightId
                );

        if (next != null) {
            validateNextConnection(
                    routeInfo,
                    scheduleVO,
                    next
            );
        }
    }


    /*
     * 직전 항공편과 신규 항공편의 연결 검증
     */
    private void validatePreviousConnection(
            ScheduleVO previous,
            ScheduleVO currentRoute,
            ScheduleVO currentSchedule
    ) {

        /*
         * 직전 도착 공항과 신규 출발 공항이 같아야 한다.
         */
        if (previous.getArrival_airport_id()
                != currentRoute.getDeparture_airport_id()) {

            throw new IllegalStateException(
                    "항공기 위치가 연결되지 않습니다. "
                    + "직전 항공편 "
                    + previous.getFlight_no()
                    + "은(는) "
                    + previous.getArrival_airport_code()
                    + "에 도착하지만, 등록하려는 항공편은 "
                    + currentRoute.getDeparture_airport_code()
                    + "에서 출발합니다."
            );
        }

        long requiredMinutes =
                getRequiredTurnaroundMinutes(
                        previous.getFlight_type(),
                        currentRoute.getFlight_type()
                );

        long actualMinutes =
                Duration.between(
                        parseDateTime(
                                previous.getArrival_time(),
                                "직전 항공편 도착 시각"
                        ),
                        parseDateTime(
                                currentSchedule.getDeparture_time(),
                                "출발 시각"
                        )
                ).toMinutes();

        if (actualMinutes < requiredMinutes) {

            throw new IllegalStateException(
                    "직전 항공편 "
                    + previous.getFlight_no()
                    + " 도착 후 회항 준비시간이 부족합니다. "
                    + "필요 "
                    + requiredMinutes
                    + "분 / 현재 "
                    + actualMinutes
                    + "분"
            );
        }
    }


    /*
     * 신규 항공편과 다음 항공편의 연결 검증
     */
    private void validateNextConnection(
            ScheduleVO currentRoute,
            ScheduleVO currentSchedule,
            ScheduleVO next
    ) {

        /*
         * 신규 도착 공항과 다음 출발 공항이 같아야 한다.
         */
        if (currentRoute.getArrival_airport_id()
                != next.getDeparture_airport_id()) {

            throw new IllegalStateException(
                    "항공기 위치가 연결되지 않습니다. "
                    + "등록하려는 항공편은 "
                    + currentRoute.getArrival_airport_code()
                    + "에 도착하지만, 다음 항공편 "
                    + next.getFlight_no()
                    + "은(는) "
                    + next.getDeparture_airport_code()
                    + "에서 출발합니다."
            );
        }

        long requiredMinutes =
                getRequiredTurnaroundMinutes(
                        currentRoute.getFlight_type(),
                        next.getFlight_type()
                );

        long actualMinutes =
                Duration.between(
                        parseDateTime(
                                currentSchedule.getArrival_time(),
                                "도착 시각"
                        ),
                        parseDateTime(
                                next.getDeparture_time(),
                                "다음 항공편 출발 시각"
                        )
                ).toMinutes();

        if (actualMinutes < requiredMinutes) {

            throw new IllegalStateException(
                    "다음 항공편 "
                    + next.getFlight_no()
                    + " 출발 전 회항 준비시간이 부족합니다. "
                    + "필요 "
                    + requiredMinutes
                    + "분 / 현재 "
                    + actualMinutes
                    + "분"
            );
        }
    }


    /*
     * 연결되는 두 항공편 중 하나라도 국제선이면 90분,
     * 둘 다 국내선이면 60분을 적용한다.
     */
    private long getRequiredTurnaroundMinutes(
            String firstFlightType,
            String secondFlightType
    ) {

        if ("INT".equals(firstFlightType)
                || "INT".equals(secondFlightType)) {

            return INTERNATIONAL_TURNAROUND_MINUTES;
        }

        return DOMESTIC_TURNAROUND_MINUTES;
    }


    /*
     * HTML datetime-local 형식:
     * 2026-07-24T08:30
     */
    private LocalDateTime parseDateTime(
            String value,
            String fieldName
    ) {

        if (value == null || value.isBlank()) {
            throw new IllegalStateException(
                    fieldName + "을 입력해 주세요."
            );
        }

        try {
            return LocalDateTime.parse(value);

        } catch (DateTimeParseException e) {

            throw new IllegalStateException(
                    fieldName
                    + " 형식이 올바르지 않습니다. ("
                    + value
                    + ")"
            );
        }
    }

    /* =====================================================================
       공통 검증

       시즌은 화면에서 고르는 값이 아니라 출발일로 결정되는 값이므로,
       "출발일이 유효한 시즌 구간에 들어가는가" 를 등록 전에 확인한다.
       ===================================================================== */
    private void validateFareAvailable(int routeId, String departureTime) {

        int seasonCount = staffScheduleMapper.countSeasonByDate(departureTime);

        if (seasonCount == 0) {
            throw new IllegalStateException(
                    "출발일이 포함된 시즌이 없습니다. 시즌(SEASON)을 먼저 등록해 주세요. "
                    + "(출발일 " + departureTime + ")");
        }

        if (seasonCount > 1) {
            throw new IllegalStateException(
                    "출발일이 " + seasonCount + "개 시즌 구간에 중복 포함됩니다. "
                    + "시즌 기간이 겹치지 않도록 정리해 주세요.");
        }

        int fareCount = staffScheduleMapper.countFareByRouteAndDate(routeId, departureTime);

        if (fareCount == 0) {
            throw new IllegalStateException(
                    "해당 노선의 운임(FARE)이 등록되어 있지 않습니다. "
                    + "기준정보에서 운임을 먼저 등록해 주세요.");
        }
    }
}