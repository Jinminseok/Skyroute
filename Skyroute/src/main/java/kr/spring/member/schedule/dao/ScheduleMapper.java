package kr.spring.member.schedule.dao;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ScheduleMapper {
	// 1. 공항 목록 조회
		public List<Map<String, Object>> selectAirportList();		
		
		// 2. 실시간 출/도착 운항 현황 조회
		public List<Map<String, Object>> selectFlightStatusList(Map<String, Object> map);
		
		// 3. 정기 운항 스케줄 조회
		public List<Map<String, Object>> selectFlightScheduleList(Map<String, Object> map);
		
		// 출발지, 도착지, 날짜 조건으로 스케줄 리스트 조회
	    List<Map<String, Object>> getScheduleList(String departure, String arrival, String flightDate);
}
