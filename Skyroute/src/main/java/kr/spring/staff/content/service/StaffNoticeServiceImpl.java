package kr.spring.staff.content.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.spring.staff.content.dao.StaffNoticeMapper;
import kr.spring.staff.content.vo.StaffNoticeCategoryStatsVO;
import kr.spring.staff.content.vo.StaffNoticeVO;
import kr.spring.util.NoticeCategoryUtil;

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
	public int selectRowCount(Map<String, Object> map) {
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

	@Override
	public List<StaffNoticeCategoryStatsVO> selectNoticeCategoryStats() {
		List<StaffNoticeCategoryStatsVO> dbList = staffNoticeMapper.selectNoticeCategoryStats();

		Map<String, Integer> countMap = new HashMap<String, Integer>();

		for (StaffNoticeCategoryStatsVO stats : dbList) {
			countMap.put(stats.getCategory(), stats.getCnt());
		}

		List<StaffNoticeCategoryStatsVO> result = new ArrayList<StaffNoticeCategoryStatsVO>();

		for (String category : NoticeCategoryUtil.getCategoryMap().keySet()) {
			StaffNoticeCategoryStatsVO stats = new StaffNoticeCategoryStatsVO();
			stats.setCategory(category);
			stats.setCnt(countMap.getOrDefault(category, 0));

			result.add(stats);
		}

		return result;
	}
}