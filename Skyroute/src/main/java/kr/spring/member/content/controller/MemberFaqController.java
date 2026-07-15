package kr.spring.member.content.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import kr.spring.staff.content.service.StaffFaqService;
import kr.spring.staff.content.vo.StaffFaqVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/member/faq")
public class MemberFaqController {

    private final StaffFaqService staffFaqService;

    @GetMapping("/list")
    public String faqList(@RequestParam(defaultValue = "") String category,
            			  @RequestParam(defaultValue = "") String keyword,
            			  Model model) {
                          
    	Map<String, Object> map = new HashMap<>();
        map.put("category", category);
        map.put("keyword", keyword);
        // 사용자 화면 - 공개 상태인 것만 조회
        map.put("is_visible", "Y");
        
        // 전체보기 상태인지 확인
        boolean isViewAll = category.isEmpty() && keyword.isEmpty();

        List<StaffFaqVO> list = staffFaqService.selectMemberFaqList(map);

        // 전체보기 상태라면 리스트를 10개로
        if (isViewAll && list != null && list.size() > 10) {
            list = list.subList(0, 10);
        }

        // 전체 건수
        int count = list != null ? list.size() : 0;

        model.addAttribute("count", count);
        model.addAttribute("list", list);
        model.addAttribute("category", category);
        model.addAttribute("keyword", keyword);

        return "thviews/member/faq/faq_list";
    }
}