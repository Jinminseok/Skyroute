package kr.spring.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import kr.spring.member.security.CustomAccessDeniedHandler;
import kr.spring.member.security.CustomLogoutSuccessHandler;
import kr.spring.member.security.UserSecurityService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
//이 클래스가 Spring 설정 파일이라는 의미
@Configuration
//모든 요청 URL이 스프링 시큐리티의 제어를 받도록 만드는 애너테이션,스프링 시큐리티를 활성화하는 역할
@EnableWebSecurity
//Controller 메서드 레벨에서 권한을 체크할 수 있도록 설정. @PreAuthorize 사용시 추가
@EnableMethodSecurity
public class SecurityConfig{

	// [수정됨] 자동 로그인(Remember-Me)에 사용되던 DataSource와 key 변수는 삭제되었습니다.

	//로그인 시 사용자 정보를 조회하고, 이를 기반으로 인증(Authentication)을 수행하는 데 사용
	@Autowired
	private UserSecurityService userSecurityService;
	
	//인증(로그인)에 성공한 후, 리다이렉트할 URL을 지정하거나 처리 로직을 직접 작성할 때 사용
	@Autowired
	private AuthenticationSuccessHandler authenticationSuccessHandler;
	
	//로그인 실패 시 처리를 담당하는 클래스
	@Autowired
	private AuthenticationFailureHandler authenticationFailureHandler;
	@Autowired
	private CustomLogoutSuccessHandler customLogoutSuccessHandler;
	// 권한이 없을 때 처리하는 클래스
	@Autowired
	private CustomAccessDeniedHandler customAccessDeniedHandler;

	@Bean
	SecurityFilterChain filterChain(HttpSecurity http) throws Exception {	
		return http
				
				.authorizeHttpRequests(authorize -> authorize
				
						.requestMatchers(
								"/",
								"/assets/**",
								"/main/**",
								"/booking/flights/search",
								"/booking/flights/detail",
								"/member/member_route",
								"/member/**",
								"/member/member_schedule"
						).permitAll()
						
						.requestMatchers("/member/findId", "/member/findPw").permitAll()
						
						// 관리자 및 스태프 전용 구역 보호
						.requestMatchers("/admin/**").hasAuthority("ADMIN")
						.requestMatchers("/staff/**").hasAuthority("STAFF")
						
						// 위 조건 외의 모든 요청은 로그인 필요
						.anyRequest().authenticated() 
				)
				// 일반 로그인 설정
				.formLogin(login -> login 
						.loginPage("/member/login")
						.defaultSuccessUrl("/", true)
						.failureHandler(authenticationFailureHandler)
						.usernameParameter("id")
						.passwordParameter("password"))
				
				// 로그아웃 설정
				.logout(logout -> logout 
						.logoutUrl("/member/logout")
						.logoutSuccessHandler(customLogoutSuccessHandler)
						.invalidateHttpSession(true)
						.deleteCookies("JSESSIONID"))
						
				// 예외 처리 설정
				.exceptionHandling(error -> error
						.authenticationEntryPoint(
								(req,res,e) -> { req.getRequestDispatcher("/main/resultError").forward(req, res);}
						)
						.accessDeniedHandler(customAccessDeniedHandler)
					)
				.build();	
	}
	
	// 비밀번호 암호화 객체 생성
	@Bean
	public PasswordEncoder passwordEncoder() {
		 // BCrypt 방식 암호화 사용
		return new BCryptPasswordEncoder();
	}
	
	// [수정됨] PersistentTokenRepository 객체 생성 메서드도 파이널 프로젝트에서 사용하지 않으므로 삭제되었습니다.
}