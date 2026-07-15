package kr.spring.member.booking.payment;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * PortOne V1 결제 단건 조회(GET /payments/{imp_uid}) 응답 중
 * 검증에 필요한 필드만 담는 DTO.
 */
@Getter
@Setter
@ToString
public class SHIamportPayment {

	private String impUid;
	private String merchantUid;

	/** ready / paid / cancelled / failed */
	private String status;

	/** PortOne 서버가 알려주는 실제 결제 금액 (검증 기준) */
	private Long amount;

	private String payMethod;   // card / kakaopay / vbank ...
	private String pgProvider;
	private String receiptUrl;

	/** 결제 완료 상태인지 */
	public boolean isPaid() {
		return "paid".equals(status);
	}
}