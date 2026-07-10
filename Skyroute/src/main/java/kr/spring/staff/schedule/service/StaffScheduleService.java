package kr.spring.staff.schedule.service;

import java.util.List;

import kr.spring.staff.schedule.vo.ScheduleVO;

public interface StaffScheduleService {
	public List<ScheduleVO> getScheduleList();
    public void insertSchedule(ScheduleVO scheduleVO);
    public void updateSchedule(ScheduleVO scheduleVO);
    public void deleteSchedule(int flight_id);
    public void updateFlightStatus(ScheduleVO scheduleVO);
}
