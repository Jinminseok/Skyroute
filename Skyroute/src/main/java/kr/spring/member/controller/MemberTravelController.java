package kr.spring.member.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import kr.spring.admin.service.AirCraftService;
import kr.spring.admin.vo.AirCraftVO;
import kr.spring.staff.basedata.service.StaffAirportService;
import kr.spring.staff.basedata.vo.AirportVO;
import kr.spring.staff.basedata.service.StaffSeatService;

@Controller
@RequestMapping("/member/travel")
public class MemberTravelController {

	@Autowired
	private StaffAirportService airportService;

	@GetMapping("/member_airport")
	public String airport(
			@RequestParam(required = false) Integer airportId,
			Model model) {

		List<AirportVO> airportList = airportService.getActiveAirportList();

		model.addAttribute("activeMenu", "travel");
		model.addAttribute("activeTravel", "airport");
		model.addAttribute("airportList", airportList);

		if (airportId != null) {
			AirportVO selectedAirport = airportService.getActiveAirport(airportId);
			model.addAttribute("selectedAirport", selectedAirport);
		}

		return "thviews/member/travel/member_airport";
	}

	@GetMapping("/member_cabin")
	public String cabin(Model model) {
		model.addAttribute("activeMenu", "travel");
		model.addAttribute("activeTravel", "cabin");

		return "thviews/member/travel/member_cabin";
	}

	@GetMapping("/member_aircraft")
	public String aircraft(
			@RequestParam(required = false) Integer aircraftId,
			Model model) {

		List<AirCraftVO> aircraftList =
				airCraftService.selectActiveAircraftList();

		model.addAttribute("activeMenu", "travel");
		model.addAttribute("activeTravel", "aircraft");
		model.addAttribute("aircraftList", aircraftList);
		model.addAttribute("seatList", List.of());

		if (aircraftId != null) {
			AirCraftVO selectedAircraft =
					airCraftService.selectActiveAircraft(aircraftId);

			if (selectedAircraft != null) {
				model.addAttribute(
						"selectedAircraft",
						selectedAircraft
				);

				model.addAttribute(
						"seatClassList",
						airCraftService.selectSeatClassCountList(aircraftId)
				);

				model.addAttribute(
						"seatList",
						staffSeatService.getSeatsByAircraft(aircraftId)
				);
			}
		}

		return "thviews/member/travel/member_aircraft";
	}

	@GetMapping("/member_service")
	public String service(Model model) {
		model.addAttribute("activeMenu", "travel");
		model.addAttribute("activeTravel", "service");

		return "thviews/member/travel/member_service";
	}
	
	@Autowired
	private AirCraftService airCraftService;
	
	
	@Autowired
	private StaffSeatService staffSeatService;
	
	
	
}