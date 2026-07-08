package kr.spring.member.service;

import org.apache.ibatis.annotations.Param;

import kr.spring.member.vo.MemberVO;

public interface MemberMailService {
	public void sendMail(String toEmail, String subject, String body);
	// 이름과 이메일로 아이디 조회
	public String findIdByNameAndEmail(@Param("name") String name, @Param("email") String email);
	// 아이디, 이름, 이메일로 회원 존재 여부 확인 (true/false)
	public boolean checkMemberForResetPw(@Param("id") String id, @Param("name") String name, @Param("email") String email);
	// 임시 비밀번호로 업데이트
	public void updatePassword(@Param("id") String id, @Param("encodedPw") String encodedPw);	
}
