package kr.spring.member.booking.service;

import java.util.List;

import kr.spring.member.booking.vo.AirportOptionVO;
import kr.spring.member.booking.vo.FlightDetailVO;
import kr.spring.member.booking.vo.FlightSearchForm;
import kr.spring.member.booking.vo.FlightSearchResultVO;
import kr.spring.member.booking.vo.SeatClassOptionVO;

public interface FlightSearchService {

	public List<AirportOptionVO>selectActiveAirportList();
	public List<SeatClassOptionVO>selectSeatClassList();
	public void validateReferenceData(FlightSearchForm form);
	public List<FlightSearchResultVO>searchOutboundFlightList(FlightSearchForm form);
	public List<FlightSearchResultVO>searchInboundFlightList(FlightSearchForm form);
	
	public FlightDetailVO selectFlightDetail(Long flightId);
}