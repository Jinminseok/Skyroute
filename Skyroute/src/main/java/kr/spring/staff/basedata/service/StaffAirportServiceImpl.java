package kr.spring.staff.basedata.service;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import kr.spring.staff.basedata.dao.StaffAirportMapper;
import kr.spring.staff.basedata.vo.AirportVO;

@Service
public class StaffAirportServiceImpl implements StaffAirportService{

	@Autowired
	private StaffAirportMapper airportMapper;
	
	@Override
	public List<AirportVO> getAirportList() {
		
		return airportMapper.selectAirportList();
	}

	@Override
	public AirportVO getAirport(int airportId) {
		
		return airportMapper.selectAirport(airportId);
	}

	@Override
	public void registerAirport(AirportVO airportVO) {
		airportMapper.insertAirport(airportVO);
		
	}

	@Override
	public void modifyAirport(AirportVO airportVO) {
		airportMapper.updateAirport(airportVO);
		
	}

	@Override
	public void removeAirport(int airportId) {
		 airportMapper.deleteAirport(airportId);
		
	}

	@Override
	public void toggleAirportActive(AirportVO airportVO) {
		airportMapper.updateAirportActive(airportVO);
		
	}

	@Override
	public List<Map<String, Object>> getRegionList() {
		return airportMapper.selectRegionList();
	}

	@Override
	public int checkDuplicateAirport(AirportVO airportVO) {
		return airportMapper.checkDuplicateAirport(airportVO);
	}
	
	@Override
	public List<AirportVO> getActiveAirportList() {
		return airportMapper.selectActiveAirportList();
	}

	@Override
	public AirportVO getActiveAirport(int airportId) {
		return airportMapper.selectActiveAirport(airportId);
	}

}
