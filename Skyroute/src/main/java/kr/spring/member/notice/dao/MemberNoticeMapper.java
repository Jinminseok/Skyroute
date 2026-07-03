package kr.spring.member.notice.dao;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

import kr.spring.member.notice.vo.MemberNoticeVO;

@Mapper
public interface MemberNoticeMapper {
	
	//공지사항 목록
	public List<MemberNoticeVO> selectList(Map<String, Object> map);
	
	//공지사항 전체/검색 레코드 수
	public Integer selectRowCount(Map<String, Object> map);
	
	//공지사항 상세
	public MemberNoticeVO selectNotice(Long notice_id);
}
