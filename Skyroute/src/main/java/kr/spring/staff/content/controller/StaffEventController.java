package kr.spring.staff.content.controller;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpServletRequest;
import kr.spring.member.vo.PrincipalDetails;
import kr.spring.staff.content.service.StaffEventService;
import kr.spring.staff.content.vo.EventVO;
import kr.spring.util.FileUtil;

@Controller
@RequestMapping("/staff/content/event")
public class StaffEventController {

	@Autowired
	private StaffEventService staffEventService;

	@GetMapping("/write")
	public String writeForm(Model model) {
		EventVO event = new EventVO();
		event.setIs_visible("Y");
		event.setDisplay_order(0);

		model.addAttribute("activeMenu", "content");
		model.addAttribute("event", event);

		return "thviews/staff_main/content/event/event_write";
	}

	@PostMapping("/write")
	public String write(EventVO event,
						@AuthenticationPrincipal PrincipalDetails principalDetails,
						HttpServletRequest request,
						Model model) throws IOException {

		if (principalDetails == null || principalDetails.getMemberVO() == null) {
			return "redirect:/member/login";
		}

		String error = validateEvent(event, true);

		if (error != null) {
			return returnWriteForm(model, event, error);
		}

		String filename = FileUtil.createFile(request, event.getUpload());

		event.setImage_url(filename);
		event.setCreated_by(principalDetails.getMemberVO().getMember_id());
		event.setIs_ended("N");

		if (isBlank(event.getIs_visible())) {
			event.setIs_visible("Y");
		}

		staffEventService.insertEvent(event);

		return "redirect:/staff/content/event";
	}

	@GetMapping("/detail")
	public String detail(@RequestParam("event_id") long event_id, Model model) {
		EventVO event = staffEventService.selectEvent(event_id);

		if (event == null) {
			return "redirect:/staff/content/event";
		}

		model.addAttribute("activeMenu", "content");
		model.addAttribute("event", event);
		model.addAttribute("participationList",
				staffEventService.selectParticipationList(event_id));

		return "thviews/staff_main/content/event/event_detail";
	}

	@GetMapping("/update")
	public String updateForm(@RequestParam("event_id") long event_id, Model model) {
		EventVO event = staffEventService.selectEvent(event_id);

		if (event == null) {
			return "redirect:/staff/content/event";
		}

		model.addAttribute("activeMenu", "content");
		model.addAttribute("event", event);

		return "thviews/staff_main/content/event/event_update";
	}

	@PostMapping("/update")
	public String update(EventVO event,
						 HttpServletRequest request,
						 Model model) throws IOException {

		EventVO originEvent = staffEventService.selectEvent(event.getEvent_id());

		if (originEvent == null) {
			return "redirect:/staff/content/event";
		}

		String error = validateEvent(event, false);

		if (error != null) {
			return returnUpdateForm(model, event, error);
		}

		boolean imageChanged = event.getUpload() != null && !event.getUpload().isEmpty();

		if (imageChanged) {
			String filename = FileUtil.createFile(request, event.getUpload());
			event.setImage_url(filename);
		}

		staffEventService.updateEvent(event);

		if (imageChanged) {
			FileUtil.removeFile(request, originEvent.getImage_url());
		}

		return "redirect:/staff/content/event/detail?event_id=" + event.getEvent_id();
	}

	@PostMapping("/hide")
	public String hide(@RequestParam("event_id") long event_id) {
		staffEventService.hideEvent(event_id);

		return "redirect:/staff/content/event/detail?event_id=" + event_id;
	}

	@PostMapping("/end")
	public String end(@RequestParam("event_id") long event_id) {
		staffEventService.endEvent(event_id);

		return "redirect:/staff/content/event/detail?event_id=" + event_id;
	}

	private String returnWriteForm(Model model, EventVO event, String error) {
		model.addAttribute("activeMenu", "content");
		model.addAttribute("event", event);
		model.addAttribute("error", error);

		return "thviews/staff_main/content/event/event_write";
	}

	private String returnUpdateForm(Model model, EventVO event, String error) {
		model.addAttribute("activeMenu", "content");
		model.addAttribute("event", event);
		model.addAttribute("error", error);

		return "thviews/staff_main/content/event/event_update";
	}

	private String validateEvent(EventVO event, boolean imageRequired) {
		if (isBlank(event.getTitle()) || isBlank(event.getContent())) {
			return "이벤트명과 상세 내용은 필수입니다.";
		}

		if (event.getStart_date() == null || event.getEnd_date() == null) {
			return "이벤트 시작일과 종료일을 입력하세요.";
		}

		if (event.getStart_date().after(event.getEnd_date())) {
			return "종료일은 시작일보다 빠를 수 없습니다.";
		}

		if (imageRequired && (event.getUpload() == null || event.getUpload().isEmpty())) {
			return "배너 이미지를 선택하세요.";
		}

		if (event.getUpload() != null && !event.getUpload().isEmpty()) {
			String contentType = event.getUpload().getContentType();

			if (contentType == null || !contentType.startsWith("image/")) {
				return "이미지 파일만 업로드할 수 있습니다.";
			}

			if (event.getUpload().getSize() > 10 * 1024 * 1024) {
				return "이미지 파일은 10MB 이하만 업로드할 수 있습니다.";
			}
		}

		return null;
	}

	private boolean isBlank(String value) {
		return value == null || value.trim().isEmpty();
	}
}