package kr.spring.staff.basedata.service;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.spring.admin.vo.AirCraftVO;
import kr.spring.staff.basedata.dao.StaffSeatMapper;
import kr.spring.staff.basedata.vo.SeatClassVO;
import kr.spring.staff.basedata.vo.SeatVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class StaffSeatServiceImpl implements StaffSeatService {

    private final StaffSeatMapper staffSeatMapper;

    @Override
    public void generateSeats(Map<String, Object> payload) {
        
        // 1. 프론트엔드에서 넘어온 데이터 파싱 (Long 대신 int로 통일)
        int aircraftId = Integer.parseInt(payload.get("aircraft_id").toString());
        String className = payload.get("class_name").toString();
        int startRow = Integer.parseInt(payload.get("start_row").toString());
        int endRow = Integer.parseInt(payload.get("end_row").toString());
        int columns = Integer.parseInt(payload.get("seat_columns").toString());
        int totalRows = endRow - startRow + 1;

        // 2. 등급명에 따른 우선순위(sort_order) 자동 할당 로직
        int sortOrder = 3; // 기본값 이코노미
        if ("일등석".equals(className)) {
            sortOrder = 1;
        } else if ("비즈니스".equals(className)) {
            sortOrder = 2;
        }

        // 3. SEAT_CLASS(좌석 등급) 테이블 확인 및 처리
        SeatClassVO seatClass = staffSeatMapper.selectSeatClassByName(className);
        
        if (seatClass == null) {
            // 등록되지 않은 등급이면 새로 생성
            seatClass = new SeatClassVO();
            seatClass.setClass_name(className);
            
            seatClass.setSeat_rows(totalRows); 
            seatClass.setSeat_columns(columns);
            
            seatClass.setSort_order(sortOrder);
            
            staffSeatMapper.insertSeatClass(seatClass);
            log.info("새로운 좌석 등급 생성 완료 - ID: {}, 이름: {}", seatClass.getSeat_class_id(), className);
        }

        // 4. 좌석(SEAT) 일괄 생성 루프
        int classId = seatClass.getSeat_class_id();
        int generatedCount = 0;

        for (int r = startRow; r <= endRow; r++) {
            for (int c = 1; c <= columns; c++) {
                char columnChar = (char) ('A' + c - 1); 
                String seatNo = r + String.valueOf(columnChar); 
                
                SeatVO seat = new SeatVO();
                seat.setAircraft_id(aircraftId);
                seat.setSeat_class_id(classId);
                seat.setSeat_no(seatNo);
                
                staffSeatMapper.insertSeat(seat);
                generatedCount++;
            }
        }
        
        log.info("항공기 ID [{}]에 [{}] 등급 좌석 총 {}개 자동 생성 완료!", aircraftId, className, generatedCount);
    }

    @Override
    public List<AirCraftVO> getAircraftList() {
        return staffSeatMapper.selectAircraftList();
    }

    @Override
    public void updateAircraftActive(Map<String, Object> payload) {
        staffSeatMapper.updateAircraftActive(payload);
    }

	@Override
	public void updateAircraftStatus(Map<String, Object> payload) {
		staffSeatMapper.updateAircraftStatus(payload);
	}

	@Override
	public List<Map<String, Object>> getSeatSummaryList() {
		return staffSeatMapper.selectSeatSummaryList();
	}

	@Override
	public List<SeatVO> getSeatsByAircraft(int aircraftId) {
		return staffSeatMapper.selectSeatsByAircraft(aircraftId);
	}

    
}