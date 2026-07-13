package kr.spring.admin.service;

import java.util.List;

import kr.spring.admin.vo.AirCraftSeatClassVO;
import kr.spring.admin.vo.AirCraftVO;

public interface AirCraftService {
	public void insertAircraft(AirCraftVO aircraft);
    public List<AirCraftVO> selectListAircraft();
    public void updateAircraftStatus(int aircraft_id, String is_active);
    
    public List<AirCraftVO> selectActiveAircraftList();

	public AirCraftVO selectActiveAircraft(int aircraft_id);

	public List<AirCraftSeatClassVO> selectSeatClassCountList(int aircraft_id);
}
