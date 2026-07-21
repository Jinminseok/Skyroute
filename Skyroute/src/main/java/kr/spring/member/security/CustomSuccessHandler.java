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
		String auth = user.getRole(); // ADMIN, STAFF, USER
		
		// 로그인 요청을 보낸 페이지(URL) 확인
		String referer = request.getHeader("Referer");
		boolean isAdminLoginPage = (referer != null && referer.contains("Admin_login"));
		
		log.info("====== [CustomSuccessHandler] 로그인 성공! 권한: [{}], 관리자페이지여부: [{}] ======", auth, isAdminLoginPage);

		// 1. 관리자 전용 로그인 페이지(/main/home/Admin_login)에서 일반 사용자(USER)가 로그인한 경우
		if (isAdminLoginPage && "USER".equals(auth)) {
			log.warn("🚫 사용자 계정으로 관리자 로그인 시도 차단 : {}", user.getId());
			
			// 인증 세션 즉시 파기(로그아웃)
			new SecurityContextLogoutHandler().logout(request, response, authentication);
			
			FlashMap flashMap = new FlashMap();
			flashMap.put("error", "error_user_not_allowed");
			FlashMapManager flashMapManager = new SessionFlashMapManager();
			flashMapManager.saveOutputFlashMap(flashMap, request, response);
			
			response.sendRedirect("/main/home/Admin_login");
			return;
		}

		// 2. 일반 사용자 로그인 페이지(/member/login)에서 관리자/스태프(ADMIN, STAFF)가 로그인한 경우
		if (!isAdminLoginPage && (auth != null && (auth.contains("ADMIN") || auth.contains("STAFF")))) {
			log.warn("🚫 관리자/스태프 계정으로 일반 사용자 로그인 시도 차단 : {}", user.getId());
			
			// 인증 세션 즉시 파기(로그아웃)
			new SecurityContextLogoutHandler().logout(request, response, authentication);
			
			FlashMap flashMap = new FlashMap();
			flashMap.put("error", "error_admin_not_allowed");
			FlashMapManager flashMapManager = new SessionFlashMapManager();
			flashMapManager.saveOutputFlashMap(flashMap, request, response);
			
			response.sendRedirect("/member/login");
			return;
		}

		// 3. 정상 권한에 맞게 성공적으로 로그인된 경우 권한별 리다이렉트
		if (auth != null && auth.contains("ADMIN")) { 			
			response.sendRedirect("/admin/base1");
			return;
		} else if (auth != null && auth.contains("STAFF")) {
			response.sendRedirect("/staff/main");
			return;
		}
		
		// 일반 사용자 로그인 성공시
		response.sendRedirect("/main/home");
	}
}