package kr.spring.member.controller;

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
import kr.spring.staff.content.vo.EventVO;

@Controller
@RequestMapping("/member/event")
public class MemberEventController {

	@Autowired
	private StaffEventService staffEventService;

	@GetMapping("/detail")
	public String detail(@RequestParam("event_id") long event_id,
						 @AuthenticationPrincipal PrincipalDetails principalDetails,
						 Model model) {
		EventVO event = staffEventService.selectActiveEvent(event_id);

		if (event == null) {
			return "redirect:/main/home";
		}

		boolean participated = false;

		if (principalDetails != null && principalDetails.getMemberVO() != null) {
			participated = staffEventService.isParticipated(
					event_id,
					principalDetails.getMemberVO().getMember_id());
		}

		model.addAttribute("event", event);
		model.addAttribute("participated", participated);

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

			redirectAttributes.addFlashAttribute("message", "이벤트 참여가 완료되었습니다.");
		} catch (IllegalStateException e) {
			redirectAttributes.addFlashAttribute("message", e.getMessage());
		}

		return "redirect:/member/event/detail?event_id=" + event_id;
	}
}