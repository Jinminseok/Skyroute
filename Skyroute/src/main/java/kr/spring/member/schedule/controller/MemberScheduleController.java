package kr.spring.member.schedule.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import kr.spring.member.controller.MemberUserController;
import kr.spring.member.schedule.service.ScheduleService;
import kr.spring.member.service.MemberService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequestMapping("/member")
public class MemberScheduleController {

	@Autowired
	private ScheduleService scheduleService;

	@GetMapping("/member_schedule")
	public String scheduleForm(
			@RequestParam(value = "tabType", required = false, defaultValue = "STATUS") String tabType,
			@RequestParam(value = "searchType", required = false, defaultValue = "ROUTE") String searchType,
			@RequestParam(value = "departureId", required = false) Long departureId,
			@RequestParam(value = "arrivalId", required = false) Long arrivalId,
			@RequestParam(value = "flightNo", required = false) String flightNo,
			@RequestParam(value = "schedTripType", required = false, defaultValue = "RT") String schedTripType,
			@RequestParam(value = "startDate", required = false) String startDate, // 통합 시작일
			@RequestParam(value = "endDate", required = false) String endDate,     // 통합 종료일
			Model model) {

		List<Map<String, Object>> airportList = scheduleService.selectAirportList();
		model.addAttribute("airportList", airportList);

		Map<String, Object> paramMap = new HashMap<>();
		paramMap.put("startDate", startDate);
		paramMap.put("endDate", endDate);
		paramMap.put("departureId", departureId);
		paramMap.put("arrivalId", arrivalId);

		// [1] 출/도착 조회 탭 처리
		if ("STATUS".equals(tabType)) {
			paramMap.put("searchType", searchType);
			paramMap.put("flightNo", flightNo);
			
			if (startDate != null && !"".equals(startDate) && endDate != null && !"".equals(endDate)) {
				List<Map<String, Object>> statusResults = scheduleService.selectFlightStatusList(paramMap);
				model.addAttribute("statusResults", statusResults);
			}
		} 
		// [2] 스케줄 조회 탭 처리 (날짜 입력 간소화 반영)
		else if ("SCHEDULE".equals(tabType)) {
			if (departureId != null && arrivalId != null && startDate != null && !"".equals(startDate) && endDate != null && !"".equals(endDate)) {
				
				// A. 가는 편 조회
				List<Map<String, Object>> outboundSchedules = scheduleService.selectFlightScheduleList(paramMap);
				model.addAttribute("outboundSchedules", outboundSchedules);
				
				// B. 왕복(RT) 선택 시, 형님의 아이디어대로 동일한 날짜 범위 주머니를 리버스 턴 해서 재사용 처리!
				if ("RT".equals(schedTripType)) {
					Map<String, Object> inboundMap = new HashMap<>();
					inboundMap.put("departureId", arrivalId);
					inboundMap.put("arrivalId", departureId);
					inboundMap.put("startDate", startDate);
					inboundMap.put("endDate", endDate);
					
					List<Map<String, Object>> inboundSchedules = scheduleService.selectFlightScheduleList(inboundMap);
					model.addAttribute("inboundSchedules", inboundSchedules);
				}
			}
		}

		// 입력값 백전 배달
		model.addAttribute("tabType", tabType);
		model.addAttribute("searchType", searchType);
		model.addAttribute("departureId", departureId);
		model.addAttribute("arrivalId", arrivalId);
		model.addAttribute("flightNo", flightNo);
		model.addAttribute("schedTripType", schedTripType);
		model.addAttribute("startDate", startDate);
		model.addAttribute("endDate", endDate);

		return "thviews/member/member_schedule";
	}
	
	@GetMapping("/member/schedule/search")
	public String searchSchedule(
	        @RequestParam("departure") String departure,
	        @RequestParam("arrival") String arrival,
	        @RequestParam("goingDate") String goingDate,
	        @RequestParam(value = "returnDate", required = false) String returnDate,
	        @RequestParam(value = "isRoundTrip", defaultValue = "true") boolean isRoundTrip,
	        Model model) {

	    // 1. 가는편 조회 (출발지 -> 도착지)
	    List<Map<String, Object>> goingFlights = scheduleService.getScheduleList(departure, arrival, goingDate);
	    model.addAttribute("goingFlights", goingFlights);

	    // 2. 왕복일 경우에만 오는편 조회 (도착지 -> 출발지)
	    if (isRoundTrip && returnDate != null && !returnDate.isEmpty()) {
	        List<Map<String, Object>> returnFlights = scheduleService.getScheduleList(arrival, departure, returnDate);
	        model.addAttribute("returnFlights", returnFlights);
	        model.addAttribute("isRoundTrip", true);
	    } else {
	        model.addAttribute("isRoundTrip", false);
	    }

	    // 검색 조건 유지용 데이터
	    model.addAttribute("searchDep", departure);
	    model.addAttribute("searchArr", arrival);
	    model.addAttribute("goingDate", goingDate);
	    model.addAttribute("returnDate", returnDate);

	    return "thviews/member/member_schedule"; 
	}
}
