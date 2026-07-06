package kr.spring.member.notice.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpServletRequest;
import kr.spring.member.notice.service.MemberNoticeService;
import kr.spring.member.notice.vo.MemberNoticeVO;
import kr.spring.util.PagingUtil;
import kr.spring.util.StringUtil;
import lombok.extern.slf4j.Slf4j;

@Controller
@Slf4j
@RequestMapping("/member/notice")
public class MemberNoticeController {

	@Autowired
	private MemberNoticeService memberNoticeService;
	
	//공지사항 목록
	@GetMapping("/list")
	public String getList(@RequestParam(defaultValue = "1") int pageNum, String keyfield, String keyword, Model model) {
		
		Map<String,Object> map = new HashMap<String,Object>();
		
		if(keyword != null) {
			keyword = keyword.trim();
			
			if(keyword.length() == 0) {
				keyword = null;
			}
		}
		
		if(keyword != null && (keyfield == null || keyfield.length() == 0)) {
			keyfield = "1";
		}
		
		map.put("keyfield", keyfield);
		map.put("keyword", keyword);
		
		//전체/검색 레코드 수
		int count = memberNoticeService.selectRowCount(map);
		
		//페이지 처리
		PagingUtil page = new PagingUtil(keyfield, keyword, pageNum, count, 10, 10, "list");
		
		List<MemberNoticeVO> list = null;
		
		if(count > 0) {
			map.put("skip", page.getSkip());
			map.put("limit", page.getLimit());
			
			list = memberNoticeService.selectList(map);
		}
		
		model.addAttribute("count", count);
		model.addAttribute("list", list);
		model.addAttribute("page", page.getPage());
		model.addAttribute("keyfield", keyfield);
		model.addAttribute("keyword", keyword);
		model.addAttribute("activeMenu", "notice");
		
		return "thviews/member/member_notice_list";
	}
	
	//공지사항 상세
	@GetMapping("/detail")
	public String process(long notice_id, Model model, HttpServletRequest request) {
		
		log.debug("<<공지사항 상세 - notice_id>> : " + notice_id);

		MemberNoticeVO notice = memberNoticeService.selectNotice(notice_id);
		
		if(notice == null) {
			model.addAttribute("accessTitle", "공지사항");
			model.addAttribute("accessMsg", "조회 결과가 없습니다.");
			model.addAttribute("accessBtn", "목록으로");
			model.addAttribute("accessUrl", request.getContextPath() + "/member/notice/list");
			
			return "thviews/common/resultView";
		}
		
		//제목에 태그를 허용하지 않음.
		notice.setTitle(StringUtil.useNoHtml(notice.getTitle()));
		
		//본문은 태그를 허용하지 않으면서 줄바꿈 처리
		notice.setContent(StringUtil.useBrNoHtml(notice.getContent()));
		
		model.addAttribute("notice", notice);
		model.addAttribute("activeMenu", "notice");
		
		return "thviews/member/member_notice_detail";
	}
	
}
