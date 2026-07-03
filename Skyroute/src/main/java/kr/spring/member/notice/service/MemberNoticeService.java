package kr.spring.member.notice.service;

import java.util.List;
import java.util.Map;

import kr.spring.member.notice.vo.MemberNoticeVO;

public interface MemberNoticeService {

	public List<MemberNoticeVO> selectList(Map<String,Object> map);
	public Integer selectRowCount(Map<String,Object> map);
	public MemberNoticeVO selectNotice(Long notice_id);
}
