package kr.spring.member.booking.payment;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * PortOne V2 결제 단건 조회(GET /payments/{paymentId}) 응답 중
 * 검증에 필요한 필드만 담는 DTO.
 */
@Getter
@Setter
@ToString
public class SHIamportPayment {

	/** V2 결제 식별자. 우리가 채번한 merchant_uid 와 동일하다. */
	private String paymentId;

	/** PG사 거래번호(선택) */
	private String pgTxId;

	/** V2 상태값: READY / PAID / CANCELLED / FAILED / VIRTUAL_ACCOUNT_ISSUED ... */
	private String status;

	/** PortOne 서버가 알려주는 실제 결제 금액 (검증 기준, amount.total) */
	private Long amount;

	private String payMethod;   // method.type
	private String pgProvider;  // channel.pgProvider
	private String receiptUrl;

	/** 결제 완료 상태인지 (V2 는 대문자 PAID) */
	public boolean isPaid() {
		return "PAID".equals(status);
	}
}