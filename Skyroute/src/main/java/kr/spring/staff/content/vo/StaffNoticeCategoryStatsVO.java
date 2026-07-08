package kr.spring.staff.content.vo;

import kr.spring.util.NoticeCategoryUtil;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class StaffNoticeCategoryStatsVO {

	private String category;
	private int cnt;

	public String getCategoryLabel() {
		return NoticeCategoryUtil.getLabel(category);
	}
}