package kr.spring.staff.basedata.service;

import java.util.List;
import java.util.Map;

import kr.spring.admin.vo.AirCraftVO;
import kr.spring.staff.basedata.vo.SeatClassVO;
import kr.spring.staff.basedata.vo.SeatVO;

public interface StaffSeatService {
    public void generateSeats(Map<String, Object> payload);
    
    // 보유 항공기 목록 조회
    public List<AirCraftVO> getAircraftList();
    
    // 항공기 사용 여부
    public void updateAircraftActive(Map<String, Object> payload);
    
    // 항공기 상태
    public void updateAircraftStatus(Map<String, Object> payload);
    
    // 좌석 리스트
    public List<Map<String, Object>> getSeatSummaryList();
    public List<SeatVO> getSeatsByAircraft(int aircraftId);
    public List<SeatClassVO> getSeatClassList();
    
    // 좌석 상태 변경
    public void updateSeatActive(Map<String, Object> payload);

    // 좌석 초기화(삭제)
    public void deleteSeatsByAircraft(int aircraftId);
}