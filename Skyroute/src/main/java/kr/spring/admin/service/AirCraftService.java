package kr.spring.admin.service;

import java.util.List;

import org.apache.ibatis.annotations.Select;

import kr.spring.admin.vo.AirCraftVO;

public interface AirCraftService {
	public void insertAircraft(AirCraftVO aircraft);
    public List<AirCraftVO> selectListAircraft();
    public void updateAircraftStatus(int aircraft_id, String is_active);
}
