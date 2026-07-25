package kr.spring.admin.service;

import java.util.List;

import kr.spring.admin.vo.AirCraftSeatClassVO;
import kr.spring.admin.vo.AirCraftVO;

public interface AirCraftService {

    // 등록
    void insertAircraft(AirCraftVO aircraft);

    // 전체 목록
    List<AirCraftVO> selectListAircraft();

    // 항공기 제원 수정
    String updateAircraft(AirCraftVO aircraft);

    // 사용 여부 변경
    String updateAircraftStatus(
            int aircraft_id,
            String is_active
    );

    // 항공기 삭제
    String deleteAircraft(int aircraft_id);

    // 사용 중인 항공기 목록
    List<AirCraftVO> selectActiveAircraftList();

    // 사용 중인 항공기 단건
    AirCraftVO selectActiveAircraft(int aircraft_id);

    // 좌석 등급별 좌석 수
    List<AirCraftSeatClassVO> selectSeatClassCountList(
            int aircraft_id
    );
}