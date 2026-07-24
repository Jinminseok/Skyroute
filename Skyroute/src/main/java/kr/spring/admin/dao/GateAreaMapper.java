package kr.spring.admin.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import kr.spring.admin.vo.GateAreaVO;

@Mapper
public interface GateAreaMapper {

	//게이트 전체 조회
	List<GateAreaVO> selectGateAreaList();

	//게이트 단일 조회
	GateAreaVO selectGateArea(int gateareaId);

	//게이트 등록
	int insertGateArea(GateAreaVO gatearea);

	//게이트 수정
	int updateGateArea(GateAreaVO gatearea);

	//게이트 삭제
	int deleteGateArea(int gateareaId);
	
	// 해당 구역을 사용하는 게이트 수
	int countGatesByGateAreaId(int gateAreaId);

	// 게이트 구역 상태 변경
	int updateGateAreaStatus(
		@Param("gateAreaId") int gateAreaId,
		@Param("isActive") String isActive
	);
}
