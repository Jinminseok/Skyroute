package kr.spring.member.booking.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.validation.Valid;
import kr.spring.member.booking.service.FlightSearchService;
import kr.spring.member.booking.vo.AirportOptionVO;
import kr.spring.member.booking.vo.FlightSearchForm;
import kr.spring.member.booking.vo.FlightSearchResultVO;
import kr.spring.member.booking.vo.SeatClassOptionVO;
import kr.spring.staff.content.service.StaffEventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/booking/flights")
public class FlightSearchController {

	private final FlightSearchService flightSearchService;

	private final StaffEventService staffEventService;


	@GetMapping("/search")
	public String search(@Valid @ModelAttribute("flightSearchForm") FlightSearchForm flightSearchForm, BindingResult bindingResult, Model model) {

		/*
		 * Bean Validation 통과 후
		 * DB 기준 데이터 유효성 검사
		 */
		if (!bindingResult.hasErrors()) {
			try {
				flightSearchService.validateReferenceData(flightSearchForm);
			} catch (IllegalArgumentException e) {
				bindingResult.reject("invalidReferenceData", e.getMessage());
			}
		}


		/*
		 * 검색 조건 오류이면 입력값을 유지한 채
		 * 메인 화면으로 돌아간다.
		 */
		if (bindingResult.hasErrors()) {
			log.debug("<<항공권 검색 조건 오류>> : {}", bindingResult.getAllErrors());
			addMainPageData(model);
			return "thviews/main/main";
		}

		List<AirportOptionVO>airportList = flightSearchService.selectActiveAirportList();
		List<SeatClassOptionVO>seatClassList = flightSearchService.selectSeatClassList();
		List<FlightSearchResultVO>outboundFlightList = flightSearchService.searchOutboundFlightList(flightSearchForm);
		List<FlightSearchResultVO>inboundFlightList = flightSearchService.searchInboundFlightList(flightSearchForm);

		model.addAttribute("airportList", airportList);
		model.addAttribute("seatClassList", seatClassList);
		model.addAttribute("departureAirport", findAirport(airportList, flightSearchForm.getDepartureAirportId()));
		model.addAttribute("arrivalAirport", findAirport(airportList, flightSearchForm.getArrivalAirportId()));
		model.addAttribute("selectedSeatClass", findSeatClass(seatClassList, flightSearchForm.getSeatClassId()));
		model.addAttribute("outboundFlightList", outboundFlightList);
		model.addAttribute("inboundFlightList", inboundFlightList);
		model.addAttribute("passengerCount", flightSearchForm.getPassengerCount());
		model.addAttribute("activeMenu", "book");
	
		return "thviews/member/member_flight_list";
	}


	private void addMainPageData(Model model) {
		
		model.addAttribute("eventList", staffEventService.selectActiveEventList());
		model.addAttribute("airportList", flightSearchService.selectActiveAirportList());
		model.addAttribute("seatClassList", flightSearchService.selectSeatClassList());
		model.addAttribute("today", LocalDate.now());
		model.addAttribute("activeMenu", "book");
	}


	private AirportOptionVO findAirport(List<AirportOptionVO> airportList, Long airportId) {

		return airportList.stream().filter(airport -> airport.getAirportId().equals(airportId)).findFirst().orElseThrow(() -> new IllegalStateException("공항 정보를 찾을 수 없습니다."));
	}


	private SeatClassOptionVO findSeatClass(List<SeatClassOptionVO>seatClassList, Long seatClassId) {

		return seatClassList.stream().filter(seatClass -> seatClass.getSeatClassId().equals(seatClassId)).findFirst().orElseThrow(() -> new IllegalStateException("좌석 등급 정보를 찾을 수 없습니다."));
	}
}