package kr.spring.staff.delay.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import kr.spring.staff.delay.vo.FlightNoticeVO;

@Mapper
public interface StaffFlightNoticeMapper {
	// 지연/결항 안내 등록
    public void insertFlightNotice(FlightNoticeVO noticeVO);
    
    // 지연/결항 안내 이력 조회
    public List<FlightNoticeVO> selectFlightNoticeList();
    
    // 운항 스케줄 동기화(업데이트)
    public void updateFlightStatusFromNotice(FlightNoticeVO noticeVO);
}
