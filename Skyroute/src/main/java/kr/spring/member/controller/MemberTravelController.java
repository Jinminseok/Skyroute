package kr.spring.member.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/member/travel")
public class MemberTravelController {

	@GetMapping("/member_airport")
	public String airport(Model model) {
		model.addAttribute("activeMenu", "travel");
		model.addAttribute("activeTravel", "airport");

		return "thviews/member/travel/member_airport";
	}

	@GetMapping("/member_cabin")
	public String cabin(Model model) {
		model.addAttribute("activeMenu", "travel");
		model.addAttribute("activeTravel", "cabin");

		return "thviews/member/travel/member_cabin";
	}

	@GetMapping("/member_aircraft")
	public String aircraft(Model model) {
		model.addAttribute("activeMenu", "travel");
		model.addAttribute("activeTravel", "aircraft");

		return "thviews/member/travel/member_aircraft";
	}

	@GetMapping("/member_service")
	public String service(Model model) {
		model.addAttribute("activeMenu", "travel");
		model.addAttribute("activeTravel", "service");

		return "thviews/member/travel/member_service";
	}
}