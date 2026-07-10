package kr.spring.member.booking.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import kr.spring.member.booking.vo.AirportOptionVO;
import kr.spring.member.booking.vo.FlightSearchQueryVO;
import kr.spring.member.booking.vo.FlightSearchResultVO;
import kr.spring.member.booking.vo.SeatClassOptionVO;

@Mapper
public interface FlightSearchMapper {

	/*
	 * 활성 공항 목록
	 */
	public List<AirportOptionVO>
			selectActiveAirportList();


	/*
	 * 좌석 등급 목록
	 */
	public List<SeatClassOptionVO>
			selectSeatClassList();


	/*
	 * 출발지와 도착지가 모두 활성 공항인지 확인
	 */
	public int countActiveAirportPair(@Param("departureAirportId") Long departureAirportId,
									  @Param("arrivalAirportId") Long arrivalAirportId);


	/*
	 * 좌석 등급 존재 여부 확인
	 */
	public int countSeatClass(@Param("seatClassId") Long seatClassId);


	/*
	 * 실제 항공편 목록 검색
	 */
	public List<FlightSearchResultVO>
			selectFlightList(FlightSearchQueryVO query);
}