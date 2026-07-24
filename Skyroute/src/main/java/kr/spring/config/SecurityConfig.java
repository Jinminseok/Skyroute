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
import org.springframework.security.web.csrf.XorCsrfTokenRequestAttributeHandler;

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
		
		/*
	     * Spring Security 6은 CSRF 토큰을 기본적으로 지연 로딩합니다.
	     *
	     * 로그아웃 직후 Thymeleaf가 로그인 POST 폼을 렌더링할 때
	     * CSRF 토큰을 뒤늦게 생성하면 이미 응답이 일부 전송된 상태에서
	     * 세션을 생성하려고 하여 ERR_INCOMPLETE_CHUNKED_ENCODING이
	     * 발생할 수 있습니다.
	     *
	     * null로 설정하면 CSRF 토큰이 요청 초기에 로딩됩니다.
	     */
	    XorCsrfTokenRequestAttributeHandler csrfRequestHandler =
	            new XorCsrfTokenRequestAttributeHandler();

	    csrfRequestHandler.setCsrfRequestAttributeName(null);
		
		return http
				
				/*
				 * CSRF 보호를 끄는 것이 아닐,
				 * CSRF 토큰의 지연 로딩만 해체합니다.
				 */
				
				.csrf(csrf -> csrf
						.csrfTokenRequestHandler(csrfRequestHandler)
				)
				
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
						
						// 관리자 전용 경로
						.requestMatchers("/admin/**").hasAuthority("ADMIN")
						
						// 지상직 전용 경로
						.requestMatchers("/staff/**").hasAuthority("STAFF")
						
						// 위 조건 외의 모든 요청은 로그인 필요
						.anyRequest().authenticated() 
				)
				// 일반 로그인 설정
				.formLogin(login -> login 
				        .loginPage("/member/login")
				        .successHandler(authenticationSuccessHandler) 
				        .failureHandler(authenticationFailureHandler) 
				        .usernameParameter("id")
				        .passwordParameter("password"))
				
				// 로그아웃 설정
				.logout(logout -> logout 
						.logoutUrl("/member/logout")
						.logoutSuccessHandler(customLogoutSuccessHandler)
						.invalidateHttpSession(true)
						.clearAuthentication(true)
						.deleteCookies("JSESSIONID")
						.permitAll()
				)
						
				// 예외 처리 설정
				.exceptionHandling(error -> error
						.authenticationEntryPoint(
								(request,response,exception) -> { 
									request.getRequestDispatcher("/main/resultError").forward(request, response);
								}
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