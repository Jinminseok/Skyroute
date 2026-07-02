package kr.spring.admin.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import kr.spring.member.vo.MemberVO;

@Mapper
public interface AdminMemberMapper {
	//권한에 따른 회원 목록 조회
	List<MemberVO> selectMemberListByRole(@Param("role") String role);
	
	
	
	// 회원 상세 조회
	MemberVO selectMemberById(@Param("member_id") long memberId);
	
	// 회원 상태, 권한, 정지사유 업데이트
	void updateMemberStatusAndRole(MemberVO memberVO);
	
	// 권한 및 검색어(아이디/이름)에 따른 회원 목록 조회
	List<MemberVO> selectMemberListByRoleAndKeyword(@Param("role") String role, @Param("keyword") String keyword);

	// 지상직(STAFF) 계정 생성
	void insertStaff(MemberVO memberVO);
}
