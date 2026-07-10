package kr.spring.staff.basedata.dao;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

import kr.spring.staff.basedata.vo.AirportVO;

@Mapper
public interface StaffAirportMapper {
	//공항 목록 조회
		public List<AirportVO> selectAirportList();
		
		//공항 단일 조회
		public AirportVO selectAirport(int airportId);
		
		//공항 등록
		public void insertAirport(AirportVO airportVO);
		
		//공항 수정
		public void updateAirport(AirportVO airportVO);
		
		//공항 삭제
		public void deleteAirport(int airportId);
		
		public void updateAirportActive(AirportVO airportVO);
		
		//권역 목록 조회
		public List<Map<String,Object>> selectRegionList();
		
		public int checkDuplicateAirport(AirportVO airportVO);
}
