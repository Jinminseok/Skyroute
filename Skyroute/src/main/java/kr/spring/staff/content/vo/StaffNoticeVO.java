package kr.spring.staff.content.vo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import kr.spring.member.vo.MemberVO;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString(exclude = {"content"})
public class StaffNoticeVO {

	private long notice_id;

	@NotBlank
	@Size(max = 200)
	private String title;

	@NotBlank
	private String content;

	@Pattern(regexp = "Y|N")
	private String is_public = "Y";

	private long created_by;

	// DB는 TIMESTAMP지만 화면 출력용이라 String으로 받음
	private String created_at;
	private String updated_at;

	// 등록자 이름 출력용
	private MemberVO memberVO;
}