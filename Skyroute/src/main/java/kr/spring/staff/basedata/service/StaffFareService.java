package kr.spring.staff.basedata.service;

import java.util.List;
import java.util.Map;

import kr.spring.admin.vo.SeasonVO;
import kr.spring.staff.basedata.vo.FareVO;

public interface StaffFareService {
	public List<FareVO> selectFareList(Map<String, Object> map);
    public int selectFareCount(Map<String, Object> map);
    public FareVO selectFare(Long fare_id);
    public void insertFare(FareVO fareVO);
    public void updateFare(FareVO fareVO);
    public String updateFareActive(Map<String, Object> payload);
    
    // Controller에서 호출할 때 예외 처리를 위해 boolean 반환
    public boolean disableFare(Long fare_id);
    public List<SeasonVO> getSeasonList();
}
