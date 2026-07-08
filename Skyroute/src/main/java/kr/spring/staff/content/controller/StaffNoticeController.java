package kr.spring.staff.content.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import kr.spring.member.vo.PrincipalDetails;
import kr.spring.staff.content.service.StaffNoticeService;
import kr.spring.staff.content.vo.StaffNoticeVO;
import kr.spring.util.PagingUtil;
import kr.spring.util.StringUtil;
import kr.spring.util.ValidationUtil;
import lombok.extern.slf4j.Slf4j;

@Controller
@Slf4j
@RequestMapping("/staff/content/notice")
public class StaffNoticeController {

	@Autowired
	private StaffNoticeService staffNoticeService;

	@ModelAttribute
	public StaffNoticeVO initCommand() {
		return new StaffNoticeVO();
	}


	// 공지사항 목록
	@GetMapping({"/list", ""})
	public String getList(@RequestParam(defaultValue = "1") int pageNum,
						  String keyfield,
						  String keyword,
						  String is_public,
						  Model model) {

		Map<String, Object> map = new HashMap<String, Object>();

		if (keyword != null) {
			keyword = keyword.trim();

			if (keyword.length() == 0) {
				keyword = null;
			}
		}

		if (keyword != null && (keyfield == null || keyfield.length() == 0)) {
			keyfield = "1";
		}

		if (is_public != null) {
			is_public = is_public.trim();

			if (is_public.length() == 0) {
				is_public = null;
			}
		}

		if (is_public != null && !"Y".equals(is_public) && !"N".equals(is_public)) {
			is_public = null;
		}

		map.put("keyfield", keyfield);
		map.put("keyword", keyword);
		map.put("is_public", is_public);

		int count = staffNoticeService.selectRowCount(map);

		String addKey = null;

		if (is_public != null) {
			addKey = "&is_public=" + is_public;
		}

		PagingUtil page = new PagingUtil(keyfield, keyword, pageNum, count, 10, 10, "list", addKey);

		List<StaffNoticeVO> list = null;

		if (count > 0) {
			map.put("skip", page.getSkip());
			map.put("limit", page.getLimit());

			list = staffNoticeService.selectList(map);
		}

		model.addAttribute("count", count);
		model.addAttribute("list", list);
		model.addAttribute("page", page.getPage());
		model.addAttribute("keyfield", keyfield);
		model.addAttribute("keyword", keyword);
		model.addAttribute("is_public", is_public);
		model.addAttribute("activeMenu", "content");

		return "thviews/staff_main/content/notice/notice_list";
	}


	// 등록 폼
	@GetMapping("/write")
	public String form(Model model) {
		model.addAttribute("activeMenu", "content");
		return "thviews/staff_main/content/notice/notice_write";
	}


	// 등록 처리
	@PostMapping("/write")
	public String submit(@Valid StaffNoticeVO staffNoticeVO,
						 BindingResult result,
						 HttpServletRequest request,
						 @AuthenticationPrincipal PrincipalDetails principal,
						 Model model) {

		log.debug("<<지상직 공지사항 등록>> : {}", staffNoticeVO);

		if (result.hasErrors()) {
			ValidationUtil.printErrorFields(result);
			model.addAttribute("activeMenu", "content");
			return "thviews/staff_main/content/notice/notice_write";
		}

		if (principal == null || principal.getMemberVO() == null) {
			return "thviews/common/accessDenied";
		}

		staffNoticeVO.setCreated_by(principal.getMemberVO().getMember_id());

		if (staffNoticeVO.getIs_public() == null || staffNoticeVO.getIs_public().length() == 0) {
			staffNoticeVO.setIs_public("Y");
		}

		staffNoticeService.insertNotice(staffNoticeVO);

		model.addAttribute("message", "공지사항 등록이 완료되었습니다.");
		model.addAttribute("url", request.getContextPath() + "/staff/content/notice/list");

		return "thviews/common/resultAlert";
	}


	// 상세
	@GetMapping("/detail")
	public String detail(long notice_id,
						 Model model,
						 HttpServletRequest request) {

		log.debug("<<지상직 공지사항 상세 - notice_id>> : {}", notice_id);

		StaffNoticeVO notice = staffNoticeService.selectNotice(notice_id);

		if (notice == null) {
			model.addAttribute("accessTitle", "공지사항 관리");
			model.addAttribute("accessMsg", "조회 결과가 없습니다.");
			model.addAttribute("accessBtn", "목록으로");
			model.addAttribute("accessUrl", request.getContextPath() + "/staff/content/notice/list");

			return "thviews/common/resultView";
		}

		notice.setTitle(StringUtil.useNoHtml(notice.getTitle()));
		notice.setContent(StringUtil.useBrNoHtml(notice.getContent()));

		model.addAttribute("notice", notice);
		model.addAttribute("activeMenu", "content");

		return "thviews/staff_main/content/notice/notice_detail";
	}


