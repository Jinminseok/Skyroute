package kr.spring.member.booking.vo;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FlightSearchQueryVO {

	private Long departureAirportId;

	private Long arrivalAirportId;

	private Long seatClassId;

	private int passengerCount;

	private LocalDateTime departureStart;

	private LocalDateTime departureEnd;
}