package kr.spring.staff.content.vo;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class StaffFaqVO {
	private int faq_id;			// FAQ ID (PK)
	private String category;	// 카테고리 (예 : 예약, 결제, 환불)
	private String question;	// 질문
	private String answer;		// 답변
	private String is_visible;	// 노출 여부
	private String created_at;	// 등록일시
	private int priority_num;	// 우선순위
}
