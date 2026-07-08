package kr.spring.staff.content.service;

import java.util.List;
import java.util.Map;

import kr.spring.staff.content.vo.StaffNoticeVO;

public interface StaffNoticeService {

	public List<StaffNoticeVO> selectList(Map<String, Object> map);

	public Integer selectRowCount(Map<String, Object> map);

	public void insertNotice(StaffNoticeVO notice);

	public StaffNoticeVO selectNotice(Long notice_id);

	public void updateNotice(StaffNoticeVO notice);

	public void updateNoticePublic(StaffNoticeVO notice);

	public void deleteNotice(Long notice_id);
}