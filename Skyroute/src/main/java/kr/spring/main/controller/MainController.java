package kr.spring.main.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import jakarta.annotation.Resource;
import kr.spring.member.service.MemberService;
import kr.spring.member.vo.MemberVO;
import kr.spring.member.vo.PrincipalDetails;
import kr.spring.member.vo.UserRole;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
public class MainController {

	@Autowired
	private MemberService memberService;

	@GetMapping("/")
	public String init(@AuthenticationPrincipal PrincipalDetails principal) {
		if(principal!=null && principal.getMemberVO().getAuthority().equals(UserRole.ADMIN.getValue())) {
			return "redirect:/admin/home";
		}
		return "redirect:/main/home";
	}

	@GetMapping("/main/home")
	public String main(Model model) {
		return "thviews/main/main";
	}
}
