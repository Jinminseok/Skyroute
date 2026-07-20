package kr.spring.member.booking.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.spring.member.booking.dao.SHBookingMapper;
import kr.spring.member.booking.exception.SHSeatTakenException;
import kr.spring.member.booking.vo.SHBookingPassengerVO;
import kr.spring.member.booking.vo.SHBookingVO;
import kr.spring.member.booking.vo.SHPassengerForm;
import kr.spring.member.booking.vo.SHPaymentVO;
import kr.spring.member.booking.vo.SHReserveForm;
import kr.spring.member.booking.vo.SHSeatMapVO;
import kr.spring.member.booking.vo.SHSeatVO;
import kr.spring.member.booking.vo.SHTicketVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/*
 * 예약 서비스
 *
 * ────────────────────────────────────────────────────────────
 * [핵심 설계]
 *
 * 1. 좌석 재고 테이블은 없다.
 *    잔여 좌석 = SEAT - (CONFIRMED + 만료되지 않은 HOLDING) TICKET
 *
 * 2. 좌석 중복 점유는 DB 가 막는다.
 *    uq_ticket_active_seat 부분 유니크 인덱스 → 경쟁 시 ORA-00001
 *    애플리케이션 락(synchronized, SELECT FOR UPDATE)을 쓰지 않는다.
 *
 * 3. 유아(INFANT)는 좌석을 점유하지 않는다.
 *    BOOKING_PASSENGER 행은 만들되 TICKET 은 만들지 않는다.
 *
 * 4. 금액은 스냅샷이다.
 *    TICKET.fare_amount ← FLIGHT_FARE.price
 *    BOOKING.total_amount ← 생성 시점 티켓 합계. 이후 절대 수정하지 않는다.
 *    부분 취소 후 유효 금액은 살아 있는 티켓에서 파생한다.
 *
 * 5. 실패 경로는 커밋되어야 한다.
 *    결제 실패 시 좌석 반납(RELEASED)은 반드시 남아야 하므로
 *    failPayment 는 별도 트랜잭션으로 분리한다.
 * ────────────────────────────────────────────────────────────
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class SHBookingServiceImpl implements SHBookingService {

	private final SHBookingMapper shBookingMapper;


	/* 좌석 HOLD 유지 시간 (분) */
	private static final int HOLD_MINUTES = 10;


	/* ===================================================================
	   좌석 선택
	   =================================================================== */

	@Override
	@Transactional(readOnly = true)
	public SHSeatMapVO getSeatMap(Long flightId, Long seatClassId, String legType) {

		SHSeatMapVO seatMap = shBookingMapper.selectSeatMapHeader(flightId, seatClassId);

		if (seatMap == null) {
			throw new IllegalStateException(
					"예약할 수 없는 항공편입니다. (운임 미등록 또는 출발 시각 경과)");
		}

		seatMap.setLegType(legType);

		List<SHSeatVO> seatList = shBookingMapper.selectSeatList(flightId, seatClassId);

		if (seatList.isEmpty()) {
			throw new IllegalStateException(
					"해당 좌석 등급에 배정된 좌석이 없습니다.");
		}

		seatMap.setSeatList(seatList);
		
		seatMap.setMaxSeatColumns(
		        shBookingMapper.selectMaxSeatColumnsByFlight(
		                flightId
		        )
		);

		return seatMap;
	}


	/* ===================================================================
	   좌석 HOLD : BOOKING + BOOKING_PASSENGER + TICKET 을 한 트랜잭션으로
	   =================================================================== */

	@Override
	public Long holdSeats(SHReserveForm reserveForm, Long memberId) {

		validateReserveForm(reserveForm);

		/* 1) 구간별 운임 스냅샷을 미리 읽어 둔다 */
		SHSeatMapVO outbound = shBookingMapper.selectSeatMapHeader(
				reserveForm.getOutboundFlightId(),
				reserveForm.getSeatClassId());

		if (outbound == null) {
			throw new IllegalStateException("가는 편 항공권을 예약할 수 없습니다.");
		}

		SHSeatMapVO inbound = null;

		if (reserveForm.isRoundTrip()) {

			inbound = shBookingMapper.selectSeatMapHeader(
					reserveForm.getInboundFlightId(),
					reserveForm.getSeatClassId());

			if (inbound == null) {
				throw new IllegalStateException("오는 편 항공권을 예약할 수 없습니다.");
			}
		}


		/* 2) 좌석이 아직 비어 있는지 재확인 (친절한 에러 메시지를 위한 선검사) */
		checkSeatsAvailable(reserveForm.getOutboundFlightId(), reserveForm.getOutboundSeatIds());

		if (reserveForm.isRoundTrip()) {
			checkSeatsAvailable(reserveForm.getInboundFlightId(), reserveForm.getInboundSeatIds());
		}


		/* 3) BOOKING (PENDING) */
		SHBookingVO booking = new SHBookingVO();

		booking.setBookingNo(generateBookingNo());
		booking.setMemberId(memberId);
		booking.setTripType(reserveForm.getTripType());
		booking.setOutboundFlightId(reserveForm.getOutboundFlightId());
		booking.setInboundFlightId(reserveForm.getInboundFlightId());

		shBookingMapper.insertBooking(booking);

		Long bookingId = booking.getBookingId();


		/* 4) BOOKING_PASSENGER + TICKET */
		long totalAmount = 0L;

		int seatIndex = 0;

		for (SHPassengerForm passengerForm : reserveForm.getPassengers()) {

			SHBookingPassengerVO passenger = toPassengerVO(bookingId, passengerForm);

			shBookingMapper.insertBookingPassenger(passenger);

			/* 유아는 좌석을 점유하지 않으므로 티켓을 만들지 않는다 */
			if (!passengerForm.needsSeat()) {
				continue;
			}

			totalAmount += insertTicket(
					bookingId,
					passenger.getBookingPassengerId(),
					reserveForm.getOutboundFlightId(),
					reserveForm.getOutboundSeatIds().get(seatIndex),
					"OUTBOUND",
					outbound.getPrice());

			if (reserveForm.isRoundTrip()) {

				totalAmount += insertTicket(
						bookingId,
						passenger.getBookingPassengerId(),
						reserveForm.getInboundFlightId(),
						reserveForm.getInboundSeatIds().get(seatIndex),
						"INBOUND",
						inbound.getPrice());
			}

			seatIndex++;
		}


		/* 5) 원장 금액 확정 */
		shBookingMapper.updateBookingTotalAmount(bookingId, totalAmount);

		log.debug("<<좌석 HOLD>> booking_id={}, 좌석 {}석, 금액 {}원",
				bookingId, seatIndex, totalAmount);

		return bookingId;
	}


	private long insertTicket(Long bookingId, Long bookingPassengerId,
							  Long flightId, Long seatId,
							  String legType, Long fareAmount) {

		SHTicketVO ticket = new SHTicketVO();

		ticket.setBookingId(bookingId);
		ticket.setBookingPassengerId(bookingPassengerId);
		ticket.setFlightId(flightId);
		ticket.setSeatId(seatId);
		ticket.setLegType(legType);
		ticket.setFareAmount(fareAmount);
		ticket.setHoldMinutes(HOLD_MINUTES);

		try {
			shBookingMapper.insertTicket(ticket);

		} catch (DuplicateKeyException e) {

			/*
			 * uq_ticket_active_seat 위반 = 다른 사용자가 방금 같은 좌석을 잡았다.
			 * 트랜잭션 전체가 롤백되므로 이미 넣은 BOOKING / 승객 / 티켓도 사라진다.
			 */
			log.debug("<<좌석 선점 실패>> flight_id={}, seat_id={}", flightId, seatId);

			throw new SHSeatTakenException(
					"방금 다른 고객이 선택한 좌석입니다. 좌석을 다시 선택해 주세요.");
		}

		return fareAmount;
	}


	private void checkSeatsAvailable(Long flightId, List<Long> seatIds) {

		if (seatIds == null || seatIds.isEmpty()) {
			return;
		}

		int occupied = shBookingMapper.countOccupiedSeat(flightId, seatIds);

		if (occupied > 0) {
			throw new SHSeatTakenException(
					"이미 선택된 좌석이 포함되어 있습니다. 좌석을 다시 선택해 주세요.");
		}
	}


	/* ===================================================================
	   결제
	   =================================================================== */

	@Override
	public String preparePayment(Long bookingId, Long memberId, String method) {

		SHBookingVO booking = shBookingMapper.selectBooking(bookingId, memberId);

		if (booking == null) {
			throw new IllegalStateException("예약 정보를 찾을 수 없습니다.");
		}

		if (!"PENDING".equals(booking.getStatus())) {
			throw new IllegalStateException("결제할 수 있는 상태가 아닙니다.");
		}

		if (shBookingMapper.countActiveTicket(bookingId) == 0) {
			throw new SHSeatTakenException(
					"좌석 선점 시간이 만료되었습니다. 좌석을 다시 선택해 주세요.");
		}

		SHPaymentVO payment = shBookingMapper.selectPaymentByBooking(bookingId);

		/* 이미 READY 결제가 있으면 merchant_uid 를 재사용한다 (중복 생성 방지) */
		if (payment != null && "READY".equals(payment.getStatus())) {
			return payment.getMerchantUid();
		}

		SHPaymentVO newPayment = new SHPaymentVO();

		newPayment.setBookingId(bookingId);
		newPayment.setMethod(method);
		newPayment.setAmount(booking.getTotalAmount());
		newPayment.setMerchantUid(generateMerchantUid(bookingId));

		shBookingMapper.insertPayment(newPayment);

		return newPayment.getMerchantUid();
	}


	@Override
	public void confirmPayment(Long bookingId, Long memberId,
							   String impUid, String method, Long paidAmount) {

		SHBookingVO booking = shBookingMapper.selectBooking(bookingId, memberId);

		if (booking == null) {
			throw new IllegalStateException("예약 정보를 찾을 수 없습니다.");
		}

		/* 이미 확정된 예약이면 아무것도 하지 않는다 (결제 콜백 중복 호출 방어) */
		if ("CONFIRMED".equals(booking.getStatus())) {
			return;
		}

		if (!"PENDING".equals(booking.getStatus())) {
			throw new IllegalStateException("결제할 수 있는 상태가 아닙니다.");
		}

		/*
		 * 위변조 검증
		 *
		 * 클라이언트가 보낸 금액이 아니라, PortOne 서버에서 조회한 실제 결제 금액을
		 * 우리 원장 금액과 대조한다. 이 검증이 결제 파트의 존재 이유다.
		 */
		if (paidAmount == null || !paidAmount.equals(booking.getTotalAmount())) {

			log.warn("<<결제 금액 불일치>> booking_id={}, 원장={}, 결제={}",
					bookingId, booking.getTotalAmount(), paidAmount);

			throw new IllegalStateException("결제 금액이 예약 금액과 일치하지 않습니다.");
		}

		/* 좌석 확정 : 만료되지 않은 HOLDING 만 CONFIRMED 로 승격된다 */
		int confirmed = shBookingMapper.confirmTickets(bookingId);

		if (confirmed == 0) {
			throw new SHSeatTakenException(
					"좌석 선점 시간이 만료되었습니다. 결제를 취소하고 다시 예약해 주세요.");
		}

		SHPaymentVO payment = new SHPaymentVO();

		payment.setBookingId(bookingId);
		payment.setImpUid(impUid);
		payment.setMethod(method);

		shBookingMapper.updatePaymentPaid(payment);

		shBookingMapper.updateBookingStatus(bookingId, "CONFIRMED");

		log.debug("<<결제 확정>> booking_id={}, imp_uid={}, 좌석 {}석", bookingId, impUid, confirmed);
	}


	/*
	 * 결제 실패 / 사용자 이탈
	 *
	 * REQUIRES_NEW : 호출한 쪽에서 예외가 터져 롤백되더라도
	 * 좌석 반납(RELEASED)은 반드시 커밋되어야 한다.
	 */
	@Override
	@Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW)
	public void failPayment(Long bookingId, Long memberId) {

		SHBookingVO booking = shBookingMapper.selectBooking(bookingId, memberId);

		if (booking == null || !"PENDING".equals(booking.getStatus())) {
			return;
		}

		int released = shBookingMapper.releaseTicketsByBooking(bookingId);

		shBookingMapper.updatePaymentFailed(bookingId);

		shBookingMapper.updateBookingStatus(bookingId, "FAILED");

		log.debug("<<결제 실패 - 좌석 반납>> booking_id={}, {}석", bookingId, released);
	}


	/* ===================================================================
	   예약 조회
	   =================================================================== */

	@Override
	@Transactional(readOnly = true)
	public List<SHBookingVO> getBookingList(Long memberId) {
		return shBookingMapper.selectBookingList(memberId);
	}


	@Override
	@Transactional(readOnly = true)
	public SHBookingVO getBookingDetail(Long bookingId, Long memberId) {

		SHBookingVO booking = shBookingMapper.selectBooking(bookingId, memberId);

		if (booking == null) {
			return null;
		}

		booking.setTicketList(shBookingMapper.selectTicketList(bookingId));
		booking.setPayment(shBookingMapper.selectPaymentByBooking(bookingId));

		return booking;
	}


	/* ===================================================================
	   취소 / 환불
	   =================================================================== */

	@Override
	@Transactional
	public Long applyFullCancellation(
	        Long bookingId,
	        Long memberId,
	        String reason) {

	    SHBookingVO booking =
	            shBookingMapper.selectBooking(
	                    bookingId,
	                    memberId
	            );

	    if (booking == null) {
	        throw new IllegalStateException(
	                "예약 정보를 찾을 수 없습니다."
	        );
	    }

	    SHPaymentVO payment =
	            shBookingMapper
	                    .selectPaymentByBooking(
	                            bookingId
	                    );

	    /*
	     * 같은 요청이 다시 들어온 경우의 멱등 처리.
	     */
	    if ("CANCELLED".equals(
	                booking.getStatus())
	            && payment != null
	            && "REFUNDED".equals(
	                payment.getStatus())) {

	        return payment.getRefundAmount() == null
	                ? payment.getAmount()
	                : payment.getRefundAmount();
	    }

	    if (!"CONFIRMED".equals(
	            booking.getStatus())) {

	        throw new IllegalStateException(
	                "취소할 수 없는 예약 상태입니다."
	        );
	    }

	    if (payment == null
	            || !"PAID".equals(
	                    payment.getStatus())) {

	        throw new IllegalStateException(
	                "환불 가능한 결제 정보가 없습니다."
	        );
	    }

	    /*
	     * 파사드에서 검증했지만,
	     * DB 반영 직전 상태가 달라졌을 수 있으므로 재검증한다.
	     */
	    if (booking.getOutboundDepartureTime() == null
	            || !booking
	                    .getOutboundDepartureTime()
	                    .isAfter(LocalDateTime.now())) {

	        throw new IllegalStateException(
	                "이미 출발했거나 출발 시각이 지난 "
	                + "예약은 취소할 수 없습니다."
	        );
	    }

	    List<SHTicketVO> tickets =
	            shBookingMapper.selectTicketList(
	                    bookingId
	            );

	    if (tickets.isEmpty()) {
	        throw new IllegalStateException(
	                "취소할 티켓이 없습니다."
	        );
	    }

	    long refundAmount = 0L;

	    for (SHTicketVO ticket : tickets) {

	        if (!"CONFIRMED".equals(
	                ticket.getHoldStatus())) {

	            throw new IllegalStateException(
	                    "이미 취소됐거나 유효하지 않은 "
	                    + "티켓이 포함되어 있습니다."
	            );
	        }

	        if (!"NOT_CHECKED_IN".equals(
	                ticket.getCheckinStatus())) {

	            throw new IllegalStateException(
	                    "체크인 또는 탑승 처리된 "
	                    + "항공권은 취소할 수 없습니다."
	            );
	        }

	        if (ticket.getFareAmount() == null) {
	            throw new IllegalStateException(
	                    "티켓 환불 금액이 없습니다."
	            );
	        }

	        refundAmount += ticket.getFareAmount();
	    }

	    if (!refundAmountEquals(
	            booking,
	            payment,
	            refundAmount)) {

	        throw new IllegalStateException(
	                "예약 원장 금액과 환불 대상 금액이 "
	                + "일치하지 않습니다."
	        );
	    }

	    /*
	     * CONFIRMED + 미체크인 티켓만 취소한다.
	     */
	    int cancelledTickets =
	            shBookingMapper
	                    .cancelAllConfirmedTickets(
	                            bookingId
	                    );

	    /*
	     * 조회한 티켓 수와 실제 UPDATE 수가 다르면
	     * 동시 상태 변경이 발생한 것이므로 전체 롤백한다.
	     */
	    if (cancelledTickets != tickets.size()) {
	        throw new IllegalStateException(
	                "티켓 상태가 변경되어 "
	                + "예약 취소를 완료할 수 없습니다."
	        );
	    }

	    /*
	     * REFUND는 티켓별 이력으로 저장한다.
	     */
	    for (SHTicketVO ticket : tickets) {

	        shBookingMapper.insertRefund(
	                payment.getPaymentId(),
	                ticket.getTicketId(),
	                ticket.getFareAmount(),
	                reason
	        );
	    }

	    int paymentUpdated =
	            shBookingMapper
	                    .updatePaymentFullRefund(
	                            payment.getPaymentId(),
	                            refundAmount
	                    );

	    if (paymentUpdated != 1) {
	        throw new IllegalStateException(
	                "결제 상태가 변경되어 "
	                + "환불 내역을 반영할 수 없습니다."
	        );
	    }

	    int bookingUpdated =
	            shBookingMapper
	                    .updateBookingCancelled(
	                            bookingId
	                    );

	    if (bookingUpdated != 1) {
	        throw new IllegalStateException(
	                "예약 상태가 변경되어 "
	                + "취소를 완료할 수 없습니다."
	        );
	    }

	    /*
	     * BOOKING.total_amount는 수정하지 않는다.
	     * 예약·결제 당시 원장 금액으로 보존한다.
	     */
	    log.info(
	            "<<예약 전체 취소 DB 반영>> "
	            + "bookingId={}, "
	            + "ticketCount={}, "
	            + "refundAmount={}",
	            bookingId,
	            tickets.size(),
	            refundAmount
	    );

	    return refundAmount;
	}


	private boolean refundAmountEquals(
	        SHBookingVO booking,
	        SHPaymentVO payment,
	        long refundAmount) {

	    return booking.getTotalAmount() != null
	            && payment.getAmount() != null
	            && booking.getTotalAmount()
	                    == refundAmount
	            && payment.getAmount()
	                    == refundAmount;
	}

	/* ===================================================================
	   저장된 탑승객
	   =================================================================== */

	@Override
	@Transactional(readOnly = true)
	public List<SHBookingPassengerVO> getSavedPassengerList(Long memberId) {
		return shBookingMapper.selectSavedPassengerList(memberId);
	}


	/* ===================================================================
	   스케줄러
	   =================================================================== */

	@Override
	public int releaseExpiredSeats() {

		int released = shBookingMapper.releaseExpiredTickets();

		if (released > 0) {

			int failed = shBookingMapper.failEmptyPendingBookings();

			log.debug("<<만료 좌석 반납>> 좌석 {}석, 실패 처리된 예약 {}건", released, failed);
		}

		return released;
	}


	/* ===================================================================
	   내부 유틸
	   =================================================================== */

	private void validateReserveForm(SHReserveForm form) {

		if (!form.isPassengerReady()) {
			throw new IllegalStateException("탑승객 정보가 완성되지 않았습니다.");
		}

		if (!form.isSeatReady()) {
			throw new IllegalStateException("좌석 선택이 완성되지 않았습니다.");
		}

		if (form.isRoundTrip() && form.getInboundFlightId() == null) {
			throw new IllegalStateException("오는 편 항공편이 선택되지 않았습니다.");
		}

		/* 좌석 중복 선택 방어 (같은 좌석을 두 승객에게) */
		if (hasDuplicate(form.getOutboundSeatIds())
				|| hasDuplicate(form.getInboundSeatIds())) {

			throw new IllegalStateException("같은 좌석을 두 번 선택할 수 없습니다.");
		}
	}


	private boolean hasDuplicate(List<Long> seatIds) {

		if (seatIds == null) {
			return false;
		}

		return seatIds.size() != new ArrayList<>(new java.util.HashSet<>(seatIds)).size();
	}


	private SHBookingPassengerVO toPassengerVO(Long bookingId, SHPassengerForm form) {

		SHBookingPassengerVO passenger = new SHBookingPassengerVO();

		passenger.setBookingId(bookingId);
		passenger.setSavedPassengerId(form.getSavedPassengerId());
		passenger.setName(form.getName());
		passenger.setBirthDate(form.getBirthDate());
		passenger.setPhone(form.getPhone());
		passenger.setGender(form.getGender());
		passenger.setPassportNo(form.getPassportNo());
		passenger.setPassportExpiry(form.getPassportExpiry());

		/* 예약 시점에 한 번만 판정해서 저장한다. 이후 재계산하지 않는다. */
		passenger.setPassengerType(form.getPassengerType());

		return passenger;
	}


	/* SR + yyMMddHHmmss + 3자리 난수 */
	private String generateBookingNo() {

		String stamp = LocalDateTime.now()
				.format(DateTimeFormatter.ofPattern("yyMMddHHmmss"));

		int random = ThreadLocalRandom.current().nextInt(100, 1000);

		return "SR" + stamp + random;
	}


	private String generateMerchantUid(Long bookingId) {

		return "SR" + bookingId + System.currentTimeMillis();
	}

}