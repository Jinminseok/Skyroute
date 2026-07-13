package kr.spring.member.booking.vo;

import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/* 티켓 1장 = 승객 1명 × 구간 1개 */
@Getter
@Setter
@ToString
public class SHTicketVO {

	private Long ticketId;

	private Long bookingId;

	private Long bookingPassengerId;

	private Long flightId;

	private Long seatId;

	/* OUTBOUND / INBOUND */
	private String legType;

	/* FLIGHT_FARE 에서 복사된 스냅샷 금액 */
	private Long fareAmount;

	/* HOLDING / CONFIRMED / RELEASED / CANCELLED */
	private String holdStatus;

	private LocalDateTime heldAt;

	private LocalDateTime expiredAt;

	private String checkinStatus;

	/* HOLD 유지 시간(분). insertTicket 에서 expired_at 계산에 사용 */
	private int holdMinutes;


	/* ===== 조인 컬럼 ===== */

	private String passengerName;

	private LocalDate passengerBirthDate;

	private String passengerType;

	private String seatNo;

	private String seatClassName;

	private String flightNo;

	private String departureIataCode;

	private String arrivalIataCode;

	private LocalDateTime departureTime;

	private LocalDateTime arrivalTime;


	public boolean isCancelled() {
		return "CANCELLED".equals(holdStatus)
				|| "RELEASED".equals(holdStatus);
	}
}