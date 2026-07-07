package kr.spring.staff.basedata.controller;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import kr.spring.admin.vo.AirCraftVO;
import kr.spring.staff.basedata.service.StaffSeatService;
import kr.spring.staff.basedata.vo.SeatVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/staff/basedata/seat")
public class StaffSeatController {
	
	private final StaffSeatService staffSeatService;

	// 좌석 자동 생성
    @PostMapping("/generate")
    @ResponseBody
    public String generateSeats(@RequestBody Map<String, Object> payload) {
        log.info("좌석 생성 요청 데이터: {}", payload);
        try {
            // 서비스로 데이터 전달하여 좌석 생성 로직 실행
            staffSeatService.generateSeats(payload);
            return "success";
        } catch (Exception e) {
            log.error("좌석 생성 중 오류 발생: ", e);
            return "error";
        }
    }
    
    // 항공기 좌석 데이터
    @GetMapping("/map/{aircraftId}")
    @ResponseBody
    public List<SeatVO> getSeatMap(@PathVariable("aircraftId") int aircraftId) {
        return staffSeatService.getSeatsByAircraft(aircraftId);
    }
    
    // 항공기 사용 여부
    @PostMapping("/aircraft/toggle")
    @ResponseBody
    public String toggleAircraftActive(@RequestBody Map<String, Object> payload) {
        try {
            staffSeatService.updateAircraftActive(payload);
            return "success";
        } catch (Exception e) {
            log.error("항공기 상태 변경 중 오류 발생: ", e);
            return "error";
        }
    }
    
    // 항공기 상태(운항가능, 정비중 등) 변경
    @PostMapping("/aircraft/status")
    @ResponseBody
    public String updateAircraftStatus(@RequestBody Map<String, Object> payload) {
        try {
            staffSeatService.updateAircraftStatus(payload);
            return "success";
        } catch (Exception e) {
            log.error("항공기 상태 업데이트 중 오류 발생: ", e);
            return "error";
        }
    }
}