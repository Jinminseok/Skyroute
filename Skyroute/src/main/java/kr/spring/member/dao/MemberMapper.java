package kr.spring.member.dao;
import java.util.Map;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
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
	          + "VALUES (#{id}, #{passwd}, #{name}, #{phone}, #{email}, #{zipcode}, #{address1}, #{address2})")
	public void insertMember(MemberVO member);
	
}