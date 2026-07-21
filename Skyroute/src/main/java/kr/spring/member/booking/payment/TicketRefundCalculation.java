package kr.spring.member.booking.payment;

import java.math.BigDecimal;

public record TicketRefundCalculation(
		
		// 티켓 한 장의 계산ㄴ 결과
		Long ticketId,
		Long policyId,
		Long originalAmount,
		BigDecimal appliedFeeRate,
		Long feeAmount,
		Long refundAmount,
		String refundType
) {
}