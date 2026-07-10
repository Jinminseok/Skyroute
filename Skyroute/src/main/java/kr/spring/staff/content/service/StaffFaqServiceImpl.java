package kr.spring.staff.content.service;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.spring.staff.content.dao.StaffFaqMapper;
import kr.spring.staff.content.vo.StaffFaqVO;

@Service
@Transactional
public class StaffFaqServiceImpl implements StaffFaqService{
	
	@Autowired
	private StaffFaqMapper staffFaqMapper;

	@Override
	public List<StaffFaqVO> selectFaqList(Map<String, Object> map) {
		return staffFaqMapper.selectFaqList(map);
	}

	@Override
	public int selectRowCount(Map<String, Object> map) {
		return staffFaqMapper.selectRowCount(map);
	}

	@Override
	public StaffFaqVO selectFaq(int faq_id) {
		return staffFaqMapper.selectFaq(faq_id);
	}

	@Override
	public void insertFaq(StaffFaqVO faqVO) {
		staffFaqMapper.insertFaq(faqVO);
	}

	@Override
	public void updateFaq(StaffFaqVO faqVO) {
		staffFaqMapper.updateFaq(faqVO);
	}

	@Override
	public void deleteFaq(int faq_id) {
		staffFaqMapper.deleteFaq(faq_id);
	}

	@Override
	public void updateFaqVisible(Map<String, Object> map) {
		staffFaqMapper.updateFaqVisible(map);
	}

}
