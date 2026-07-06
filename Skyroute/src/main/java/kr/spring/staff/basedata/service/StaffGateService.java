package kr.spring.staff.basedata.service;

import java.util.List;

import kr.spring.staff.basedata.vo.GateVO;

public interface StaffGateService {
	List<GateVO> getGateList(GateVO searchVO);
    void registerGate(GateVO gateVO);
    void modifyGate(GateVO gateVO);
    void removeGate(Long gateId); 
    void toggleGateActive(Long gateId, String isActive); 
}
