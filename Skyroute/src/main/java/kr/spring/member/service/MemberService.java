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
	//회원 탈퇴
	public void deleteAccount(Long member_id);
	// 회원가입 - 이메일 중복 체크
	public MemberVO selectCheckEmail(String email);
	
	//메인 화면 공항 리스트 조회
	public List<Map<String, Object>> selectAirportList();
	
	//운항 조회
	public List<Map<String, Object>> selectActiveRegionList();
	public List<Map<String, Object>> selectActiveRouteList();
	
	// 마이페이지 여권 정보 
	public Map<String, Object> selectSavedPassenger(Long memberId);
	public void upsertPassportInfo(Map<String, Object> paramMap);
	
	//마이페이지 관심노선
	public List<Map<String, Object>> selectFavoriteRouteList(long memberId);
	//마이페이지 관심노선 삭제
	public int deleteFavoriteRoute(Map<String, Object> paramMap);
}
