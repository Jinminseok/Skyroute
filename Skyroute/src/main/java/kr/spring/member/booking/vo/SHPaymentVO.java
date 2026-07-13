package kr.spring.member.booking.vo;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/*
 * 결제
 *
 * imp_uid : PortOne 이 발급하는 거래 식별자
 * merchant_uid : 우리가 만들어 PortOne 에 넘기는 주문 번호
 */
@Getter
@Setter
@ToString
public class SHPaymentVO {

	private Long paymentId;

	private Long bookingId;

	/* KAKAOPAY / CARD / VBANK */
	private String method;

	private Long amount;

	/* READY / PAID / FAILED / CANCELLED / PARTIAL_REFUNDED / REFUNDED */
	private String status;

	private String impUid;

	private String merchantUid;

	private LocalDateTime paidAt;

	private Long refundAmount;

	private LocalDateTime refundedAt;

	private LocalDateTime createdAt;
}