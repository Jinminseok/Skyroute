package kr.spring.member.security;

import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import kr.spring.member.service.MemberService;
import kr.spring.member.vo.MemberVO;
import kr.spring.member.vo.PrincipalDetails;
import kr.spring.member.vo.UserRole;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
//클래스 내에서 final 또는 @NonNull로 선언된 필드에 대해 생성자를 자동으로 생성
@RequiredArgsConstructor
@Service
//로그인 시 사용자 정보를 조회하고, 이를 기반으로 인증(Authentication)을 수행하는 데 사용
public class UserSecurityService implements UserDetailsService {
	//@RequiredArgsConstructor에 의해 의존성 주입됨
	private final MemberService memberService;

	@Override
	public UserDetails loadUserByUsername(String id) throws UsernameNotFoundException {
		log.debug("[Login Check 1 - UserSecurityService] 로그인 아이디 :" + id);
		
		MemberVO member = memberService.selectCheckMember(id);
		
		// 1. 회원 정보가 없는 경우
		if (member == null) {
		    throw new UsernameNotFoundException("UserNotFound");
		}

		String status = member.getStatus();

		// 2. 탈퇴회원 또는 휴면회원 체크 -> DisabledException 사용
		if ("DELETED".equals(status) || "INACTIVE".equals(status) || UserRole.INACTIVE.getValue().equals(member.getRole())) {
		    log.debug("[Login Check] 탈퇴/비활성화 회원 : " + id);
		    throw new DisabledException("UserDeletedOrInactive");
		}

		// 3. 정지 회원 체크 -> LockedException 사용
		if ("SUSPENDED".equals(status) || "SUSPENDED".equals(member.getRole())) {
		    log.debug("[Login Check] 이용 정지 회원 : " + id);
		    throw new LockedException("UserSuspended");
		}

		return new PrincipalDetails(member);
	}
}
