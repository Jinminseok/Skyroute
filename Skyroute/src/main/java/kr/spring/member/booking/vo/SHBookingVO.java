package kr.spring.member.booking.vo;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/* 예약 1건 (목록 / 상세 공용) */
@Getter
@Setter
@ToString
public class SHBookingVO {

	private Long bookingId;

	private String bookingNo;

	private Long memberId;

	private String tripType;

	private Long outboundFlightId;

	private Long inboundFlightId;

	/* PENDING / CONFIRMED / CANCELLED / FAILED */
	private String status;

	/* 결제 시점에 굳어지는 원장 금액. 이후 절대 수정하지 않는다. */
	private Long totalAmount;

	private LocalDateTime createdAt;

	private LocalDateTime cancelledAt;


	/* ===== 조인 컬럼 ===== */

	private String outboundFlightNo;

	private String outboundDepartureIata;

	private String outboundArrivalIata;

	private LocalDateTime outboundDepartureTime;

	private String inboundFlightNo;

	private String inboundDepartureIata;

	private String inboundArrivalIata;

	private LocalDateTime inboundDepartureTime;

	private int passengerCount;

	/* 취소되지 않은 티켓 기준 유효 금액 (부분 취소 반영) */
	private Long validAmount;


	/* ===== 상세 화면용 ===== */

	private List<SHTicketVO> ticketList = new ArrayList<>();

	private SHPaymentVO payment;


	public boolean isCancelable() {
		return "CONFIRMED".equals(status);
	}
}