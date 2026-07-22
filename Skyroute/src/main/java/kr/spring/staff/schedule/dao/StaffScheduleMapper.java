package kr.spring.staff.schedule.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import kr.spring.staff.schedule.vo.ScheduleVO;

@Mapper
public interface StaffScheduleMapper {

	// 1. 전체 운항 스케줄 조회
	public List<ScheduleVO> selectScheduleList();

	// 2. 단건 조회 (수정 전/후 비교용) - 추가
	public ScheduleVO selectSchedule(int flight_id);

	// 3. 신규 스케줄 등록 (flight_id 회수)
	public void insertSchedule(ScheduleVO scheduleVO);

	// 4. 스케줄 수정
	public void updateSchedule(ScheduleVO scheduleVO);

	// 5. 스케줄 삭제 (Soft Delete)
	public void deleteSchedule(int flight_id);

	// 6. 스케줄 상태 변경
	public void updateFlightStatus(ScheduleVO scheduleVO);


	/* ===== 운임(FLIGHT_FARE) 스냅샷 관리 - 추가 ===== */

	// 7. 운임 스냅샷 생성 (생성된 행 수 반환)
	public int insertFlightFare(int flight_id);

	// 8. 운임 스냅샷 삭제
	public int deleteFlightFare(int flight_id);

	// 9. 출발일이 속한 시즌 개수 (0=공백, 1=정상, 2+=겹침)
	public int countSeasonByDate(@Param("departure_time") String departureTime);

	// 10. 해당 노선 + 출발일 시즌의 운임 개수
	public int countFareByRouteAndDate(@Param("route_id") int routeId,
			@Param("departure_time") String departureTime);

	// 11. 좌석이 점유된 항공편인지 확인
	public int countActiveTicket(int flight_id);

	/* ===== 항공기 로테이션 검증 ===== */

	// 선택한 노선의 출발/도착 공항과 국내선/국제선 구분 조회
	public ScheduleVO selectRouteInfo(int route_id);

	// 동일 항공기의 운항 시간 중복 건수
	public int countAircraftTimeOverlap(
			@Param("aircraft_id") int aircraftId,
			@Param("departure_time") String departureTime,
			@Param("arrival_time") String arrivalTime,
			@Param("exclude_flight_id") Integer excludeFlightId
			);

	// 신규 출발 전에 가장 마지막으로 도착한 항공편
	public ScheduleVO selectPreviousAircraftFlight(
			@Param("aircraft_id") int aircraftId,
			@Param("departure_time") String departureTime,
			@Param("exclude_flight_id") Integer excludeFlightId
			);

	// 신규 도착 뒤에 가장 먼저 출발하는 항공편
	public ScheduleVO selectNextAircraftFlight(
			@Param("aircraft_id") int aircraftId,
			@Param("arrival_time") String arrivalTime,
			@Param("exclude_flight_id") Integer excludeFlightId
			);
}