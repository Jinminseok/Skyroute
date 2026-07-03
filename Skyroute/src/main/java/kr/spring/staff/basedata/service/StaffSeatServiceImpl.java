package kr.spring.staff.basedata.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    public void generateSeats(SeatClassVO seatClassVO, int startRow) {
        
        // 등급 저장합니다.
        staffSeatMapper.insertSeatClass(seatClassVO);
        
        int currentClassId = seatClassVO.getSeat_class_id();
        
        log.debug("도면 등록 완료! 발급된 등급 ID: {}", currentClassId);

        // 좌석 생성
        int endRow = startRow + seatClassVO.getSeat_rows() - 1;

        // 행 수 만큼 반복
        for (int r = startRow; r <= endRow; r++) {
            
            // 열 수 만큼 반복
            for (int c = 1; c <= seatClassVO.getSeat_columns(); c++) {
                
                // 숫자를 알파벳으로 변환
                char columnChar = (char) ('A' + c - 1); 
                
                // 행 + 열 조합 (예: 1 + A = 1A)
                String seatNo = r + String.valueOf(columnChar); 
                
                // SeatVO 객체 조립
                SeatVO seat = new SeatVO();
                seat.setAircraft_id(seatClassVO.getAircraft_id());
                seat.setSeat_class_id(currentClassId);
                seat.setSeat_no(seatNo);
                
                staffSeatMapper.insertSeat(seat);
            }
        }
        
        log.debug("{}번 줄부터 {}번 줄까지 좌석 자동 생성 완료!", startRow, endRow);
    }
}