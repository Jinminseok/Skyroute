package kr.spring.member.security;

import java.io.IOException;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
import kr.spring.member.vo.UserRole;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
//인증(로그인)에 성공한 후, 리다이렉트할 URL을 지정하거나 처리 로직을 직접 작성할 때 사용
public class CustomSuccessHandler extends SimpleUrlAuthenticationSuccessHandler{
	
	@Override
    public void onAuthenticationSuccess(HttpServletRequest request, 
    		HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        MemberVO user = ((PrincipalDetails)authentication.getPrincipal()).getMemberVO();
        log.debug("[Login Check 2] CustomSuccessHandler : " + user);
       
		if(user.getAuthority().equals(UserRole.ADMIN.getValue())) {//관리자					
			setDefaultTargetUrl("/admin/home");
		}else if(user.getAuthority().equals(UserRole.SUSPENDED.getValue())) {//정지회원	
			log.debug("[Login Check 2] 정지회원 : " + user.getId());
			//정지회원일 경우 로그아웃 처리		
			new SecurityContextLogoutHandler().logout(request, response, authentication);
			
			FlashMap flashMap = new FlashMap();
	        flashMap.put("error", "error_suspended");
	        FlashMapManager flashMapManager = new SessionFlashMapManager();
	        flashMapManager.saveOutputFlashMap(flashMap, request, response);
	        
			setDefaultTargetUrl("/member/login");
		}else {
			//루트로 이동시 생략 가능
			setDefaultTargetUrl("/");
		}
       super.onAuthenticationSuccess(request, response, authentication);
    }
}
