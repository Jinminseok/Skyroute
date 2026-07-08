package kr.spring.staff.content.service;

import java.util.List;
import java.util.Map;

import kr.spring.staff.content.vo.StaffNoticeCategoryStatsVO;
import kr.spring.staff.content.vo.StaffNoticeVO;

public interface StaffNoticeService {

	public int selectRowCount(Map<String, Object> map);
	public List<StaffNoticeVO> selectList(Map<String, Object> map);

	public List<StaffNoticeCategoryStatsVO> selectNoticeCategoryStats();

	public void insertNotice(StaffNoticeVO staffNoticeVO);
	public StaffNoticeVO selectNotice(Long notice_id);
	public void updateNotice(StaffNoticeVO staffNoticeVO);
	public void updateNoticePublic(StaffNoticeVO staffNoticeVO);
	public void deleteNotice(Long notice_id);
}