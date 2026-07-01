package kr.spring.staff.inquiry.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/staff/inquiry")
public class StaffInquiryController {

	@GetMapping("/list")
	public String list(Model model) {
		model.addAttribute("activeMenu", "inquiry");
		return "thviews/staff_main/inquiry/inquiry_list";
	}

	@GetMapping("/detail")
	public String detail(Model model) {
		model.addAttribute("activeMenu", "inquiry");
		return "thviews/staff_main/inquiry/inquiry_list";
	}
}