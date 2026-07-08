package kr.spring.staff.content.controller;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import kr.spring.staff.content.service.StaffEventService;
import kr.spring.staff.content.vo.EventVO;

@Controller
@RequestMapping("/staff/content")
public class StaffContentController {

	@Autowired
	private StaffEventService staffEventService;

	private void setContentModel(Model model, String activeTab) {
		List<EventVO> eventList = staffEventService.selectEventList();
		LocalDate today = LocalDate.now();

		for (EventVO event : eventList) {
			setEventDisplayStatus(event, today);
		}

		model.addAttribute("activeMenu", "content");
		model.addAttribute("activeTab", activeTab);
		model.addAttribute("eventList", eventList);
	}

	private void setEventDisplayStatus(EventVO event, LocalDate today) {
		LocalDate startDate = toLocalDate(event.getStart_date());
		LocalDate endDate = toLocalDate(event.getEnd_date());

		boolean started = startDate == null || !today.isBefore(startDate);
		boolean periodEnded = endDate != null && today.isAfter(endDate);
		boolean forcedEnded = "Y".equals(event.getIs_ended());
		boolean announced = "ANNOUNCED".equals(event.getResult_status());

		boolean actualVisible = "Y".equals(event.getIs_visible())
				&& started
				&& !periodEnded
				&& !forcedEnded
				&& !announced;

		event.setDisplay_visibility(actualVisible ? "노출" : "비노출");

		if (forcedEnded || periodEnded || announced) {
			event.setProgress_status("종료됨");
		} else if (!started) {
			event.setProgress_status("진행 예정");
		} else {
			event.setProgress_status("진행 중");
		}

		boolean drawRequired = false;

		if (announced) {
			event.setEvent_status("결과 발표 완료");
		} else if (forcedEnded) {
			if (event.getParticipation_count() > 0) {
				event.setEvent_status("강제 종료 · 추첨 필요");
				drawRequired = true;
			} else {
				event.setEvent_status("강제 종료 · 응모자 없음");
			}
		} else if (periodEnded) {
			if (event.getParticipation_count() > 0) {
				event.setEvent_status("응모 마감 · 추첨 필요");
				drawRequired = true;
			} else {
				event.setEvent_status("응모 마감 · 응모자 없음");
			}
		} else if (!started) {
			event.setEvent_status("응모 예정");
		} else {
			event.setEvent_status("응모 진행 중");
		}

		event.setDrawRequired(drawRequired);
	}

	private LocalDate toLocalDate(java.util.Date date) {
		if (date == null) {
			return null;
		}

		return date.toInstant()
				.atZone(ZoneId.systemDefault())
				.toLocalDate();
	}

	@GetMapping({"", "/list", "/notice"})
	public String list() {
		return "redirect:/staff/content/notice/list ";
	}

	@GetMapping("/event")
	public String event() {
		return "redirect:/staff/content/notice/list?tab=event";
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
	
	@GetMapping("/event/fragment")
	public String eventFragment(Model model) {
		setContentModel(model, "event");
		return "thviews/staff_main/content/content_list :: eventList";
	}
}