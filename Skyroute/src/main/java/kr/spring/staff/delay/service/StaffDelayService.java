package kr.spring.staff.delay.service;

import java.util.List;

import kr.spring.staff.delay.vo.FlightNoticeVO;

public interface StaffDelayService {
	// 지연/결항 안내 등록
    public void insertFlightNotice(FlightNoticeVO noticeVO);
    
    // 지연/결항 안내 이력 조회
    public List<FlightNoticeVO> getFlightNoticeList();
}
