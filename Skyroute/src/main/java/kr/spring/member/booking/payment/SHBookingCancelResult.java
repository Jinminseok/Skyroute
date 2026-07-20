package kr.spring.member.booking.payment;

/**
 * 예약 전체 취소 결과.
 */
public record SHBookingCancelResult(

        Long bookingId,

        Long refundAmount,

        String cancellationId,

        boolean alreadyCompleted

) {
}