package kr.spring.staff.schedule.service;

import java.util.List;

import kr.spring.staff.schedule.vo.ScheduleVO;

public interface StaffScheduleService {

    public List<ScheduleVO> getScheduleList();

    /* 추가 : 단건 조회 */
    public ScheduleVO getSchedule(int flightId);

    public void insertSchedule(ScheduleVO scheduleVO);

    public void updateSchedule(ScheduleVO scheduleVO);

    public void deleteSchedule(int flightId);

    public void updateFlightStatus(ScheduleVO scheduleVO);
}