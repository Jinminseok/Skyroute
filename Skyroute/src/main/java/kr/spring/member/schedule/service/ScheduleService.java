package kr.spring.member.schedule.service;

import java.util.List;
import java.util.Map;

public interface ScheduleService {
		// 1. 공항 목록 조회 (검색 폼의 출발지/도착지 셀렉트 박스용)
		public List<Map<String, Object>> selectAirportList();
		
		// 2. 실시간 출/도착 운항 현황 조회 (노선별, 편명별 동적 검색)
		public List<Map<String, Object>> selectFlightStatusList(Map<String, Object> map);
		
		// 3. 정기 운항 스케줄 조회 (가는 편, 오는 편 검색용)
		public List<Map<String, Object>> selectFlightScheduleList(Map<String, Object> map);
		
		// 출발지, 도착지, 날짜 조건으로 스케줄 리스트 조회
	    List<Map<String, Object>> getScheduleList(String departure, String arrival, String flightDate);
}
