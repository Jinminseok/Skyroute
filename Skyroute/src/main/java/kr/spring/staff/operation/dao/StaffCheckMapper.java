package kr.spring.staff.operation.dao;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

import kr.spring.staff.operation.vo.CheckVO;

@Mapper
public interface StaffCheckMapper {
	//운항 스케줄(드롭다운) 목록 조회 
    List<Map<String, Object>> selectFlightList();
    
    List<CheckVO> selectPassengerList(Long flightId);
    
    // 상태 업데이트
    void updateTicketStatus(CheckVO checkVO);
}
