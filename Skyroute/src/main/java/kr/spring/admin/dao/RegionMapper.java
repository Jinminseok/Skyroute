package kr.spring.admin.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import kr.spring.admin.vo.RegionVO;

@Mapper
public interface RegionMapper {
	
	//권역 조회
	List<RegionVO> selectRegionList();
	
	//권역 단일 조회
	RegionVO selectRegion(int regionId);
	
	//권역 등록
	int insertRegion(RegionVO region);
	
	//권역 수정
	int updateRegion(RegionVO region);
	
	//권역 삭제
	int deleteRegion(int regionId);
	
	// 권역을 사용하는 공항 수
	int countAirportsByRegionId(int regionId);
	
	// 권역 상태 변경
	int updateRegionStatus(@Param("regionId") int regionId, @Param("isActive") String isActive);
}
