package kr.spring.staff.operation.service;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.spring.staff.operation.dao.StaffCheckMapper;
import kr.spring.staff.operation.vo.CheckVO;

@Service
@Transactional
public class StaffCheckServiceImpl implements StaffCheckService{

    @Autowired
    private StaffCheckMapper staffCheckMapper;
    
    @Override
    public List<CheckVO> getPassengerList(Long flightId) {
        return staffCheckMapper.selectPassengerList(flightId);
    }

    @Override
    public void modifyTicketStatus(CheckVO checkVO) {
        staffCheckMapper.updateTicketStatus(checkVO);
    }

    @Override
    public List<Map<String, Object>> getFlightList() {
        return staffCheckMapper.selectFlightList();
    }
}