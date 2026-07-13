package kr.spring.admin.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.spring.admin.dao.AirCraftMapper;
import kr.spring.admin.vo.AirCraftSeatClassVO;
import kr.spring.admin.vo.AirCraftVO;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class AirCraftServiceImpl implements AirCraftService{
	
	private final AirCraftMapper airCraftMapper;

	@Override
    public void insertAircraft(AirCraftVO aircraft) {
        airCraftMapper.insertAircraft(aircraft);
    }

    @Override
    public List<AirCraftVO> selectListAircraft() {
        return airCraftMapper.selectListAircraft();
    }

	@Override
	public void updateAircraftStatus(int aircraft_id, String is_active) {
		airCraftMapper.updateAircraftStatus(aircraft_id, is_active);
	}

	@Override
	public List<AirCraftVO> selectActiveAircraftList() {
		return airCraftMapper.selectActiveAircraftList();
	}

	@Override
	public AirCraftVO selectActiveAircraft(int aircraft_id) {
		return airCraftMapper.selectActiveAircraft(aircraft_id);
	}

	@Override
	public List<AirCraftSeatClassVO> selectSeatClassCountList(int aircraft_id) {
		return airCraftMapper.selectSeatClassCountList(aircraft_id);
	}

}
