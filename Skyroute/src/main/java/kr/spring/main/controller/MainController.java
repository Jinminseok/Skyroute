package kr.spring.main.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import kr.spring.member.service.MemberService;
import kr.spring.member.vo.PrincipalDetails;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
public class MainController {

	@Autowired
	private MemberService memberService;

	@GetMapping("/")
	public String init(@AuthenticationPrincipal PrincipalDetails principal) {
		if (principal != null && principal.getMemberVO() != null) {
			String auth = principal.getMemberVO().getAuthority();
			
		
			log.info("====== [MainController] 넘어온 권한 값: [{}] ======", auth);
			
		
			if (auth != null && auth.contains("ADMIN")) {
				return "redirect:/admin/home";
			}
		}
		return "redirect:/main/home";
	}

	@GetMapping("/main/home")
	public String main(Model model) {
		return "thviews/main/main"; 
	}
	
	@GetMapping("/main/home/Admin_login")
	public String Admin_login(Model model) {
		return "thviews/member/Admin_login";
	}
	@GetMapping("/accessDenied")
	public String accessDenied(Model model) {
		log.info("🚫 [AccessDenied] 권한 부족! 메인으로 쫓겨납니다."); // 추가
	    model.addAttribute("message", "접근 권한이 없습니다.");
	    return "redirect:/main/home"; // 일단 에러 나면 일반 메인으로 튕기게 설정
	}
}