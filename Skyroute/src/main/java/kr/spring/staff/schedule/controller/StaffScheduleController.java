package kr.spring.staff.schedule.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/staff/schedule")
public class StaffScheduleController {

	@GetMapping("/today")
	public String today(Model model) {
		model.addAttribute("activeMenu", "today");
		return "thviews/staff_main/schedule/schedule_today";
	}

	@GetMapping("/list")
	public String list(Model model) {
		model.addAttribute("activeMenu", "schedule");
		return "thviews/staff_main/schedule/schedule_list";
	}

	@GetMapping("/write")
	public String write(Model model) {
		model.addAttribute("activeMenu", "schedule");
		return "thviews/staff_main/schedule/schedule_list";
	}

	@GetMapping("/modify")
	public String modify(Model model) {
		model.addAttribute("activeMenu", "schedule");
		return "thviews/staff_main/schedule/schedule_list";
	}

	@GetMapping("/detail")
	public String detail(Model model) {
		model.addAttribute("activeMenu", "schedule");
		return "thviews/staff_main/schedule/schedule_list";
	}
}