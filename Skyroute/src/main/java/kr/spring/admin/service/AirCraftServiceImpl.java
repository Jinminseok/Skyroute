package kr.spring.admin.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.spring.admin.dao.AirCraftMapper;
import kr.spring.admin.vo.AirCraftSeatClassVO;
import kr.spring.admin.vo.AirCraftVO;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class AirCraftServiceImpl implements AirCraftService {

    private final AirCraftMapper airCraftMapper;


    @Override
    public void insertAircraft(AirCraftVO aircraft) {
        airCraftMapper.insertAircraft(aircraft);
    }


    @Override
    public List<AirCraftVO> selectListAircraft() {
        return airCraftMapper.selectListAircraft();
    }


    /*
     * 항공기 제원 수정
     *
     * 수정 가능:
     * - 모델명
     * - 총 좌석 수
     *
     * 수정 불가:
     * - 등록번호
     * - 운영 상태
     * - 사용 여부
     */
    @Override
    public String updateAircraft(AirCraftVO aircraft) {

        if (aircraft == null) {
            return "invalid";
        }

        int aircraftId = aircraft.getAircraft_id();

        String modelName =
                aircraft.getModel_name() == null
                ? ""
                : aircraft.getModel_name().trim();

        int totalSeats = aircraft.getTotal_seats();

        if (aircraftId <= 0
                || modelName.isBlank()
                || totalSeats < 1) {

            return "invalid";
        }

        AirCraftVO savedAircraft =
                airCraftMapper.selectAircraft(aircraftId);

        if (savedAircraft == null) {
            return "not-found";
        }

        /*
         * 총 좌석 수를 실제로 변경하는 경우에만
         * 이미 등록된 좌석 수와 일치하는지 확인합니다.
         */
        boolean totalSeatsChanged =
                savedAircraft.getTotal_seats()
                != totalSeats;

        if (totalSeatsChanged) {

            int registeredSeatCount =
                    airCraftMapper
                        .countSeatsByAircraftId(aircraftId);

            /*
             * 좌석이 이미 등록된 항공기는
             * 총 좌석 수를 등록 좌석 수와 다르게
             * 변경할 수 없습니다.
             */
            if (registeredSeatCount > 0
                    && totalSeats != registeredSeatCount) {

                return "seat-count-mismatch:"
                        + registeredSeatCount;
            }
        }

        aircraft.setModel_name(modelName);

        int updatedCount =
                airCraftMapper.updateAircraft(aircraft);

        return updatedCount == 1
                ? "success"
                : "not-found";
    }


    /*
     * 항공기 사용 여부 토글
     */
    @Override
    public String updateAircraftStatus(
            int aircraftId,
            String isActive) {

        if (aircraftId <= 0) {
            return "invalid";
        }

        if (!"Y".equals(isActive)
                && !"N".equals(isActive)) {

            return "invalid";
        }

        AirCraftVO savedAircraft =
                airCraftMapper.selectAircraft(aircraftId);

        if (savedAircraft == null) {
            return "not-found";
        }

        /*
         * 이미 같은 상태라면 추가 UPDATE 없이 성공 처리
         */
        if (isActive.equals(
                savedAircraft.getIs_active())) {

            return "success";
        }

        /*
         * 진행 중이거나 예정된 스케줄이 있으면
         * 미사용으로 전환할 수 없습니다.
         */
        if ("N".equals(isActive)) {

            int scheduleCount =
                    airCraftMapper
                        .countFutureOrRunningFlightsByAircraftId(
                                aircraftId
                        );

            if (scheduleCount > 0) {
                return "in-use";
            }
        }

        int updatedCount =
                airCraftMapper.updateAircraftStatus(
                        aircraftId,
                        isActive
                );

        return updatedCount == 1
                ? "success"
                : "not-found";
    }


    /*
     * 항공기 물리 삭제
     *
     * 실제 운항 기록이나 좌석 데이터가 연결된 항공기는
     * 삭제하지 않고 사용 여부만 N으로 관리합니다.
     */
    @Override
    public String deleteAircraft(int aircraftId) {

        if (aircraftId <= 0) {
            return "invalid";
        }

        AirCraftVO savedAircraft =
                airCraftMapper.selectAircraft(aircraftId);

        if (savedAircraft == null) {
            return "not-found";
        }

        /*
         * 과거 기록을 포함해 FLIGHT가 하나라도 있으면
         * FK와 운항 이력 보존 때문에 삭제할 수 없습니다.
         */
        int flightCount =
                airCraftMapper
                    .countFlightsByAircraftId(aircraftId);

        if (flightCount > 0) {
            return "flight-in-use";
        }

        /*
         * 항공기에 좌석이 등록돼 있으면
         * 좌석을 먼저 정리해야 합니다.
         */
        int seatCount =
                airCraftMapper
                    .countSeatsByAircraftId(aircraftId);

        if (seatCount > 0) {
            return "seat-in-use";
        }

        int deletedCount =
                airCraftMapper.deleteAircraft(aircraftId);

        return deletedCount == 1
                ? "success"
                : "not-found";
    }


    @Override
    public List<AirCraftVO> selectActiveAircraftList() {
        return airCraftMapper.selectActiveAircraftList();
    }


    @Override
    public AirCraftVO selectActiveAircraft(
            int aircraftId) {

        return airCraftMapper
                .selectActiveAircraft(aircraftId);
    }


    @Override
    public List<AirCraftSeatClassVO>
            selectSeatClassCountList(int aircraftId) {

        return airCraftMapper
                .selectSeatClassCountList(aircraftId);
    }
}