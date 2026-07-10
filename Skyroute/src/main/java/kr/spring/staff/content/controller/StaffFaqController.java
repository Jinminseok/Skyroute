package kr.spring.staff.content.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import kr.spring.staff.content.service.StaffFaqService;
import kr.spring.staff.content.vo.StaffFaqVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/staff/content/faq")
public class StaffFaqController {

    private final StaffFaqService staffFaqService;

    // 1. FAQ 목록 및 검색
    @GetMapping("/list")
    public String list(@RequestParam(defaultValue = "1") int pageNum,
                       @RequestParam(defaultValue = "") String category,
                       @RequestParam(defaultValue = "") String keyword,
                       Model model) {
                       
        Map<String, Object> map = new HashMap<>();
        map.put("category", category);
        map.put("keyword", keyword);

        int count = staffFaqService.selectRowCount(map);

        // 페이징 처리 (1페이지당 10개)
        int pageSize = 10;
        int start = (pageNum - 1) * pageSize + 1;
        int end = pageNum * pageSize;
        map.put("start", start);
        map.put("end", end);

        List<StaffFaqVO> list = null;
        if (count > 0) {
            list = staffFaqService.selectFaqList(map);
        }

        // 전체 페이지 수 계산
        int totalPage = (int) Math.ceil((double) count / pageSize);

        model.addAttribute("count", count);
        model.addAttribute("list", list);
        model.addAttribute("currentPage", pageNum);
        model.addAttribute("totalPage", totalPage);
        model.addAttribute("category", category);
        model.addAttribute("keyword", keyword);
        
        // 공통 메뉴 활성화 값
        model.addAttribute("activeMenu", "content");

        return "thviews/staff_main/content/faq/faq_list";
    }

    // 2. FAQ 등록 폼
    @GetMapping("/write")
    public String writeForm(Model model) {
        model.addAttribute("activeMenu", "content");
        return "thviews/staff_main/content/faq/faq_write";
    }

    // 3. FAQ 등록 처리
    @PostMapping("/write")
    public String writeSubmit(StaffFaqVO faqVO) {
        staffFaqService.insertFaq(faqVO);
        return "redirect:/staff/content/faq/list";
    }

    // 4. FAQ 상세 보기
    @GetMapping("/detail")
    public String detail(@RequestParam("faq_id") int faq_id, Model model) {
        StaffFaqVO faq = staffFaqService.selectFaq(faq_id);
        model.addAttribute("faq", faq);
        model.addAttribute("activeMenu", "content");
        return "thviews/staff_main/content/faq/faq_detail";
    }

    // 5. FAQ 수정 폼
    @GetMapping("/modify")
    public String modifyForm(@RequestParam("faq_id") int faq_id, Model model) {
        StaffFaqVO faq = staffFaqService.selectFaq(faq_id);
        model.addAttribute("faq", faq);
        model.addAttribute("activeMenu", "content");
        return "thviews/staff_main/content/faq/faq_modify";
    }

    // 6. FAQ 수정 처리
    @PostMapping("/modify")
    public String modifySubmit(StaffFaqVO faqVO) {
        staffFaqService.updateFaq(faqVO);
        return "redirect:/staff/content/faq/detail?faq_id=" + faqVO.getFaq_id();
    }

    // 7. FAQ 삭제 처리
    @PostMapping("/delete")
    public String deleteSubmit(@RequestParam("faq_id") int faq_id) {
        staffFaqService.deleteFaq(faq_id);
        return "redirect:/staff/content/faq/list";
    }

    // 8. 노출 여부 즉시 토글 (AJAX 통신용)
    @PostMapping("/toggle")
    @ResponseBody
    public String toggleVisible(@RequestBody Map<String, Object> payload) {
        try {
            staffFaqService.updateFaqVisible(payload);
            return "success";
        } catch (Exception e) {
            log.error("FAQ 상태 토글 중 오류 발생: ", e);
            return "error";
        }
    }
}