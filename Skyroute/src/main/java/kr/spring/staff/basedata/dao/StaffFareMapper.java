package kr.spring.staff.basedata.dao;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

import kr.spring.admin.vo.SeasonVO;
import kr.spring.staff.basedata.vo.FareVO;

@Mapper
public interface StaffFareMapper {
	//운임 목록 및 조건 검색
	public List<FareVO> selectFareList(Map<String, Object> map);
	public int selectFareCount(Map<String, Object> map);
	
	//운임 상세 조회
	public FareVO selectFare(Long fare_id);
	
	//운임 등록
	public void insertFare(FareVO fareVO);
	
	//운임 수정
	public void updateFare(FareVO fareVO);
	
	//운임 비활성화
	 public void updateFareActive(Map<String, Object> payload);
    
    // 비활성화 전 무결성 검증
    public int checkFareInUse(Long fare_id);
    public List<SeasonVO> selectSeasonList();
	
}
