package kr.spring.admin.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import kr.spring.admin.vo.AirCraftSeatClassVO;
import kr.spring.admin.vo.AirCraftVO;

@Mapper
public interface AirCraftMapper {

    // 항공기 등록
    int insertAircraft(AirCraftVO aircraft);

    // 보유 항공기 전체 목록 조회
    List<AirCraftVO> selectListAircraft();

    // 항공기 단건 조회
    AirCraftVO selectAircraft(
            @Param("aircraft_id") int aircraft_id
    );

    // 항공기 제원 수정
    int updateAircraft(AirCraftVO aircraft);

    // 항공기 사용 여부 변경
    int updateAircraftStatus(
            @Param("aircraft_id") int aircraft_id,
            @Param("is_active") String is_active
    );

    // 항공기 물리 삭제
    int deleteAircraft(
            @Param("aircraft_id") int aircraft_id
    );

    // 해당 항공기에 등록된 좌석 수
    int countSeatsByAircraftId(
            @Param("aircraft_id") int aircraft_id
    );

    // 해당 항공기를 참조하는 전체 운항 스케줄 수
    int countFlightsByAircraftId(
            @Param("aircraft_id") int aircraft_id
    );

    // 진행 중이거나 예정된 운항 스케줄 수
    int countFutureOrRunningFlightsByAircraftId(
            @Param("aircraft_id") int aircraft_id
    );

    // 사용 중인 항공기 목록
    List<AirCraftVO> selectActiveAircraftList();

    // 사용 중인 항공기 단건 조회
    AirCraftVO selectActiveAircraft(
            @Param("aircraft_id") int aircraft_id
    );

    // 항공기 좌석 등급별 좌석 수
    List<AirCraftSeatClassVO> selectSeatClassCountList(
            @Param("aircraft_id") int aircraft_id
    );
}