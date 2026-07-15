package kr.spring.member.schedule.service;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.spring.member.schedule.dao.ScheduleMapper;

@Service
@Transactional
public class ScheduleServiceImpl implements ScheduleService{

	@Autowired
	private ScheduleMapper scheduleMapper;
	
	@Override
	public List<Map<String, Object>> selectAirportList() {
		return scheduleMapper.selectAirportList();
	}

	@Override
	public List<Map<String, Object>> selectFlightStatusList(Map<String, Object> map) {
		return scheduleMapper.selectFlightStatusList(map);
	}

	@Override
	public List<Map<String, Object>> selectFlightScheduleList(Map<String, Object> map) {
		return scheduleMapper.selectFlightScheduleList(map);
	}

	@Override
	public List<Map<String, Object>> getScheduleList(String departure, String arrival, String flightDate) {
		return scheduleMapper.getScheduleList(departure, arrival, flightDate);
	}

	@Override
	public List<Map<String, Object>> selectMainFlightStatusTop5() {
		return scheduleMapper.selectMainFlightStatusTop5();
	}

}
