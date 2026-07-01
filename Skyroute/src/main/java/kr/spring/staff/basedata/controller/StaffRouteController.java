package kr.spring.staff.basedata.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/staff/basedata/route")
public class StaffRouteController {

	@GetMapping("/list")
	public String list(Model model) {
		model.addAttribute("activeMenu", "base");
		return "thviews/staff_main/basedata/base_list";
	}

	@GetMapping("/write")
	public String write(Model model) {
		model.addAttribute("activeMenu", "base");
		return "thviews/staff_main/basedata/base_list";
	}

	@GetMapping("/modify")
	public String modify(Model model) {
		model.addAttribute("activeMenu", "base");
		return "thviews/staff_main/basedata/base_list";
	}

	@GetMapping("/detail")
	public String detail(Model model) {
		model.addAttribute("activeMenu", "base");
		return "thviews/staff_main/basedata/base_list";
	}
}