package kr.spring.admin.dao;

import java.util.List;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import kr.spring.admin.vo.AirCraftVO;

@Mapper
public interface AirCraftMapper {
	// 항공기 등록
	@Insert("INSERT INTO aircraft (reg_no, model_name, total_seats) "
			+ "VALUES (#{reg_no}, #{model_name}, #{total_seats})")
	public void insertAircraft(AirCraftVO aircraft);
	
	// 보유 항공기 전체 목록 조회
	@Select("SELECT * FROM aircraft ORDER BY aircraft_id DESC")
    public List<AirCraftVO> selectListAircraft();
}
