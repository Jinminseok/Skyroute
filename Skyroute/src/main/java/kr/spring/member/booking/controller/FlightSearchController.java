package kr.spring.member.booking.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import kr.spring.member.booking.service.FlightSearchService;
import kr.spring.member.booking.vo.AirportOptionVO;
import kr.spring.member.booking.vo.FlightDetailVO;
import kr.spring.member.booking.vo.FlightSearchForm;
import kr.spring.member.booking.vo.FlightSearchResultVO;
import kr.spring.member.booking.vo.SeatClassOptionVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/booking/flights")
public class FlightSearchController {

	private final FlightSearchService flightSearchService;


	@GetMapping("/search")
	public String search(
			@Valid
			@ModelAttribute("flightSearchForm")
			FlightSearchForm flightSearchForm,
			BindingResult bindingResult,
			Model model) {

		/*
		 * 검색 결과 화면의 조건 수정 폼에서 사용할
		 * 공항 및 좌석 등급 목록을 항상 전달한다.
		 */
		List<AirportOptionVO> airportList =
				flightSearchService.selectActiveAirportList();

		List<SeatClassOptionVO> seatClassList =
				flightSearchService.selectSeatClassList();

		addSearchPageCommonData(
				model,
				airportList,
				seatClassList
		);


		/*
		 * Bean Validation 통과 후 실제 DB 기준값을 검증한다.
		 */
		if (!bindingResult.hasErrors()) {
			try {
				flightSearchService.validateReferenceData(
						flightSearchForm
				);
			} catch (IllegalArgumentException e) {
				bindingResult.reject(
						"invalidReferenceData",
						e.getMessage()
				);
			}
		}


		/*
		 * 검색 조건 오류가 발생해도 메인 화면으로 이동하지 않고,
		 * 검색 결과 화면에서 조건 수정 폼을 열린 상태로 보여 준다.
		 */
		if (bindingResult.hasErrors()) {
			log.debug(
					"<<항공권 검색 조건 오류>> : {}",
					bindingResult.getAllErrors()
			);

			model.addAttribute(
					"outboundFlightList",
					List.of()
			);

			model.addAttribute(
					"inboundFlightList",
					List.of()
			);

			model.addAttribute(
					"passengerCount",
					flightSearchForm.getPassengerCount()
			);

			model.addAttribute("searchReady", false);
			model.addAttribute("searchFormOpen", true);

			return "thviews/member/member_flight_list";
		}


		List<FlightSearchResultVO> outboundFlightList =
				flightSearchService.searchOutboundFlightList(
						flightSearchForm
				);

		List<FlightSearchResultVO> inboundFlightList =
				flightSearchService.searchInboundFlightList(
						flightSearchForm
				);


		model.addAttribute(
				"departureAirport",
				findAirport(
						airportList,
						flightSearchForm.getDepartureAirportId()
				)
		);

		model.addAttribute(
				"arrivalAirport",
				findAirport(
						airportList,
						flightSearchForm.getArrivalAirportId()
				)
		);

		model.addAttribute(
				"selectedSeatClass",
				findSeatClass(
						seatClassList,
						flightSearchForm.getSeatClassId()
				)
		);

		model.addAttribute(
				"outboundFlightList",
				outboundFlightList
		);

		model.addAttribute(
				"inboundFlightList",
				inboundFlightList
		);

		model.addAttribute(
				"passengerCount",
				flightSearchForm.getPassengerCount()
		);

		model.addAttribute("searchReady", true);
		model.addAttribute("searchFormOpen", false);

		return "thviews/member/member_flight_list";
	}


	/*
	 * 앞 단계에서 구현한 항공편 상세 조회 기능을 유지한다.
	 */
	@GetMapping("/detail")
	public String detail(
			@RequestParam(
					name = "flightId",
					required = false
			)
			String flightIdValue,
			Model model,
			HttpServletRequest request) {

		Long flightId = parseFlightId(flightIdValue);

		if (flightId == null) {
			return showFlightNotFound(model, request);
		}

		FlightDetailVO flight =
				flightSearchService.selectFlightDetail(flightId);

		if (flight == null) {
			return showFlightNotFound(model, request);
		}

		model.addAttribute("flight", flight);
		model.addAttribute("activeMenu", "book");

		return "thviews/member/member_flight_detail";
	}


	private void addSearchPageCommonData(
			Model model,
			List<AirportOptionVO> airportList,
			List<SeatClassOptionVO> seatClassList) {

		model.addAttribute("airportList", airportList);
		model.addAttribute("seatClassList", seatClassList);
		model.addAttribute("today", LocalDate.now());
		model.addAttribute("activeMenu", "book");
	}


	private AirportOptionVO findAirport(
			List<AirportOptionVO> airportList,
			Long airportId) {

		return airportList.stream()
				.filter(airport -> Objects.equals(
						airport.getAirportId(),
						airportId
				))
				.findFirst()
				.orElseThrow(() ->
						new IllegalStateException(
								"공항 정보를 찾을 수 없습니다."
						)
				);
	}


	private SeatClassOptionVO findSeatClass(
			List<SeatClassOptionVO> seatClassList,
			Long seatClassId) {

		return seatClassList.stream()
				.filter(seatClass -> Objects.equals(
						seatClass.getSeatClassId(),
						seatClassId
				))
				.findFirst()
				.orElseThrow(() ->
						new IllegalStateException(
								"좌석 등급 정보를 찾을 수 없습니다."
						)
				);
	}


	private Long parseFlightId(String flightIdValue) {

		if (flightIdValue == null
				|| flightIdValue.isBlank()) {

			return null;
		}

		try {
			long flightId =
					Long.parseLong(flightIdValue.trim());

			return flightId > 0
					? flightId
					: null;

		} catch (NumberFormatException e) {
			return null;
		}
	}


	private String showFlightNotFound(
			Model model,
			HttpServletRequest request) {

		model.addAttribute(
				"accessTitle",
				"항공편 상세 조회"
		);

		model.addAttribute(
				"accessMsg",
				"조회 결과가 없습니다."
		);

		model.addAttribute(
				"accessBtn",
				"메인으로"
		);

		model.addAttribute(
				"accessUrl",
				request.getContextPath() + "/main/home"
		);

		return "thviews/common/resultView";
	}
}