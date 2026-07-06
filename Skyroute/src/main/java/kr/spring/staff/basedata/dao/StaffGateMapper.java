package kr.spring.staff.basedata.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import kr.spring.staff.basedata.vo.GateVO;

@Mapper
public interface StaffGateMapper {
	// 게이트 목록 조회 (검색 조건 포함)
    List<GateVO> selectGateList(GateVO searchVO);
    
    // 게이트 코드 중복 확인 (공항ID + 게이트코드)
    int checkGateCodeDuplicate(@Param("airportId") Long airportId, @Param("gateCode") String gateCode);
    
    // 게이트 등록
    int insertGate(GateVO gateVO);
    
    // 게이트 상세 조회 (수정 검증용)
    GateVO selectGateById(Long gateId);
    
    // 게이트 수정 (구역, 국내/국제 구분)
    int updateGate(GateVO gateVO);
    
    // 운항 스케줄에 사용 중인지 확인 (비활성화 검증용)
    int checkGateUsedInSchedule(Long gateId);
    
    // 게이트 사용여부 변경 (활성/비활성)
    int updateGateStatus(@Param("gateId") Long gateId, @Param("isActive") String isActive);
    
    int deleteGate(Long gateId);
}