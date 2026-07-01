package kr.spring.member.security;

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
	public UserDetails loadUserByUsername(String id) throws UsernameNotFoundException{
		log.debug("[Login Check 1 - UserSecurityService] 로그인 아이디 :" + id);
		MemberVO member = memberService.selectCheckMember(id);
		if (member==null || member.getRole().equals(UserRole.INACTIVE.getValue())) {
			log.debug("[Login Check 1] 로그인 아이디가 없거나 탈퇴회원");
			throw new UsernameNotFoundException("UserNotFound");
		}
		return new PrincipalDetails(member);
	}
}
