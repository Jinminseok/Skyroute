package kr.spring.staff.content.dao;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

import kr.spring.staff.content.vo.StaffFaqVO;

@Mapper
public interface StaffFaqMapper {
	// 1. FAQ 목록 조회 (카테고리 및 검색어 필터링 포함)
    public List<StaffFaqVO> selectFaqList(Map<String, Object> map);
    
    // 2. FAQ 전체 데이터 개수 (페이징용)
    public int selectRowCount(Map<String, Object> map);
    
    // 3. FAQ 상세 조회
    public StaffFaqVO selectFaq(int faq_id);
    
    // 4. FAQ 등록
    public void insertFaq(StaffFaqVO faqVO);
    
    // 5. FAQ 수정
    public void updateFaq(StaffFaqVO faqVO);
    
    // 6. FAQ 삭제 (완전 삭제)
    public void deleteFaq(int faq_id);
    
    // 7. FAQ 노출/비노출 상태 즉시 변경 (토글용)
    public void updateFaqVisible(Map<String, Object> map);

    // 8. 사용자용 (페이징 X, 우선순위 정렬)
    public List<StaffFaqVO> selectMemberFaqList(Map<String, Object> map);
    
    // 9. 관리자 우선순위 지정 화면용
    public List<StaffFaqVO> selectFaqPriorityList();
    
    // 10. 우선순위 번호 수정
    public void updateFaqPriority(StaffFaqVO faqVO);
}
