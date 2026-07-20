package kr.spring.member.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import kr.spring.member.dao.MemberMapper;
import kr.spring.member.vo.MemberVO;

@Service
@Transactional
public class MemberServiceImpl implements MemberService{

	@Resource
	private MemberMapper memberMapper;
	
	@Override
	public void insertMember(MemberVO member) {
		memberMapper.insertMember(member);
	}	
	
	@Override
	public MemberVO selectCheckMember(String id) {
		return memberMapper.selectCheckMember(id);
	}

	@Override
	public MemberVO selectMember(Long mem_num) {
		return memberMapper.selectMember(mem_num);
	}

	@Override
	public void updateMemberProfile(MemberVO member) {
		memberMapper.updateMemberProfile(member);
	}

	@Override
	public List<Map<String, Object>> selectAirportList() {
		return memberMapper.selectAirportList();
	}

	@Override
	public void deleteAccount(Long member_id) {
		memberMapper.deleteAccount(member_id);
	}

	@Override
	public MemberVO selectCheckEmail(String email) {		
		return memberMapper.selectCheckEmail(email);
	}

	@Override
	public List<Map<String, Object>> selectActiveRegionList() {
		return memberMapper.selectActiveRegionList();
	}

	@Override
	public List<Map<String, Object>> selectActiveRouteList() {
		return memberMapper.selectActiveRouteList();
	}

	@Override
	public Map<String, Object> selectSavedPassenger(Long memberId) {
	    return memberMapper.selectSavedPassenger(memberId);
	}

	@Override
	public void upsertPassportInfo(Map<String, Object> paramMap) {
		memberMapper.upsertPassportInfo(paramMap);
	}

	@Override
	public List<Map<String, Object>> selectFavoriteRouteList(long memberId) {
		return memberMapper.selectFavoriteRouteList(memberId);
	}

	@Override
	public int deleteFavoriteRoute(Map<String, Object> paramMap) {
		return memberMapper.deleteFavoriteRoute(paramMap);
	}

	@Override
	public String toggleFavoriteFlight(long flightId, long memberId) {
		Map<String, Object> paramMap = new HashMap<>();
		paramMap.put("flightId", flightId);
		paramMap.put("memberId", memberId);

		// 1. 이미 하트가 눌려있는지 확인
		int count = memberMapper.selectFavoriteFlightCount(paramMap);

		if (count > 0) {
			// 2. 이미 등록되어 있다면 삭제 처리
			memberMapper.deleteFavoriteFlightByFlightId(paramMap);
			return "REMOVE_SUCCESS";
		} else {
			// 3. 등록되어 있지 않다면 신규 등록 처리
			memberMapper.insertFavoriteFlight(paramMap);
			return "ADD_SUCCESS";
		}
	}
	



}
