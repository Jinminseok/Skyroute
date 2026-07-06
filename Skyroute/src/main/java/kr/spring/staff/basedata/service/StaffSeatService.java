package kr.spring.staff.basedata.service;

import java.util.List;
import java.util.Map;

import kr.spring.admin.vo.AirCraftVO;

public interface StaffSeatService {
    public void generateSeats(Map<String, Object> payload);
    
    // 보유 항공기 목록 조회
    public List<AirCraftVO> getAircraftList();
    
    // 항공기 사용 여부
    public void updateAircraftActive(Map<String, Object> payload);
    
    // 항공기 상태
    public void updateAircraftStatus(Map<String, Object> payload);
}