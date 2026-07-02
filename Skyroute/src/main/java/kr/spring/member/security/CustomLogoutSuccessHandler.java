package kr.spring.member.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class CustomLogoutSuccessHandler implements LogoutSuccessHandler {

    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response,
                                Authentication authentication) throws IOException, ServletException {
        
        // 1. 기본 이동 경로 (일반 사용자가 로그아웃 했을 때)
        String redirectUrl = "/"; 

        // 2. 인증 정보가 존재하는 경우 권한에 따라 분기
        if (authentication != null) {
            boolean isAdmin = authentication.getAuthorities().stream()
                    .anyMatch(role -> role.getAuthority().equals("ADMIN"));
            
            boolean isStaff = authentication.getAuthorities().stream()
                    .anyMatch(role -> role.getAuthority().equals("STAFF"));

            if (isAdmin) {
                // 관리자인 경우 관리자 로그인 페이지로 이동
                redirectUrl = "/main/home/Admin_login";
            } else if (isStaff) {
                // 지상직인 경우 지상직 전용 페이지로 이동 (경로명은 프로젝트에 맞게 수정하세요)
                redirectUrl = "/main/home/Admin_login"; 
            }
        }

        // 3. 결정된 URL로 리다이렉트 실행
        response.sendRedirect(redirectUrl);
    }
}