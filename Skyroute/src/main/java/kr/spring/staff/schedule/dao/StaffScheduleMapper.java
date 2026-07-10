package kr.spring.staff.schedule.dao;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import kr.spring.staff.schedule.vo.ScheduleVO;

@Mapper
public interface StaffScheduleMapper {
    // 1. 전체 운항 스케줄 조회
    public List<ScheduleVO> selectScheduleList();
    
    // 2. 신규 스케줄 등록
    public void insertSchedule(ScheduleVO scheduleVO);
    
    // 3. 스케줄 수정
    public void updateSchedule(ScheduleVO scheduleVO);
    
    // 4. 스케줄 삭제 (Soft Delete)
    public void deleteSchedule(int flight_id);
    
    // 5. 스케줄 상태 변경
    public void updateFlightStatus(ScheduleVO scheduleVO);
}