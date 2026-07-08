package kr.spring.admin.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import kr.spring.admin.vo.AirCraftVO;

@Mapper
public interface AirCraftMapper {
    
    // 항공기 등록
    public void insertAircraft(AirCraftVO aircraft);
    
    // 보유 항공기 전체 목록 조회
    public List<AirCraftVO> selectListAircraft();
    
    // 항공기 사용여부 상태 변경 (토글)
    public void updateAircraftStatus(@Param("aircraft_id") int aircraft_id, @Param("is_active") String is_active);
}