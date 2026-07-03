package kr.spring.staff.basedata.service;

import java.util.List;

import kr.spring.staff.basedata.vo.AirportVO;

public interface StaffAirportService {
	public List<AirportVO> getAirportList();
    public AirportVO getAirport(int airportId);
    public void registerAirport(AirportVO airportVO);
    public void modifyAirport(AirportVO airportVO);
    public void removeAirport(int airportId);
    public void toggleAirportActive(AirportVO airportVO);
    
}
