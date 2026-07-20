package kr.spring.staff.operation.service;

import java.util.List;
import java.util.Map;

import kr.spring.staff.operation.vo.CheckVO;

public interface StaffCheckService {
	List<Map<String, Object>> getFlightList();
	List<CheckVO> getPassengerList(Long flightId);
    void modifyTicketStatus(CheckVO checkVO);
}
