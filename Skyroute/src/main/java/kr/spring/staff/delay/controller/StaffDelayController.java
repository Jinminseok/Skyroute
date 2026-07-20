package kr.spring.staff.delay.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import kr.spring.member.vo.PrincipalDetails;
import kr.spring.staff.delay.service.StaffDelayService;
import kr.spring.staff.delay.vo.FlightNoticeVO;
import kr.spring.staff.schedule.service.StaffScheduleService;
import kr.spring.staff.schedule.vo.ScheduleVO;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequestMapping("/staff/delay")
public class StaffDelayController {
	
	@Autowired
    private StaffDelayService delayService;
	@Autowired
    private StaffScheduleService scheduleService; // 항공편 목록을 가져오기 위함

    // 1. 지연/결항 관리 메인 화면 띄우기
    @GetMapping("/list")
    public String delayList(Model model) {
        // 활성화될 메뉴 지정
        model.addAttribute("activeMenu", "delay");
        
        // 항공편 전체 목록(상태가 CANCELLED인 항공편 제외)
        List<ScheduleVO> flightList = scheduleService.getScheduleList().stream()
            .filter(f -> !"CANCELLED".equals(f.getFlight_status()) && !"CANCELED".equals(f.getFlight_status()))
            .collect(java.util.stream.Collectors.toList());
        model.addAttribute("flightList", flightList);
        
        // 안내 등록 이력 데이터 전달
        List<FlightNoticeVO> noticeList = delayService.getFlightNoticeList();
        model.addAttribute("noticeList", noticeList);
        
        return "thviews/staff_main/delay/delay_list";
    }

    // 2. 새로운 지연/결항 이력 등록
    @PostMapping("/api/insert")
    @ResponseBody
    public String insertNotice(@RequestBody FlightNoticeVO noticeVO, 
            @AuthenticationPrincipal PrincipalDetails principalDetails) {
    	try {
    		// 로그인한 사용자의 신분증에서 사번 가져오기
            int staffId = (int) principalDetails.getMemberVO().getMember_id();
            // 가져온 사번을 공지사항 작성자 ID로 세팅
            noticeVO.setCreated_by(staffId);
            
            // 서비스 호출 (이력 저장 + 항공편 상태 변경)
            delayService.insertFlightNotice(noticeVO);
            return "success";
        } catch (Exception e) {
            log.error("지연/결항 등록 오류: ", e);
            return "fail";
        }
    }
}