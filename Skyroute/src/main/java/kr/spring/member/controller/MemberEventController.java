package kr.spring.member.controller;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

	@GetMapping("/ended")
	public String endedEventList(Model model) {
		List<EventVO> eventList = staffEventService.selectEndedEventList();

		model.addAttribute("eventList", eventList);
		model.addAttribute("activeMenu", "event");

		return "thviews/member/event_ended";
	}
	
	@GetMapping("/winner")
	public String winnerEventList(
			@RequestParam(value = "pageNum", defaultValue = "1") int pageNum,
			@RequestParam(value = "keyword", defaultValue = "") String keyword,
			Model model) {

		final int rowCount = 10;
		final int pageBlock = 5;

		keyword = keyword.trim();

		Map<String, Object> map = new HashMap<>();
		map.put("keyword", keyword);

		int winnerCount =
				staffEventService.selectWinnerEventRowCount(map);

		int winnerPageCount =
				(int) Math.ceil((double) winnerCount / rowCount);

		if (pageNum < 1) {
			pageNum = 1;
		}

		if (winnerPageCount > 0 && pageNum > winnerPageCount) {
			pageNum = winnerPageCount;
		}

		int skip = (pageNum - 1) * rowCount;
		int endRow = pageNum * rowCount;

		map.put("skip", skip);
		map.put("endRow", endRow);

		List<EventVO> eventList =
				staffEventService.selectWinnerEventList(map);

		int winnerStartPage =
				((pageNum - 1) / pageBlock) * pageBlock + 1;

		int winnerEndPage =
				Math.min(
						winnerStartPage + pageBlock - 1,
						winnerPageCount);

		model.addAttribute("eventList", eventList);
		model.addAttribute("winnerCount", winnerCount);
		model.addAttribute("winnerKeyword", keyword);
		model.addAttribute("winnerPageNum", pageNum);
		model.addAttribute("winnerPageCount", winnerPageCount);
		model.addAttribute("winnerStartPage", winnerStartPage);
		model.addAttribute("winnerEndPage", winnerEndPage);
		model.addAttribute("activeMenu", "event");

		return "thviews/member/event_winner";
	}

	@GetMapping("/winner/detail")
	public String winnerDetail(@RequestParam("event_id") long event_id,
							   Model model) {

		EventVO event = staffEventService.selectEvent(event_id);

		if (event == null || !"ANNOUNCED".equals(event.getResult_status())) {
			return "redirect:/member/event/winner";
		}

		List<EventParticipationVO> winnerList =
				staffEventService.selectWinnerList(event_id);

		model.addAttribute("event", event);
		model.addAttribute("winnerList", winnerList);
		model.addAttribute("activeMenu", "event");

		return "thviews/member/event_winner_detail";
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
				"Y".equals(event.getIs_visible())
				&& "N".equals(event.getIs_ended())
				&& !"ANNOUNCED".equals(event.getResult_status())
				&& startDate != null
				&& today.isBefore(startDate);

		boolean endedPublicEvent =
		        "ANNOUNCED".equals(event.getResult_status())
		        || (
		            "Y".equals(event.getIs_visible())
		            && (
		                "Y".equals(event.getIs_ended())
		                || (
		                    endDate != null
		                    && today.isAfter(endDate)
		                )
		            )
		        );

		if (!activeEvent
				&& !scheduledEvent
				&& !endedPublicEvent
				&& !participated) {
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