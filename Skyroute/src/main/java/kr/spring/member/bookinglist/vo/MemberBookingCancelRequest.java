package kr.spring.member.bookinglist.vo;

/**
 * 회원 예약 전체 취소 요청.
 */
public record MemberBookingCancelRequest(

        Long bookingId,

        String reason

) {
}