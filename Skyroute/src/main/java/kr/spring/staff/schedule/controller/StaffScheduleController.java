package kr.spring.staff.schedule.controller;

import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

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
        // 메뉴 활성화
        model.addAttribute("activeMenu", "schedule"); 
        
        model.addAttribute("routeList", routeService.getRouteList());
        model.addAttribute("aircraftList", seatService.getAircraftList());
        model.addAttribute("gateList", gateService.getGateList(new GateVO()));
        List<ScheduleVO> scheduleList = scheduleService.getScheduleList();
        model.addAttribute("scheduleList", scheduleList);
        
        long normalCnt = scheduleList.stream().filter(f -> "SCHEDULED".equals(f.getFlight_status())).count();
        long delayedCnt = scheduleList.stream().filter(f -> "DELAYED".equals(f.getFlight_status())).count();
        long canceledCnt = scheduleList.stream().filter(f -> "CANCELED".equals(f.getFlight_status())).count();
        
        model.addAttribute("totalCnt", scheduleList.size());
        model.addAttribute("normalCnt", normalCnt);
        model.addAttribute("delayedCnt", delayedCnt);
        model.addAttribute("canceledCnt", canceledCnt);
        
        return "thviews/staff_main/schedule/schedule_today"; 
    }
    
    
    @GetMapping("/api/list")
    @ResponseBody
    public List<ScheduleVO> getScheduleData() {
        return scheduleService.getScheduleList();
    }

    @PostMapping("/api/insert")
    @ResponseBody
    public String insertSchedule(@RequestBody ScheduleVO scheduleVO) {
        try {
            scheduleService.insertSchedule(scheduleVO);
            return "success";
        } catch (Exception e) {
            log.error("스케줄 등록 오류", e);
            return "fail";
        }
    }

    @PostMapping("/api/update")
    @ResponseBody
    public String updateSchedule(@RequestBody ScheduleVO scheduleVO) {
        try {
            scheduleService.updateSchedule(scheduleVO);
            return "success";
        } catch (Exception e) {
            log.error("스케줄 수정 오류", e);
            return "fail";
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