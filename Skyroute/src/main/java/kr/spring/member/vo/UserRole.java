package kr.spring.member.vo;

import lombok.Getter;

@Getter
public enum UserRole {
	//탈퇴,정지,회원,관리자
	INACTIVE("INACTIVE"), SUSPENDED("SUSPENDED"), USER("USER"), ADMIN("ADMIN"), STAFF("STAFF");

	private String value;

	UserRole(String value) {
		this.value = value;
	}

}
