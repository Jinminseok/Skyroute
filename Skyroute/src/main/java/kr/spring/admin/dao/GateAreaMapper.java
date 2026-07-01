package kr.spring.admin.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import kr.spring.admin.vo.GateAreaVO;
import kr.spring.admin.vo.RegionVO;

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
}
