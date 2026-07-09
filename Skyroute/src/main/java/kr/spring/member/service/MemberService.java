package kr.spring.member.service;

import java.util.List;
import java.util.Map;

import kr.spring.member.vo.MemberVO;

public interface MemberService {
	//회원관리 - 일반회원
	public void insertMember(MemberVO member);
	public MemberVO selectCheckMember(String id);
	public MemberVO selectMember(Long mem_num);
	//회원 정보 수정
	public void updateMemberProfile(MemberVO member);

}
