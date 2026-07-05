package kr.spring.staff.content.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import kr.spring.staff.content.service.StaffEventService;

@Controller
@RequestMapping("/staff/content")
public class StaffContentController {

	@Autowired
	private StaffEventService staffEventService;

	private void setContentModel(Model model, String activeTab) {
		model.addAttribute("activeMenu", "content");
		model.addAttribute("activeTab", activeTab);
		model.addAttribute("eventList", staffEventService.selectEventList());
	}

	@GetMapping("/list")
	public String list(Model model) {
		setContentModel(model, "notice");
		return "thviews/staff_main/content/content_list";
	}

	@GetMapping("/notice")
	public String notice(Model model) {
		setContentModel(model, "notice");
		return "thviews/staff_main/content/content_list";
	}

	@GetMapping("/event")
	public String event(Model model) {
		setContentModel(model, "event");
		return "thviews/staff_main/content/content_list";
	}

	@GetMapping("/faq")
	public String faq(Model model) {
		setContentModel(model, "faq");
		return "thviews/staff_main/content/content_list";
	}

	@GetMapping("/detail")
	public String detail(Model model) {
		setContentModel(model, "notice");
		return "thviews/staff_main/content/content_list";
	}
}