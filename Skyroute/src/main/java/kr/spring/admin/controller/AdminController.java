package kr.spring.admin.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
public class AdminController {


	@GetMapping("/admin/home")
	public String adminMain() {
		log.info("🎯 [AdminController] 관리자 메인 페이지 진입 성공!"); // 추가
		
	    return "thviews/admin_main/admin_main"; 
	}
	
}