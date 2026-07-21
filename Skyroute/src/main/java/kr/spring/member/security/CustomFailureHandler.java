package kr.spring.member.security;

import java.io.IOException;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.FlashMap;
import org.springframework.web.servlet.FlashMapManager;
import org.springframework.web.servlet.support.SessionFlashMapManager;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
//로그인 실패 시 처리를 담당하는 클래스. 사용자가 인증(로그인)을 시도했지만 실패했을 때, 사용자를 어떤 URL로 리다이렉트할지 지정하거나 추가적인 로직을 실행
public class CustomFailureHandler extends SimpleUrlAuthenticationFailureHandler{

	@Override
	public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
			AuthenticationException exception) throws IOException, ServletException {
		
		log.error("[CustomFailureHandler] 로그인 실패 예외 클래스: {}, 메시지: {}", 
				exception.getClass().getName(), exception.getMessage());
		
		FlashMap flashMap = new FlashMap();
		
		// 🎯 예외 종류에 따라 FlashMap 에러 키값 세팅
		if (exception instanceof LockedException || "UserSuspended".equals(exception.getMessage())) {
			// 계정 정지 (SUSPENDED)
			flashMap.put("error", "error_suspended");
			
		} else if (exception instanceof DisabledException || "UserDeletedOrInactive".equals(exception.getMessage())) {
			// 탈퇴 / 비활성화 (DELETED / INACTIVE)
			flashMap.put("error", "error_deleted");
			
		} else {
			// 아이디/비밀번호 불일치 및 기타 (UserNotFound / BadCredentialsException 등)
			flashMap.put("error", "error");
		}
		
		FlashMapManager flashMapManager = new SessionFlashMapManager();
		flashMapManager.saveOutputFlashMap(flashMap, request, response);
		
		// 시도했던 경로 확인 후 실패 URL 설정
		String referer = request.getHeader("Referer");
		if (referer != null && referer.contains("Admin_login")) {
			setDefaultFailureUrl("/main/home/Admin_login");
		} else {
			setDefaultFailureUrl("/member/login");
		}
		
		super.onAuthenticationFailure(request, response, exception);
	}

}
