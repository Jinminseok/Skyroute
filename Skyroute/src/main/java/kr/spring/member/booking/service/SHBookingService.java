package kr.spring.member.booking.service;

import java.util.List;

import kr.spring.member.booking.vo.SHBookingPassengerVO;
import kr.spring.member.booking.vo.SHBookingVO;
import kr.spring.member.booking.vo.SHReserveForm;
import kr.spring.member.booking.vo.SHSeatMapVO;

public interface SHBookingService {

	/* ===== 좌석 선택 ===== */

	/* 좌석맵 조회 (legType : OUTBOUND / INBOUND) */
	public SHSeatMapVO getSeatMap(Long flightId, Long seatClassId, String legType);


	/* ===== 예약 생성 ===== */

	/*
	 * 좌석 HOLD
	 *
	 * BOOKING(PENDING) + BOOKING_PASSENGER + TICKET(HOLDING) 을
	 * 한 트랜잭션으로 생성하고 booking_id 를 돌려준다.
	 * 좌석 경쟁에서 지면 SHSeatTakenException 이 발생한다.
	 */
	public Long holdSeats(SHReserveForm reserveForm, Long memberId);


	/* ===== 결제 ===== */

	/* 결제 준비 : PAYMENT(READY) 행 생성 후 merchant_uid 반환 */
	public String preparePayment(Long bookingId, Long memberId, String method);

	/*
	 * 결제 확정
	 *
	 * PortOne 검증 결과 금액이 일치하면
	 * PAYMENT → PAID, TICKET → CONFIRMED, BOOKING → CONFIRMED
	 */
	public void confirmPayment(Long bookingId, Long memberId, String impUid, String method, Long paidAmount);

	/* 결제 실패 / 사용자 취소 → 좌석 즉시 반납 */
	public void failPayment(Long bookingId, Long memberId);


	/* ===== 예약 조회 ===== */

	public List<SHBookingVO> getBookingList(Long memberId);

	public SHBookingVO getBookingDetail(Long bookingId, Long memberId);


	/* ===== 취소 ===== */

	/*
	 * PortOne 전액 취소 성공 후
	 * 내부 DB 상태를 일괄 반영한다.
	 */
	public Long applyFullCancellation(
	        Long bookingId,
	        Long memberId,
	        String reason
	);


	/* ===== 저장된 탑승객 ===== */

	public List<SHBookingPassengerVO> getSavedPassengerList(Long memberId);


	/* ===== 스케줄러 ===== */

	/* 만료된 HOLD 좌석 반납 */
	public int releaseExpiredSeats();
}