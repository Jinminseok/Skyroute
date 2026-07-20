package kr.spring.staff.delay.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.spring.staff.delay.dao.StaffFlightNoticeMapper;
import kr.spring.staff.delay.vo.FlightNoticeVO;
import kr.spring.staff.schedule.dao.StaffScheduleMapper;
import kr.spring.staff.schedule.vo.ScheduleVO;

@Service
@Transactional
public class StaffDelayServiceImpl implements StaffDelayService{

	@Autowired
	private StaffFlightNoticeMapper staffFlightNoticeMapper;
	
	@Autowired
	private StaffScheduleMapper staffScheduleMapper;
	
	@Override
    public void insertFlightNotice(FlightNoticeVO noticeVO) {
        // 1. FLIGHT_NOTICE 테이블에 이력 저장
		staffFlightNoticeMapper.insertFlightNotice(noticeVO);

        // 2. FLIGHT 테이블의 상태 및 지연시간 업데이트
        ScheduleVO schedule = new ScheduleVO();
        schedule.setFlight_id(noticeVO.getFlight_id());
        schedule.setFlight_status(noticeVO.getNotice_type().equals("DELAY") ? "DELAYED" : "CANCELLED");
        
        // 결항(null)일 경우 0으로 변환하여 저장
        int delayMins = (noticeVO.getDelay_minutes() == null) ? 0 : noticeVO.getDelay_minutes();
        staffScheduleMapper.updateFlightStatus(schedule);
    }

    @Override
    public List<FlightNoticeVO> getFlightNoticeList() {
        return staffFlightNoticeMapper.selectFlightNoticeList();
    }
	
}
