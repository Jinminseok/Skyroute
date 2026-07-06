package kr.spring.staff.notice.service;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.spring.staff.notice.dao.StaffNoticeMapper;
import kr.spring.staff.notice.vo.StaffNoticeVO;

@Service
@Transactional
public class StaffNoticeServiceImpl implements StaffNoticeService {

	@Autowired
	private StaffNoticeMapper staffNoticeMapper;

	@Override
	public List<StaffNoticeVO> selectList(Map<String, Object> map) {
		return staffNoticeMapper.selectList(map);
	}

	@Override
	public Integer selectRowCount(Map<String, Object> map) {
		return staffNoticeMapper.selectRowCount(map);
	}

	@Override
	public void insertNotice(StaffNoticeVO notice) {
		staffNoticeMapper.insertNotice(notice);
	}

	@Override
	public StaffNoticeVO selectNotice(Long notice_id) {
		return staffNoticeMapper.selectNotice(notice_id);
	}

	@Override
	public void updateNotice(StaffNoticeVO notice) {
		staffNoticeMapper.updateNotice(notice);
	}

	@Override
	public void updateNoticePublic(StaffNoticeVO notice) {
		staffNoticeMapper.updateNoticePublic(notice);
	}

	@Override
	public void deleteNotice(Long notice_id) {
		staffNoticeMapper.deleteNotice(notice_id);
	}
}