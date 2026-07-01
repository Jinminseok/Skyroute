package kr.spring.admin.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import kr.spring.admin.vo.SeasonVO;

@Mapper
public interface SeasonMapper {
	// 시즌 전체 조회
    List<SeasonVO> selectSeasonList();

    // 시즌 단일 조회
    SeasonVO selectSeason(int seasonId);

    // 시즌 등록
    int insertSeason(SeasonVO season);

    // 시즌 수정
    int updateSeason(SeasonVO season);

    // 시즌 삭제
    int deleteSeason(int seasonId);

    // 시즌 상태 비동기 변경 (토글)
    int updateSeasonStatus(@Param("seasonId") int seasonId, @Param("isActive") String isActive);
}
