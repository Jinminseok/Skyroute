package kr.spring.staff.operation.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/staff/operation/noshow")
public class StaffNoShowController {

	@GetMapping("/list")
	public String list(Model model) {
		model.addAttribute("activeMenu", "operation");
		return "thviews/staff_main/operation/passenger/passenger_list";
	}
}