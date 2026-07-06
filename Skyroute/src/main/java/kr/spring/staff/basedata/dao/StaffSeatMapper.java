package kr.spring.staff.basedata.dao;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

import kr.spring.admin.vo.AirCraftVO;
import kr.spring.staff.basedata.vo.SeatClassVO;
import kr.spring.staff.basedata.vo.SeatVO;

@Mapper
public interface StaffSeatMapper {
    
    // 이름으로 등급 조회
    public SeatClassVO selectSeatClassByName(String className);
    
    // 등급 마스터 생성
    public void insertSeatClass(SeatClassVO seatClassVO);
    
    // 물리적 좌석 생성
    public void insertSeat(SeatVO seatVO);
    
    // 항공기 목록 전체 조회
    public List<AirCraftVO> selectAircraftList();
    
    // 항공기 사용 여부 변경
    public void updateAircraftActive(Map<String, Object> payload);
    
    // 항공기 상태 변경
    public void updateAircraftStatus(Map<String, Object> payload);
}