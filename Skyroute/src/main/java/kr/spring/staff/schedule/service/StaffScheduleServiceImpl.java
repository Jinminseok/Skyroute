package kr.spring.staff.schedule.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.spring.staff.schedule.dao.StaffScheduleMapper;
import kr.spring.staff.schedule.vo.ScheduleVO;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
public class StaffScheduleServiceImpl implements StaffScheduleService{
	
	@Autowired
	private StaffScheduleMapper staffScheduleMapper;

	@Override
	public List<ScheduleVO> getScheduleList() {
		return staffScheduleMapper.selectScheduleList();
	}

	@Override
	public void insertSchedule(ScheduleVO scheduleVO) {
		staffScheduleMapper.insertSchedule(scheduleVO);
	}

	@Override
	public void updateSchedule(ScheduleVO scheduleVO) {
		staffScheduleMapper.updateSchedule(scheduleVO);
	}

	@Override
	public void deleteSchedule(int flight_id) {
		staffScheduleMapper.deleteSchedule(flight_id);
	}

	@Override
	public void updateFlightStatus(ScheduleVO scheduleVO) {
		staffScheduleMapper.updateFlightStatus(scheduleVO);
	}

}
