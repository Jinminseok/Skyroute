package kr.spring.member.booking.vo;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class SHPaymentVO {

	private Long paymentId;
	private Long bookingId;

	/* KAKAOPAY / CARD / VBANK / TOSSPAY / TRANSFER */
	private String method;

	private Long amount;

	/* READY / PAID / FAILED / CANCELLED / PARTIAL_REFUNDED / REFUNDED */
	private String status;

	private String impUid;
	private String merchantUid;

	/* PORTONE / TOSS_PAYMENTS */
	private String paymentProvider;

	/* Toss Payments paymentKey */
	private String providerPaymentKey;

	/* Y / N */
	private String partialCancelableYn;

	private LocalDateTime paidAt;
	private Long refundAmount;
	private LocalDateTime refundedAt;
	private LocalDateTime createdAt;
}