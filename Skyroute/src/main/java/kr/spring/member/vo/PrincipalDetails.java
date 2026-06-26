package kr.spring.member.vo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
/*
 * 로그인의 진행이 완료되면 시큐리티 session(Security ContextHolder)이 만들어짐.
 * Authentication 타입 객체가 생성되고 그 안에 User 정보가 담기게 되는데 있데 UserDetails 타입 객체가 User 정보 포함하게 됨.
 * Security Session => Authentication => UserDetails(PrincipalDetails)
 */
@Data
@Slf4j
public class PrincipalDetails implements UserDetails, OAuth2User {
	private MemberVO memberVO; // 콤포지션
	// OAuth 로그인 시 
	private Map<String, Object> attributes;

	// 일반 로그인 시 사용하는 생성자
	public PrincipalDetails(MemberVO memberVO) {
		this.memberVO=memberVO;
	}
	// OAuth 로그인 시 사용하는 생성자
	public PrincipalDetails(MemberVO memberVO, Map<String, Object> attributes) {
		this.memberVO=memberVO;
		this.attributes=attributes;
	}

	/**
	 * 사용자의 권한 목록을 반환
	 * 스프링 시큐리티에서 해당 사용자의 권한을 처리할 때 사용
	 */
	@Override 
	public Collection<? extends GrantedAuthority> getAuthorities() {
		Collection<GrantedAuthority> collect = new ArrayList<>(); // ArrayList는 Collection의 자식 타입이다.
		collect.add(new GrantedAuthority() {
			@Override
			public String getAuthority() {
				log.debug("memberVO : " + memberVO);
				return memberVO.getAuthority();
			}
		});
		return collect;
	}
	@Override 
	public String getPassword() {
		return memberVO.getPasswd();
	}
	
	//사용자의 아이디(username) 반환
	@Override 
	public String getUsername() {
		return memberVO.getId();
	}

	/**
	 * OAuth2 로그인 시 사용자 정보를 담고 있는 attributes 맵을 반환
	 */
	@Override
	public Map<String, Object> getAttributes() {
		return attributes;
	}

	/**
	 * OAuth2 로그인 시 식별자로 사용할 이름을 반환.
	 * 일반적으로 "sub", "id" 등의 고유 식별자를 사용할 수 있음.
	 * 현재는 null로 처리되어 있으며, 필요 시 수정 가능함
	 */
	@Override 
	public String getName() {
		return null;

	}
}