package kr.spring.staff.schedule.controller;

import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import kr.spring.staff.schedule.service.StaffScheduleService;
import kr.spring.staff.schedule.vo.ScheduleVO;

import kr.spring.staff.basedata.service.StaffRouteService;
import kr.spring.staff.basedata.service.StaffSeatService;
import kr.spring.staff.basedata.service.StaffGateService;
import kr.spring.staff.basedata.vo.GateVO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/staff/schedule")
public class StaffScheduleController {

    private final StaffScheduleService scheduleService;
    private final StaffRouteService routeService;
    private final StaffSeatService seatService;
    private final StaffGateService gateService;

    // 운항 스케쥴 화면 띄우기
    @GetMapping("/list")
    public String scheduleList(Model model) {
        model.addAttribute("activeMenu", "schedule");
        
        // 모달창 콤보박스용 기초 데이터
        model.addAttribute("routeList", routeService.getRouteList());
        model.addAttribute("aircraftList", seatService.getAircraftList());
        model.addAttribute("gateList", gateService.getGateList(new GateVO()));
        model.addAttribute("scheduleList", scheduleService.getScheduleList());
        
        return "thviews/staff_main/schedule/schedule_list"; 
    }

    // 오늘 항공편 화면 띄우기
    @GetMapping("/today")
    public String scheduleToday(Model model) {
        model.addAttribute("activeMenu", "schedule"); 
        
        model.addAttribute("routeList", routeService.getRouteList());
        model.addAttribute("aircraftList", seatService.getAircraftList());
        model.addAttribute("gateList", gateService.getGateList(new GateVO()));
        
        // 서버의 오늘 날짜 구하기
        String todayStr = java.time.LocalDate.now().toString();
        
        // 출발 시각이 오늘인 것만 필터링
        List<ScheduleVO> allList = scheduleService.getScheduleList();
        List<ScheduleVO> todayList = allList.stream()
            .filter(f -> f.getDeparture_time() != null && f.getDeparture_time().startsWith(todayStr))
            .collect(java.util.stream.Collectors.toList());
        
        // 필터링된 오늘 리스트만 화면으로 넘김
        model.addAttribute("scheduleList", todayList);
        
        // 통계수치 계산
        long totalCnt = todayList.size(); // 1. 오늘 전체 항공편
        
        long readyCnt = todayList.stream().filter(f -> 
            "SCHEDULED".equals(f.getFlight_status()) || "BOARDING".equals(f.getFlight_status())
        ).count(); // 2. 출발 대기 및 탑승중
        
        long doneCnt = todayList.stream().filter(f -> 
            "DEPARTED".equals(f.getFlight_status()) || "ARRIVED".equals(f.getFlight_status()) || "COMPLETED".equals(f.getFlight_status())
        ).count(); // 3. 운항 중 및 도착 완료
        
        long issueCnt = todayList.stream().filter(f -> 
            "DELAYED".equals(f.getFlight_status()) || "CANCELED".equals(f.getFlight_status()) || "CANCELLED".equals(f.getFlight_status())
        ).count(); // 4. 비정상 (지연/결항)
        
        model.addAttribute("totalCnt", totalCnt);
        model.addAttribute("readyCnt", readyCnt);
        model.addAttribute("doneCnt", doneCnt);
        model.addAttribute("issueCnt", issueCnt);
        
        return "thviews/staff_main/schedule/schedule_today";
    }
    
    
    @GetMapping("/api/list")
    @ResponseBody
    public List<ScheduleVO> getScheduleData() {
        return scheduleService.getScheduleList();
    }

    @PostMapping("/api/insert")
    @ResponseBody
    public ResponseEntity<String> insertSchedule(
            @RequestBody ScheduleVO scheduleVO
    ) {

        try {
            scheduleService.insertSchedule(scheduleVO);

            return ResponseEntity.ok("success");

        } catch (IllegalStateException e) {

            log.warn(
                    "스케줄 등록 검증 실패: {}",
                    e.getMessage()
            );

            return ResponseEntity
                    .badRequest()
                    .body(e.getMessage());

        } catch (Exception e) {

            log.error("스케줄 등록 오류", e);

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(
                        "시스템 오류가 발생했습니다. "
                        + "관리자에게 문의하세요."
                    );
        }
    }

    @PostMapping("/api/update")
    @ResponseBody
    public ResponseEntity<String> updateSchedule(
            @RequestBody ScheduleVO scheduleVO
    ) {

        try {
            scheduleService.updateSchedule(scheduleVO);

            return ResponseEntity.ok("success");

        } catch (IllegalStateException e) {

            log.warn(
                    "스케줄 수정 검증 실패: {}",
                    e.getMessage()
            );

            return ResponseEntity
                    .badRequest()
                    .body(e.getMessage());

        } catch (Exception e) {

            log.error("스케줄 수정 오류", e);

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(
                        "시스템 오류가 발생했습니다. "
                        + "관리자에게 문의하세요."
                    );
        }
    }

    @PostMapping("/api/delete")
    @ResponseBody
    public String deleteSchedule(@RequestParam("flightId") int flightId) {
        try {
            scheduleService.deleteSchedule(flightId);
            return "success";
        } catch (Exception e) {
            log.error("스케줄 삭제 오류", e);
            return "fail";
        }
    }
    
    @PostMapping("/api/status")
    @ResponseBody
    public String updateFlightStatus(@RequestBody ScheduleVO scheduleVO) {
        try {
            scheduleService.updateFlightStatus(scheduleVO);
            return "success";
        } catch (Exception e) {
            log.error("상태 변경 오류", e);
            return "fail";
        }
    }
}