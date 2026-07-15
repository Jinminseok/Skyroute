package kr.spring.main.controller;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import kr.spring.member.booking.service.FlightSearchService;
import kr.spring.member.booking.vo.FlightSearchForm;
import kr.spring.member.booking.vo.SeatClassOptionVO;
import kr.spring.member.schedule.service.ScheduleService;
import kr.spring.member.service.MemberService;
import kr.spring.member.vo.PrincipalDetails;
import kr.spring.staff.content.service.StaffEventService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
public class MainController {

	@Autowired
	private MemberService memberService;

	@Autowired
	private FlightSearchService flightSearchService;

	@Autowired
	private StaffEventService staffEventService;
	
	@Autowired
	private ScheduleService scheduleService;

	//사이트 최초 진입
	@GetMapping("/")
	public String init(@AuthenticationPrincipal PrincipalDetails principal) {
		if (principal != null && principal.getMemberVO() != null) {
			String auth = principal.getMemberVO().getRole();

			log.info("====== [MainController] 넘어온 권한 값: [{}] ======", auth);

			if (auth != null && auth.contains("ADMIN")) {
				return "redirect:/admin/base1";
			}

			if (auth != null && auth.contains("STAFF")) {
				return "redirect:/staff/main";
			}
		}
		return "redirect:/main/home";
	}

	//사용자 메인 화면
	@GetMapping("/main/home")
	public String main(Model model) {
		List<SeatClassOptionVO>seatClassList = flightSearchService.selectSeatClassList();

		FlightSearchForm flightSearchForm = new FlightSearchForm();

		/*
		 * 메인 화면 기본 날짜
		 */
		flightSearchForm.setDepartureDate(LocalDate.now().plusDays(1));

		flightSearchForm.setReturnDate(LocalDate.now().plusDays(3));

		/*
		 * 이코노미를 기본 선택한다.
		 * 없으면 첫 번째 좌석 등급 선택
		 */
		if (!seatClassList.isEmpty()) {

			SeatClassOptionVO defaultSeatClass = seatClassList.stream().filter(
					seatClass -> seatClass.getClassName().contains("이코노미")).findFirst().orElse(seatClassList.get(0));

			flightSearchForm.setSeatClassId(defaultSeatClass.getSeatClassId());
		}

		// 오늘의 출/도착 현황
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("searchDate", LocalDate.now().toString()); // 오늘 날짜를 조건 검색어로 바인딩
		
		// 스케줄 조회 서비스를 통해 오늘 날짜 리스트 수거 (최근 5개만)
		List<Map<String, Object>> todayFlightList = scheduleService.selectMainFlightStatusTop5();
		model.addAttribute("todayFlightList", todayFlightList);

		model.addAttribute("eventList", staffEventService.selectUserEventList());
		model.addAttribute("airportList", flightSearchService.selectActiveAirportList());
		model.addAttribute("seatClassList", seatClassList);
		model.addAttribute("flightSearchForm", flightSearchForm);
		model.addAttribute("today", LocalDate.now());
		model.addAttribute("activeMenu", "book");

		return "thviews/main/main";
	}

	@GetMapping("/main/home/Admin_login")
	public String Admin_login(Model model) {
		return "thviews/member/Admin_login";
	}

	@GetMapping("/staff/main")
	public String staffMain() {
		return "redirect:/staff/schedule/today";
	}

	@GetMapping("/accessDenied")
	public String accessDenied(Model model) {
		log.info("🚫 [AccessDenied] 권한 부족! 메인으로 쫓겨납니다.");
		model.addAttribute("message", "접근 권한이 없습니다.");
		return "redirect:/main/home";
	}
}