package kr.spring.staff.content.vo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import kr.spring.member.vo.MemberVO;
import kr.spring.util.NoticeCategoryUtil;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString(exclude = {"content"})
public class StaffNoticeVO {

	private long notice_id;

	@NotBlank
	@Size(max = 30)
	private String category;

	@NotBlank
	@Size(max = 200)
	private String title;

	@NotBlank
	private String content;

	@Pattern(regexp = "Y|N")
	private String is_public = "Y";

	private long created_by;

	private String created_at;
	private String updated_at;

	private MemberVO memberVO;

	public String getCategoryLabel() {
		return NoticeCategoryUtil.getLabel(category);
	}
}