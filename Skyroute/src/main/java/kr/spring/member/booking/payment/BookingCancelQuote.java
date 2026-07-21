package kr.spring.member.booking.payment;

import java.util.List;

public record BookingCancelQuote(
		
		// 예약 전체의 원금합계, 수수료합계, 실제 환불액합계, 티켓별 계산결과
		Long bookingId,
		Long originalAmount,
		Long totalFeeAmount,
		Long totalRefundAmount,
		List<TicketRefundCalculation> ticketCalculations
) {

	public BookingCancelQuote {
		ticketCalculations = List.copyOf(ticketCalculations);
	}
}