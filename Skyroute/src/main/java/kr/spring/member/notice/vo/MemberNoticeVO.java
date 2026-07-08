package kr.spring.member.notice.vo;

import kr.spring.member.vo.MemberVO;
import kr.spring.util.NoticeCategoryUtil;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString(exclude = {"content"})
public class MemberNoticeVO {
	
	private long notice_id;
	private String category;
	private String title;
	private String content;
	private String is_public;
	private long created_by;
	private String created_at;
	private String updated_at;
	
	//작성자 정보
	private MemberVO memberVO;
	
	public String getCategoryLabel() {
		return NoticeCategoryUtil.getLabel(category);
	}
}
