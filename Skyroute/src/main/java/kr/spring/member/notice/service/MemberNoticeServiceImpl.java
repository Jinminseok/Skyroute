package kr.spring.member.notice.service;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.spring.member.notice.dao.MemberNoticeMapper;
import kr.spring.member.notice.vo.MemberNoticeVO;

@Service
@Transactional
public class MemberNoticeServiceImpl implements MemberNoticeService{

	@Autowired
	private MemberNoticeMapper memberNoticeMapper;
	
	@Override
	public List<MemberNoticeVO> selectList(Map<String, Object> map) {
		return memberNoticeMapper.selectList(map);
	}

	@Override
	public Integer selectRowCount(Map<String, Object> map) {
		return memberNoticeMapper.selectRowCount(map);
	}

	@Override
	public MemberNoticeVO selectNotice(Long notice_id) {
		return memberNoticeMapper.selectNotice(notice_id);
	}

}
