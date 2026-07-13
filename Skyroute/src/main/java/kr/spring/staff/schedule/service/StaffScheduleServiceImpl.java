package kr.spring.staff.schedule.service;

import java.util.List;

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

        // 1) 사전 검증 : 출발일이 시즌 구간에 정확히 1개 걸치는가
        validateFareAvailable(scheduleVO.getRoute_id(), scheduleVO.getDeparture_time());

        // 2) 항공편 저장 (useGeneratedKeys 로 flight_id 회수)
        staffScheduleMapper.insertSchedule(scheduleVO);

        int flightId = scheduleVO.getFlight_id();

        // 3) 운임 스냅샷 생성
        int fareCount = staffScheduleMapper.insertFlightFare(flightId);

        if (fareCount == 0) {
            // 사전 검증을 통과했는데도 0건이면 데이터가 그 사이 바뀐 것이므로 롤백
            throw new IllegalStateException(
                    "운임을 생성하지 못했습니다. 노선의 운임(FARE) 등록 상태를 확인해 주세요.");
        }

        log.debug("<<항공편 등록>> flight_id={}, 생성된 운임={}건", flightId, fareCount);
    }


    /* =====================================================================
       수정 : 노선 또는 출발일이 바뀌면 적용 운임도 달라진다
       ===================================================================== */
    @Override
    public void updateSchedule(ScheduleVO scheduleVO) {

        int flightId = scheduleVO.getFlight_id();

        ScheduleVO before = staffScheduleMapper.selectSchedule(flightId);

        if (before == null) {
            throw new IllegalStateException("존재하지 않는 항공편입니다.");
        }

        // 운임에 영향을 주는 변경인가 (노선 or 출발일)
        boolean fareAffected =
                before.getRoute_id() != scheduleVO.getRoute_id()
                || !before.getDeparture_time().equals(scheduleVO.getDeparture_time());

        if (fareAffected) {

            /*
             * 예약이 걸린 항공편은 노선/출발일을 바꿀 수 없다.
             * 운임을 재생성하면 TICKET.fare_amount(결제 금액)와 어긋나
             * 환불 금액을 계산할 근거가 사라진다.
             * 실제 항공사도 이 경우 결항 처리 후 재예약을 유도한다.
             */
            int activeTicket = staffScheduleMapper.countActiveTicket(flightId);

            if (activeTicket > 0) {
                throw new IllegalStateException(
                        "예약이 있는 항공편은 노선과 출발일을 변경할 수 없습니다. "
                        + "결항 처리 후 신규 등록해 주세요. (점유 좌석 " + activeTicket + "건)");
            }

            validateFareAvailable(scheduleVO.getRoute_id(), scheduleVO.getDeparture_time());
        }

        staffScheduleMapper.updateSchedule(scheduleVO);

        if (fareAffected) {

            staffScheduleMapper.deleteFlightFare(flightId);

            int fareCount = staffScheduleMapper.insertFlightFare(flightId);

            if (fareCount == 0) {
                throw new IllegalStateException(
                        "변경된 조건에 해당하는 운임을 생성하지 못했습니다.");
            }

            log.debug("<<항공편 수정 - 운임 재생성>> flight_id={}, 운임={}건", flightId, fareCount);
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