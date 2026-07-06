package kr.spring.staff.notice.dao;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

import kr.spring.staff.notice.vo.StaffNoticeVO;

@Mapper
public interface StaffNoticeMapper {

	// 공지사항 목록
	public List<StaffNoticeVO> selectList(Map<String, Object> map);

	// 공지사항 전체/검색 레코드 수
	public Integer selectRowCount(Map<String, Object> map);

	// 공지사항 등록
	public void insertNotice(StaffNoticeVO notice);

	// 공지사항 상세
	public StaffNoticeVO selectNotice(Long notice_id);

	// 공지사항 수정
	public void updateNotice(StaffNoticeVO notice);

	// 공개/비공개 변경
	public void updateNoticePublic(StaffNoticeVO notice);

	// 공지사항 삭제
	public void deleteNotice(Long notice_id);
}