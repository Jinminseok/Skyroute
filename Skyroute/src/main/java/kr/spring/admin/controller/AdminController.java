package kr.spring.admin.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
public class AdminController {

	@GetMapping("/admin/base1")
	public String adminMain() {
	    
	    return "thviews/admin_main/admin_base1"; 
	}
	
	
	@GetMapping("/admin/base2")
	public String adminBase2() {
	    
	    return "thviews/admin_main/admin_base2"; 
	}


    // 3. 계정/회원 관리 페이지
    @GetMapping("/accounts")
    public String accounts() {
        return "thviews/admin_main/admin_accounts";
    }

    // 4. 전사 운영 통계 페이지
    @GetMapping("/statistics")
    public String statistics() {
        return "thviews/admin_main/admin_statistics";
    }
	
}