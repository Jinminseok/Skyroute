package kr.spring.member.security;

import java.io.IOException;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.FlashMap;
import org.springframework.web.servlet.FlashMapManager;
import org.springframework.web.servlet.support.SessionFlashMapManager;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kr.spring.member.vo.MemberVO;
import kr.spring.member.vo.PrincipalDetails;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class CustomSuccessHandler extends SimpleUrlAuthenticationSuccessHandler{
	
	@Override
    public void onAuthenticationSuccess(HttpServletRequest request, 
    		HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        
        MemberVO user = ((PrincipalDetails)authentication.getPrincipal()).getMemberVO();
        String auth = user.getRole();
        
        log.info("====== [CustomSuccessHandler] 로그인 성공! 권한 값: [{}] ======", auth);
       
		if(auth != null && auth.contains("ADMIN")) { 			
            // 1. 관리자인 경우 강제 이동 후 return
			log.info("🚀 [SuccessHandler] 관리자 로그인! /admin/base1 으로 보냅니다."); // 추가
            response.sendRedirect("/admin/base1");
            return;
            
		} else if(auth != null && auth.contains("SUSPENDED")) { 	
			log.debug("[Login Check 2] 정지회원 : " + user.getId());	
			new SecurityContextLogoutHandler().logout(request, response, authentication);
			
			FlashMap flashMap = new FlashMap();
	        flashMap.put("error", "error_suspended");
	        FlashMapManager flashMapManager = new SessionFlashMapManager();
	        flashMapManager.saveOutputFlashMap(flashMap, request, response);
	        
            // 2. 정지회원인 경우 로그인 페이지로 강제 이동 후 return
            response.sendRedirect("/member/login");
            return;
		}
        
        // 3. 그 외 일반 사용자인 경우 메인 홈으로 강제 이동
        response.sendRedirect("/main/home");
    }
}