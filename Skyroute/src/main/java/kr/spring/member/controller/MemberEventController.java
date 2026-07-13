package kr.spring.member.controller;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import kr.spring.member.vo.PrincipalDetails;
import kr.spring.staff.content.service.StaffEventService;
import kr.spring.staff.content.vo.EventParticipationVO;
import kr.spring.staff.content.vo.EventVO;

@Controller
@RequestMapping("/member/event")
public class MemberEventController {

	@Autowired
	private StaffEventService staffEventService;

	@GetMapping("/list")
	public String eventList(Model model) {
		List<EventVO> eventList = staffEventService.selectUserEventList();

		model.addAttribute("eventList", eventList);
		model.addAttribute("activeMenu", "event");

		return "thviews/member/event_list";
	}

	@GetMapping("/detail")
	public String detail(@RequestParam("event_id") long event_id,
						 @AuthenticationPrincipal PrincipalDetails principalDetails,
						 Model model) {

		EventVO event = staffEventService.selectEvent(event_id);

		if (event == null) {
			return "redirect:/main/home";
		}

		EventParticipationVO participation = null;
		boolean participated = false;

		if (principalDetails != null && principalDetails.getMemberVO() != null) {
			participation = staffEventService.selectMyEventParticipation(
					event_id,
					principalDetails.getMemberVO().getMember_id());

			participated = participation != null;
		}

		LocalDate today = LocalDate.now();
		LocalDate startDate = toLocalDate(event.getStart_date());
		LocalDate endDate = toLocalDate(event.getEnd_date());

		boolean activeEvent =
				staffEventService.selectActiveEvent(event_id) != null;

		boolean scheduledEvent =
				startDate != null && today.isBefore(startDate);

		boolean userVisibleEvent =
				"Y".equals(event.getIs_visible())
				&& "N".equals(event.getIs_ended())
				&& !"ANNOUNCED".equals(event.getResult_status())
				&& endDate != null
				&& !today.isAfter(endDate);

		if (!userVisibleEvent && !participated) {
			return "redirect:/main/home";
		}

		model.addAttribute("event", event);
		model.addAttribute("participated", participated);
		model.addAttribute("activeEvent", activeEvent);
		model.addAttribute("scheduledEvent", scheduledEvent);
		model.addAttribute("participation", participation);
		model.addAttribute("activeMenu", "event");

		return "thviews/member/event_detail";
	}

	@PostMapping("/participate")
	public String participate(@RequestParam("event_id") long event_id,
							  @AuthenticationPrincipal PrincipalDetails principalDetails,
							  RedirectAttributes redirectAttributes) {

		if (principalDetails == null || principalDetails.getMemberVO() == null) {
			return "redirect:/member/login";
		}

		try {
			staffEventService.participateEvent(
					event_id,
					principalDetails.getMemberVO().getMember_id());

			redirectAttributes.addFlashAttribute(
					"message",
					"이벤트 참여가 완료되었습니다.");
		} catch (IllegalStateException e) {
			redirectAttributes.addFlashAttribute(
					"message",
					e.getMessage());
		}

		return "redirect:/member/event/detail?event_id=" + event_id;
	}

	private LocalDate toLocalDate(java.util.Date date) {
		if (date == null) {
			return null;
		}

		return date.toInstant()
				.atZone(ZoneId.systemDefault())
				.toLocalDate();
	}
}