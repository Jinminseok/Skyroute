package kr.spring.staff.delay.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/staff/delay")
public class StaffDelayController {

	@GetMapping("/list")
	public String list(Model model) {
		model.addAttribute("activeMenu", "delay");
		return "thviews/staff_main/delay/delay_list";
	}

	@GetMapping("/write")
	public String write(Model model) {
		model.addAttribute("activeMenu", "delay");
		return "thviews/staff_main/delay/delay_list";
	}

	@GetMapping("/cancel-write")
	public String cancelWrite(Model model) {
		model.addAttribute("activeMenu", "delay");
		return "thviews/staff_main/delay/delay_list";
	}

	@GetMapping("/detail")
	public String detail(Model model) {
		model.addAttribute("activeMenu", "delay");
		return "thviews/staff_main/delay/delay_list";
	}
}