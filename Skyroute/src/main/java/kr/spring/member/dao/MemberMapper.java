package kr.spring.member.dao;
import java.util.List;
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
	public void insertMember(MemberVO member);
	//회원탈퇴
	public void deleteAccount(Long member_id);
	// 회원가입 - 이메일 중복 체크
	public MemberVO selectCheckEmail(String email);
	
	// 이름과 이메일로 아이디 조회
	public String findIdByNameAndEmail(@Param("name") String name, @Param("email") String email);
	// 아이디, 이름, 이메일로 회원 존재 여부 확인 (true/false)
	public boolean checkMemberForResetPw(@Param("id") String id, @Param("name") String name, @Param("email") String email);
	// 임시 비밀번호로 업데이트
	public void updatePassword(@Param("id") String id, @Param("encodedPw") String encodedPw);	
	//회원정보 수정
	public void updateMemberProfile(MemberVO member);

	//메인 페이지 공항 리스트 조회
	public List<Map<String, Object>> selectAirportList();
	
		
	//운항 조회
	public List<Map<String, Object>> selectActiveRegionList();
	public List<Map<String, Object>> selectActiveRouteList();
	
	
	// 마이페이지 여권 정보
	public Map<String, Object> selectSavedPassenger(@Param("memberId") Long memberId);
	public void upsertPassportInfo(Map<String, Object> paramMap);
	
	//마이페이지 관심노선
	public List<Map<String, Object>> selectFavoriteRouteList(long memberId);
	//마이페이지 관심노선 삭제
	public int deleteFavoriteRoute(Map<String, Object> paramMap);

}