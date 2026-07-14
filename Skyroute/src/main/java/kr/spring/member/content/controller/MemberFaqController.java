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

    // 💡 지상직에서 만들어둔 서비스를 그대로 재활용합니다!
    private final StaffFaqService staffFaqService;

    @GetMapping("/list")
    public String faqList(@RequestParam(defaultValue = "1") int pageNum,
                          @RequestParam(defaultValue = "") String category,
                          @RequestParam(defaultValue = "") String keyword,
                          Model model) {
                          
        Map<String, Object> map = new HashMap<>();
        map.put("category", category);
        map.put("keyword", keyword);
        
        // 🚨 핵심 포인트: 사용자 화면이므로 '공개(Y)' 상태인 것만 조회하도록 강제 설정
        map.put("is_visible", "Y");

        int count = staffFaqService.selectRowCount(map);

        // 페이징 처리 (1페이지당 10개 노출)
        int pageSize = 10;
        int start = (pageNum - 1) * pageSize + 1;
        int end = pageNum * pageSize;
        map.put("start", start);
        map.put("end", end);

        List<StaffFaqVO> list = null;
        if (count > 0) {
            list = staffFaqService.selectFaqList(map);
        }

        int totalPage = (int) Math.ceil((double) count / pageSize);

        model.addAttribute("count", count);
        model.addAttribute("list", list);
        model.addAttribute("currentPage", pageNum);
        model.addAttribute("totalPage", totalPage);
        model.addAttribute("category", category);
        model.addAttribute("keyword", keyword);

        // 레이아웃의 메뉴 활성화를 위한 변수 (필요 시 사용)
        model.addAttribute("activeMenu", "customer"); 

        return "thviews/member/faq/faq_list";
    }
}