package kr.spring.member.booking.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import kr.spring.member.booking.vo.SHBookingPassengerVO;
import kr.spring.member.booking.vo.SHBookingVO;
import kr.spring.member.booking.vo.SHPaymentVO;
import kr.spring.member.booking.vo.SHSeatMapVO;
import kr.spring.member.booking.vo.SHSeatVO;
import kr.spring.member.booking.vo.SHTicketVO;

@Mapper
public interface SHBookingMapper {

	/* ===================== 좌석 선택 ===================== */

	/* 항공편 + 좌석등급 요약 (FLIGHT_FARE 스냅샷 가격 포함) */
	public SHSeatMapVO selectSeatMapHeader(@Param("flightId") Long flightId,
										   @Param("seatClassId") Long seatClassId);

	/* 좌석 목록 + 점유 여부 */
	public List<SHSeatVO> selectSeatList(@Param("flightId") Long flightId,
										 @Param("seatClassId") Long seatClassId);

	/* 선택한 좌석이 아직 비어 있는지 재확인 (HOLD 직전 방어) */
	public int countOccupiedSeat(@Param("flightId") Long flightId,
								 @Param("seatIds") List<Long> seatIds);


	/* ===================== 예약 생성 (좌석 HOLD) ===================== */

	public void insertBooking(SHBookingVO bookingVO);

	public void insertBookingPassenger(SHBookingPassengerVO passengerVO);

	public void insertTicket(SHTicketVO ticketVO);

	public void updateBookingTotalAmount(@Param("bookingId") Long bookingId,
										 @Param("totalAmount") Long totalAmount);


	/* ===================== 결제 ===================== */

	public void insertPayment(SHPaymentVO paymentVO);

	public SHPaymentVO selectPaymentByBooking(@Param("bookingId") Long bookingId);

	public void updatePaymentPaid(SHPaymentVO paymentVO);

	public void updatePaymentFailed(@Param("bookingId") Long bookingId);

	/* 결제 성공 → 좌석 확정 */
	public int confirmTickets(@Param("bookingId") Long bookingId);

	public void updateBookingStatus(@Param("bookingId") Long bookingId,
									@Param("status") String status);


	/* ===================== 좌석 해제 ===================== */

	public int releaseTicketsByBooking(@Param("bookingId") Long bookingId);

	/* 만료된 HOLDING 좌석 일괄 해제 (스케줄러) */
	public int releaseExpiredTickets();

	/* 살아 있는 티켓이 없는 PENDING 예약을 FAILED 로 */
	public int failEmptyPendingBookings();


	/* ===================== 예약 조회 ===================== */

	public List<SHBookingVO> selectBookingList(@Param("memberId") Long memberId);

	public SHBookingVO selectBooking(@Param("bookingId") Long bookingId,
									 @Param("memberId") Long memberId);

	public List<SHTicketVO> selectTicketList(@Param("bookingId") Long bookingId);


	/* ===================== 취소 / 환불 ===================== */

	public List<SHTicketVO> selectCancelableTicketList(@Param("bookingId") Long bookingId,
													   @Param("bookingPassengerId") Long bookingPassengerId);

	public int cancelTickets(@Param("bookingId") Long bookingId,
							 @Param("bookingPassengerId") Long bookingPassengerId);

	public void insertRefund(@Param("paymentId") Long paymentId,
							 @Param("ticketId") Long ticketId,
							 @Param("amount") Long amount,
							 @Param("reason") String reason);

	public void updatePaymentRefund(@Param("paymentId") Long paymentId,
									@Param("refundAmount") Long refundAmount,
									@Param("status") String status);

	public int countActiveTicket(@Param("bookingId") Long bookingId);


	/* ===================== 저장된 탑승객 ===================== */

	public List<SHBookingPassengerVO> selectSavedPassengerList(@Param("memberId") Long memberId);

	public void insertSavedPassenger(SHBookingPassengerVO passengerVO);
}