	// 수정 폼
	@GetMapping("/modify")
	public String formUpdate(long notice_id,
							 Model model,
							 HttpServletRequest request) {

		StaffNoticeVO notice = staffNoticeService.selectNotice(notice_id);

		if (notice == null) {
			model.addAttribute("accessTitle", "공지사항 관리");
			model.addAttribute("accessMsg", "조회 결과가 없습니다.");
			model.addAttribute("accessBtn", "목록으로");
			model.addAttribute("accessUrl", request.getContextPath() + "/staff/content/notice/list");

			return "thviews/common/resultView";
		}

		model.addAttribute("staffNoticeVO", notice);
		model.addAttribute("activeMenu", "content");

		return "thviews/staff_main/content/notice/notice_modify";
	}


	// 수정 처리
	@PostMapping("/modify")
	public String submitUpdate(@Valid StaffNoticeVO staffNoticeVO,
							   BindingResult result,
							   HttpServletRequest request,
							   Model model) {

		log.debug("<<지상직 공지사항 수정>> : {}", staffNoticeVO);

		StaffNoticeVO dbNotice = staffNoticeService.selectNotice(staffNoticeVO.getNotice_id());

		if (dbNotice == null) {
			model.addAttribute("accessTitle", "공지사항 관리");
			model.addAttribute("accessMsg", "조회 결과가 없습니다.");
			model.addAttribute("accessBtn", "목록으로");
			model.addAttribute("accessUrl", request.getContextPath() + "/staff/content/notice/list");

			return "thviews/common/resultView";
		}

		if (result.hasErrors()) {
			ValidationUtil.printErrorFields(result);
			model.addAttribute("activeMenu", "content");
			return "thviews/staff_main/content/notice/notice_modify";
		}

		staffNoticeService.updateNotice(staffNoticeVO);

		model.addAttribute("message", "공지사항 수정이 완료되었습니다.");
		model.addAttribute("url", request.getContextPath() + "/staff/content/notice/detail?notice_id=" + staffNoticeVO.getNotice_id());

		return "thviews/common/resultAlert";
	}


	// 공개/비공개 변경
	@PostMapping("/public")
	public String updatePublic(long notice_id,
							   String is_public,
							   HttpServletRequest request,
							   Model model) {

		if (!"Y".equals(is_public) && !"N".equals(is_public)) {
			model.addAttribute("accessTitle", "공지사항 관리");
			model.addAttribute("accessMsg", "잘못된 공개 여부 값입니다.");
			model.addAttribute("accessBtn", "목록으로");
			model.addAttribute("accessUrl", request.getContextPath() + "/staff/content/notice/list");

			return "thviews/common/resultView";
		}

		StaffNoticeVO dbNotice = staffNoticeService.selectNotice(notice_id);

		if (dbNotice == null) {
			model.addAttribute("accessTitle", "공지사항 관리");
			model.addAttribute("accessMsg", "조회 결과가 없습니다.");
			model.addAttribute("accessBtn", "목록으로");
			model.addAttribute("accessUrl", request.getContextPath() + "/staff/content/notice/list");

			return "thviews/common/resultView";
		}

		StaffNoticeVO notice = new StaffNoticeVO();
		notice.setNotice_id(notice_id);
		notice.setIs_public(is_public);

		staffNoticeService.updateNoticePublic(notice);

		model.addAttribute("message", "공지사항 공개 여부가 변경되었습니다.");
		model.addAttribute("url", request.getContextPath() + "/staff/content/notice/list");

		return "thviews/common/resultAlert";
	}


	// 삭제 처리
	@PostMapping("/delete")
	public String delete(long notice_id,
						 HttpServletRequest request,
						 Model model) {

		StaffNoticeVO dbNotice = staffNoticeService.selectNotice(notice_id);

		if (dbNotice == null) {
			model.addAttribute("accessTitle", "공지사항 관리");
			model.addAttribute("accessMsg", "조회 결과가 없습니다.");
			model.addAttribute("accessBtn", "목록으로");
			model.addAttribute("accessUrl", request.getContextPath() + "/staff/content/notice/list");

			return "thviews/common/resultView";
		}

		staffNoticeService.deleteNotice(notice_id);

		model.addAttribute("message", "공지사항이 삭제되었습니다.");
		model.addAttribute("url", request.getContextPath() + "/staff/content/notice/list");

		return "thviews/common/resultAlert";
	}
}