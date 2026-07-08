package kr.spring.member.dao;
import java.util.Map;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import kr.spring.member.vo.MemberVO;

@Mapper
public interface MemberMapper {
	// 아이디 중복 체크
	@Select("SELECT * FROM member WHERE login_id = #{id}")
    public MemberVO selectCheckMember(String id);
	// 회원 정보 조회
	@Select("SELECT * FROM member WHERE member_id = #{member_id}")
    public MemberVO selectMember(Long member_id);
	// 회원가입
	@Insert("INSERT INTO member (login_id, password, name, phone, email, zipcode, address1, address2) "
	          + "VALUES (#{id}, #{password}, #{name}, #{phone}, #{email}, #{zipcode}, #{address1}, #{address2})")
	public void insertMember(MemberVO member);
	// 이름과 이메일로 아이디 조회
	public String findIdByNameAndEmail(@Param("name") String name, @Param("email") String email);
	// 아이디, 이름, 이메일로 회원 존재 여부 확인 (true/false)
	public boolean checkMemberForResetPw(@Param("id") String id, @Param("name") String name, @Param("email") String email);
	// 임시 비밀번호로 업데이트
	public void updatePassword(@Param("id") String id, @Param("encodedPw") String encodedPw);	
}